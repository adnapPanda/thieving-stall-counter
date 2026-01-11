package com.thievingstallcounter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("ThievingStallCounterConfig")
public interface ThievingStallCounterConfig extends Config
{
	@ConfigItem(
			position = 0,
			keyName = "showStallsThieved",
			name = "Stalls Thieved",
			description = "Displays the number of stalls thieved"
	)
	default boolean showStallsThieved() {
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "showPetChance",
			name = "% Chance of having received pet",
			description = "Displays the percentage chance of having received at least one pet"
	)
	default boolean showPetChance() {
		return true;
	}
}
