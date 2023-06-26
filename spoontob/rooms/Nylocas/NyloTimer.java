package net.runelite.client.plugins.spoontob.rooms.Nylocas;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

public class NyloTimer extends OverlayPanel {
    private final Client client;

    private final SpoonTobPlugin plugin;

    private final SpoonTobConfig config;

    private Nylocas nylo;

    @Inject
    public NyloTimer(Client client, SpoonTobPlugin plugin, SpoonTobConfig config, Nylocas nylo) {
        super((Plugin)plugin);
        this.client = client;
        this.nylo = nylo;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        getMenuEntries().add(new OverlayMenuEntry(MenuAction.RUNELITE_OVERLAY_CONFIG, "Configure", "Theatre xarpus overlay"));
    }

    public Dimension render(Graphics2D graphics) {
        this.panelComponent.getChildren().clear();
        if (this.config.entryInstanceTimer() == SpoonTobConfig.instancerTimerMode.OVERLAY && TheatreRegions.inRegion(this.client, TheatreRegions.NYLOCAS) && this.nylo
                .isInstanceTimerRunning()) {
            this.panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth("Tick:   ") + 10, 0));
            this.panelComponent.getChildren().add(LineComponent.builder()
                    .left("Tick: ")
                    .right(String.valueOf(this.nylo.getInstanceTimer()))
                    .build());
        }
        return super.render(graphics);
    }
}
