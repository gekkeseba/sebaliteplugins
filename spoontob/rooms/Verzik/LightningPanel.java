package net.runelite.client.plugins.spoontob.rooms.Verzik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ImageUtil;

public class LightningPanel extends OverlayPanel {
    private SpoonTobPlugin plugin;

    private SpoonTobConfig config;

    private Client client;

    private Verzik verzik;

    @Inject
    public LightningPanel(SpoonTobPlugin plugin, SpoonTobConfig config, Client client, Verzik verzik) {
        super((Plugin)plugin);
        this.plugin = plugin;
        this.config = config;
        this.client = client;
        this.verzik = verzik;
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
    }

    public Dimension render(Graphics2D graphics) {
        this.panelComponent.getChildren().clear();
        if (this.config.lightningInfobox() != SpoonTobConfig.lightningMode.OFF && (
                this.config.lightningInfobox() == SpoonTobConfig.lightningMode.INFOBOX || this.config.lightningInfobox() == SpoonTobConfig.lightningMode.BOTH) && this.verzik
                .isVerzikActive() && this.verzik.getVerzikPhase() == Verzik.Phase.PHASE2) {
            Color color = Color.WHITE;
            if (this.verzik.lightningAttacks > 0) {
                if (this.verzik.lightningAttacks == 1)
                    color = Color.RED;
                this.panelComponent.getChildren().add(TitleComponent.builder()
                        .color(color)
                        .text(Integer.toString(this.verzik.lightningAttacks))
                        .build());
            } else {
                BufferedImage img = ImageUtil.loadImageResource(SpoonTobPlugin.class, "Zap.png");
                ImageComponent imgComp = new ImageComponent(img);
                this.panelComponent.getChildren().add(imgComp);
            }
            this.panelComponent.setPreferredSize(new Dimension(24, 24));
        }
        return super.render(graphics);
    }
}
