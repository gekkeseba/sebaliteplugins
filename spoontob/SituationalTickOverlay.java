package net.runelite.client.plugins.spoontob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.OverlayUtil;

public class SituationalTickOverlay extends RoomOverlay {
    @Inject
    private Client client;

    @Inject
    private SpoonTobConfig config;

    @Inject
    private SpoonTobPlugin plugin;

    @Inject
    protected SituationalTickOverlay(SpoonTobConfig config) {
        super(config);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.plugin.enforceRegion() && this.config.situationalTicks()) {
            Player p = this.client.getLocalPlayer();
            if (p != null)
                if (isInBloatRegion(this.client)) {
                    Integer tick = this.plugin.situationalTicksList.get(p);
                    if (tick != null) {
                        Point canvasPoint = this.client.getLocalPlayer().getCanvasTextLocation(graphics, String.valueOf(tick), this.config.situationalTicksOffset());
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, String.valueOf(tick), (tick.intValue() == 1) ? Color.GREEN : Color.WHITE, canvasPoint);
                        } else {
                            renderSteroidsTextLocation(graphics, String.valueOf(tick), this.config.situationalTicksSize(), 1, (tick.intValue() == 1) ? Color.GREEN : Color.WHITE, canvasPoint);
                        }
                    }
                } else if (isInXarpRegion(this.client)) {
                    for (Player p2 : this.plugin.getSituationalTicksList().keySet()) {
                        int tick = ((Integer)this.plugin.getSituationalTicksList().get(p2)).intValue();
                        Point canvasPoint = p2.getCanvasTextLocation(graphics, String.valueOf(tick), this.config.situationalTicksOffset());
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, String.valueOf(tick), (tick == 1) ? Color.GREEN : Color.WHITE, canvasPoint);
                            continue;
                        }
                        renderSteroidsTextLocation(graphics, String.valueOf(tick), this.config.situationalTicksSize(), 1, (tick == 1) ? Color.GREEN : Color.WHITE, canvasPoint);
                    }
                }
        }
        return null;
    }

    private static boolean isInBloatRegion(Client client) {
        return (client.getMapRegions() != null && (client.getMapRegions()).length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> (s == 13125)));
    }

    private static boolean isInXarpRegion(Client client) {
        return (client.getMapRegions() != null && (client.getMapRegions()).length > 0 && Arrays.stream(client.getMapRegions()).anyMatch(s -> (s == 12612)));
    }

    protected void renderTextLocation(Graphics2D graphics, String txtString, Color fontColor, Point canvasPoint) {
        if (canvasPoint != null) {
            Point canvasCenterPoint = new Point(canvasPoint.getX(), canvasPoint.getY());
            Point canvasCenterPoint_shadow = new Point(canvasPoint.getX() + 1, canvasPoint.getY() + 1);
            OverlayUtil.renderTextLocation(graphics, canvasCenterPoint_shadow, txtString, Color.BLACK);
            OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, txtString, fontColor);
        }
    }
}
