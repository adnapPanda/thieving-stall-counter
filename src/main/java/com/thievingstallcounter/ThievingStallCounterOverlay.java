package com.thievingstallcounter;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;
import java.util.List;

public class ThievingStallCounterOverlay extends OverlayPanel {
    private Client client;
    private ThievingStallCounterPlugin plugin;
    private ThievingStallCounterConfig config;

    @Inject
    ThievingStallCounterOverlay(ThievingStallCounterPlugin plugin, Client client, ThievingStallCounterConfig config) {
        super(plugin);
        this.client = client;
        this.plugin = plugin;
        this.config = config;

        setPosition(OverlayPosition.TOP_LEFT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (plugin.thievedStallsRecently) {
            List<LayoutableRenderableEntity> elems = panelComponent.getChildren();
            elems.clear();
            panelComponent.setPreferredSize(new Dimension(200, 100));
            if (config.showStallsThieved())
                elems.add(LineComponent.builder()
                        .left("Total Stalls Thieved:")
                        .right(String.format("%d", plugin.stallsThieved))
                        .build());

            if (config.showPetChance())
                elems.add(LineComponent.builder()
                        .left("% Chance of pet:")
                        .right(String.format("%f", plugin.petDryChance*100))
                        .build());
        }
        return super.render(graphics);
    }
}
