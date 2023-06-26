package net.runelite.client.plugins.spoontob.rooms.Maiden;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import net.runelite.client.util.ColorUtil;

public class MaidenMaxHitToolTip extends Overlay {
    @Inject
    private Client client;

    @Inject
    private TooltipManager tooltipManager;

    @Inject
    private Maiden maiden;

    @Inject
    private SpoonTobConfig config;

    @Inject
    private MaidenMaxHitToolTip(Client client, TooltipManager tooltipManager, Maiden maiden, SpoonTobConfig config) {
        this.client = client;
        this.tooltipManager = tooltipManager;
        this.maiden = maiden;
        this.config = config;
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.maidenMaxHit() != null && !this.client.isMenuOpen() && this.maiden.isMaidenActive()) {
            NPC maidenNpc = this.maiden.getMaidenNPC();
            Model model = maidenNpc.getModel();
            LocalPoint localPoint = maidenNpc.getLocalLocation();
            if (model != null && localPoint != null) {
                Shape clickbox = Perspective.getClickbox(this.client, model, maidenNpc.getOrientation(), localPoint.getX(), localPoint.getY(), this.client.getPlane());
                if (clickbox != null && clickbox
                        .contains(this.client.getMouseCanvasPosition().getX(), this.client.getMouseCanvasPosition().getY())) {
                    int noPrayerMaxHit = (int)Math.floor(this.maiden.getMaxHit());
                    int prayerMaxHit = noPrayerMaxHit / 2;
                    int elyMaxHit = prayerMaxHit - (int)Math.floor(prayerMaxHit * 0.25D);
                    this.tooltipManager.add(new Tooltip(ColorUtil.wrapWithColorTag("No Prayer:", new Color(255, 109, 97)) + ColorUtil.wrapWithColorTag("No Prayer:", new Color(255, 109, 97)) + "</br>" +
                            ColorUtil.wrapWithColorTag(" +" + noPrayerMaxHit, new Color(-7278960)) +
                            ColorUtil.wrapWithColorTag("Prayer:", Color.ORANGE) + "</br>" + ColorUtil.wrapWithColorTag(" +" + prayerMaxHit, new Color(-7278960)) +
                            ColorUtil.wrapWithColorTag("Elysian:", Color.CYAN)));
                }
            }
        }
        return null;
    }
}
