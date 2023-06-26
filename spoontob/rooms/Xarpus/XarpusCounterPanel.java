package net.runelite.client.plugins.spoontob.rooms.Xarpus;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class XarpusCounterPanel extends OverlayPanel {
    private final Client client;

    private final SpoonTobPlugin plugin;

    private final SpoonTobConfig config;

    private Xarpus xarpus;

    @Inject
    public XarpusCounterPanel(Client client, SpoonTobPlugin plugin, SpoonTobConfig config, Xarpus xarpus) {
        super((Plugin)plugin);
        this.client = client;
        this.xarpus = xarpus;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.xarpus.isXarpusActive() && this.xarpus.isExhumedSpawned() && this.xarpus.getExhumedCounter() != null && Xarpus.P1_IDS.contains(Integer.valueOf(this.xarpus.getXarpusNPC().getId())) && this.config
                .xarpusExhumedInfo()) {
            if (this.config.fontStyle())
                graphics.setFont(new Font("SansSerif", 0, 11));
            String exhumeds = Integer.toString(this.xarpus.getExhumedCounter().getCount());
            String healed = Integer.toString(this.xarpus.healCount);
            this.panelComponent.getChildren().clear();
            String overlayTitle = "Exhume Counter";
            this.panelComponent.getChildren().add(TitleComponent.builder().text(overlayTitle).color(Color.GREEN).build());
            this.panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth(overlayTitle) + 30, 0));
            this.panelComponent.getChildren().add(LineComponent.builder().left("Exhumes: ").right(exhumeds).build());
            this.panelComponent.getChildren().add(LineComponent.builder().left("Healed: ").right(healed).build());
            return super.render(graphics);
        }
        return null;
    }
}
