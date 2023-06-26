package net.runelite.client.plugins.spoontob.rooms.Verzik;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class GreenBallPanel extends OverlayPanel {
    private SpoonTobPlugin plugin;

    private SpoonTobConfig config;

    private Client client;

    private Verzik verzik;

    @Inject
    public GreenBallPanel(SpoonTobPlugin plugin, SpoonTobConfig config, Client client, Verzik verzik) {
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
        if (this.config.greenBouncePanel() != SpoonTobConfig.greenBouncePanelMode.OFF && this.verzik.isVerzikActive() && this.verzik.getVerzikPhase() == Verzik.Phase.PHASE3 && this.verzik.greenBallOut && this.verzik.getVerzikNPC() != null) {
            String leftText = "";
            String rightText = "";
            Color rightColor = Color.WHITE;
            if (this.config.greenBouncePanel() == SpoonTobConfig.greenBouncePanelMode.BOUNCES) {
                leftText = "Bounces:";
                rightText = Integer.toString(this.verzik.greenBallBounces);
                this.panelComponent.setPreferredSize(new Dimension(95, 24));
            } else if (this.config.greenBouncePanel() == SpoonTobConfig.greenBouncePanelMode.DAMAGE) {
                leftText = "Damage:";
                if (this.verzik.getVerzikNPC().getId() == 10852) {
                    rightText = Double.toString(99.0D - this.verzik.greenBallBounces * 0.25D * 99.0D);
                } else if (this.verzik.greenBallBounces == 0) {
                    rightText = Integer.toString(74);
                } else {
                    rightText = Double.toString(74.0D - this.verzik.greenBallBounces * 0.25D * 74.0D);
                }
                this.panelComponent.setPreferredSize(new Dimension(90, 24));
            } else {
                leftText = "Bounces(Dmg):";
                if (this.verzik.getVerzikNPC().getId() == 10852) {
                    rightText = "" + this.verzik.greenBallBounces + "(" + this.verzik.greenBallBounces + ")";
                } else if (this.verzik.greenBallBounces == 0) {
                    rightText = "" + this.verzik.greenBallBounces + "(74)";
                } else {
                    rightText = "" + this.verzik.greenBallBounces + "(" + this.verzik.greenBallBounces + ")";
                }
                this.panelComponent.setPreferredSize(new Dimension(130, 24));
            }
            if (this.verzik.greenBallBounces == 0 && this.verzik.getVerzikNPC().getId() == 10852) {
                rightColor = Color.RED;
                rightText = "Death";
            }
            this.panelComponent.getChildren().add(LineComponent.builder()
                    .left(leftText)
                    .rightColor(rightColor)
                    .right(rightText)
                    .build());
        }
        return super.render(graphics);
    }
}
