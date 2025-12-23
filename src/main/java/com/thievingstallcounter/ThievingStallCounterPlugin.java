package com.thievingstallcounter;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

@Slf4j
@PluginDescriptor(
	name = "Thieving Stall Counter"
)
public class ThievingStallCounterPlugin extends Plugin
{
	private HashMap<String, Integer> petBaseChance = new HashMap<>();
	boolean thievingStall,thievedStallsRecently,loadedSession;
	int stallsThieved = 0, overlayTimer = 0, resetThievingBoolTimer = 0;
	double totalPetChance = 1;
	double petDryChance = 0;
	String stallName = null;

	@Inject
	private Client client;

	@Inject
	private ThievingStallCounterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ThievingStallCounterOverlay overlay;

	@Inject
	private Gson GSON;
	public static File DATA_FOLDER;
	static {
		DATA_FOLDER = new File(RuneLite.RUNELITE_DIR, "thieving-stall-counter");
		DATA_FOLDER.mkdirs();
	}

	@Override
	protected void startUp() throws Exception
	{
		if (client.getGameState().equals(GameState.LOGGED_IN)
				&& client.getLocalPlayer().getName() != null) {
			importData();
			loadedSession = true;
		}

		overlayManager.add(overlay);
		petBaseChance.put("veg stall", 206777);
		petBaseChance.put("bakery stall", 124066);
		petBaseChance.put("crafting stall", 47718);
		petBaseChance.put("food stall", 47718);
		petBaseChance.put("silk stall", 68926);
		petBaseChance.put("tea stall", 68926);
		petBaseChance.put("fruit stall", 124066);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		String username = client.getLocalPlayer().getName();
		if (username != null) exportData(new File(DATA_FOLDER, username + ".json"));
	}

	@Subscribe
	public void onGameTick(GameTick gameTick)
	{
		if (!loadedSession && client.getGameState().equals(GameState.LOGGED_IN)) {
			importData();
			loadedSession = true;
		}

		if (overlayTimer > 0)
		{
			overlayTimer -= 1;
		} else if (thievedStallsRecently) {
			thievedStallsRecently = false;
		}
		if (resetThievingBoolTimer > 0)
		{
			resetThievingBoolTimer -= 1;
		} else if (thievingStall) {
			thievingStall = false;
		}
	}


	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked)
	{
		if (menuOptionClicked.getMenuOption().toLowerCase().equals("steal-from") && menuOptionClicked.getMenuTarget().toLowerCase().contains("stall"))
		{
			thievingStall = true;
			thievedStallsRecently = true;
			String menuTarget = menuOptionClicked.getMenuTarget().toLowerCase();
			stallName = menuTarget.substring(menuTarget.indexOf('>')+1);
			//overlay stays active for 1 minute after last stall interaction
			overlayTimer = 100;
			resetThievingBoolTimer = 22;
		}
		else if (!menuOptionClicked.getMenuAction().name().contains("CC_OP") && !menuOptionClicked.getMenuAction().name().contains("EXAMINE"))
		{
			thievingStall = false;
			resetThievingBoolTimer = 0;
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (thievingStall && statChanged.getSkill() == Skill.THIEVING)
		{
			stallsThieved += 1;

			int baseChanceModifier = client.getRealSkillLevel(Skill.THIEVING) * 25;
			//Most stalls have a chance of 36,490 so didn't include each one in hashmap
			int realPetChance = petBaseChance.getOrDefault(stallName,36490) - baseChanceModifier;
			double petChance = 1.0D / realPetChance;
			totalPetChance *= (1-petChance);
			petDryChance = 1-totalPetChance;

			String username = client.getLocalPlayer().getName();
			if (username != null) {
				exportData(new File(DATA_FOLDER, username + ".json"));
			}
		}
	}

	private void exportData(File file) {
		if (!config.saveData()) return;

		ThievingStallCounterData data = new ThievingStallCounterData(
				stallsThieved, totalPetChance
		);
		try (Writer writer = new FileWriter(file)) {
			GSON.toJson(data, ThievingStallCounterData.class, writer);
		} catch (IOException | JsonIOException e) {
			log.error("Error while exporting Thieving Stall Counter data", e);
		}
	}

	private void importData() {
		if (!config.saveData()) return;

		DATA_FOLDER.mkdirs();
		String playerName = client.getLocalPlayer().getName();
		File data = new File(DATA_FOLDER, playerName + ".json");

		if (!data.exists()) {
			initializeCounterDataFile(data);
			return;
		}

		try (Reader reader = new FileReader(data)) {
			ThievingStallCounterData importedData = GSON.fromJson(reader, ThievingStallCounterData.class);
			stallsThieved = importedData.getStallsThieved();
			totalPetChance = importedData.getPetChanceOfBeingDry();
			petDryChance = 1 - totalPetChance;
		} catch (IOException e) {
			log.warn("Error while reading Thieving Stall Counter data", e);
		} catch (JsonParseException e) {
			log.warn("Error while importing Thieving Stall Counter data", e);

			// the file contains invalid json, let's get rid of it
			try {
				Path sourcePath = data.toPath();
				Files.move(sourcePath, sourcePath.resolveSibling(String.format("%s-corrupt-%d.json", playerName, System.currentTimeMillis())));
				initializeCounterDataFile(data);
			} catch (IOException ex) {
				log.warn("Could not neutralize corrupted Thieving Stall Counter data", ex);
			}
		}
	}

	private void initializeCounterDataFile(File data) {
		try (Writer writer = new FileWriter(data)) {
			GSON.toJson(new ThievingStallCounterData(), ThievingStallCounterData.class, writer);
		} catch (IOException | JsonIOException e) {
			log.warn("Error while initializing Thieving Stall Counter data file", e);
		}
	}

	@Provides
    ThievingStallCounterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ThievingStallCounterConfig.class);
	}
}
