package net.runelite.client.plugins.spoontob.rooms.Bloat;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.rooms.Bloat.stomp.BloatSafespot;
import net.runelite.client.plugins.spoontob.rooms.Bloat.stomp.SSLine;

public class BloatOverlay extends RoomOverlay {
    @Inject
    private Bloat bloat;

    @Inject
    private SpoonTobPlugin plugin;

    @Inject
    protected BloatOverlay(SpoonTobConfig config) {
        super(config);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.bloat.isBloatActive()) {
            if (this.config.bloatIndicator() != SpoonTobConfig.BloatIndicatorMode.OFF)
                if (this.config.bloatIndicator() == SpoonTobConfig.BloatIndicatorMode.TILE) {
                    renderNpcPoly(graphics, this.bloat.getBloatStateColor(), this.bloat.getBloatTilePoly(), 3.0D, this.bloat.getBloatStateColor().getAlpha());
                } else if (this.config.bloatIndicator() == SpoonTobConfig.BloatIndicatorMode.TRUE_LOCATION) {
                    renderNpcTLOverlay(graphics, this.bloat.getBloatNPC(), this.bloat.getBloatStateColor(), 3, this.bloat.getBloatStateColor().getAlpha(), 0);
                }
            if (this.config.showBloatHands() != SpoonTobConfig.bloatHandsMode.OFF || this.config.bloatHandsTicks()) {
                int index = 0;
                Color color = this.config.bloatHandColor();
                for (WorldPoint point : this.bloat.getBloathands().keySet()) {
                    if (this.config.showBloatHands() == SpoonTobConfig.bloatHandsMode.RAVE) {
                        color = this.plugin.raveUtils.getColor(this.bloat.getBloathands().hashCode(), true);
                    } else if (this.config.showBloatHands() == SpoonTobConfig.bloatHandsMode.RAVEST) {
                        color = this.plugin.raveUtils.getColor(index * 50, false);
                    }
                    drawTile(graphics, point, color, 1, this.config.bloatHandColor().getAlpha(), this.config.bloatColorFill());
                    if (this.config.bloatHandsTicks() && this.bloat.handsFalling) {
                        String text = String.valueOf(this.bloat.handTicks);
                        LocalPoint lp = LocalPoint.fromWorld(this.client, point);
                        if (lp != null) {
                            Point p = Perspective.getCanvasTextLocation(this.client, graphics, lp, text, 0);
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, text, Color.WHITE, p);
                            } else {
                                renderSteroidsTextLocation(graphics, text, 12, 1, Color.WHITE, p);
                            }
                        }
                    }
                    index++;
                }
            }
            if (this.bloat.bloatVar == 1) {
                if (this.config.bloatUpTimer() && this.bloat != null) {
                    Point canvasPoint = this.bloat.getBloatNPC().getCanvasTextLocation(graphics, String.valueOf(this.bloat.getBloatUpTimer()), 60);
                    if (this.bloat.getBloatState() != 1 && this.bloat.getBloatState() != 4) {
                        String str = String.valueOf(33 - this.bloat.getBloatDownCount());
                        if (this.bloat.getBloatDownCount() >= 26) {
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, str, Color.RED, canvasPoint);
                            } else {
                                renderResizeTextLocation(graphics, str, 15, 1, Color.RED, canvasPoint);
                            }
                        } else if (this.config.fontStyle()) {
                            renderTextLocation(graphics, str, Color.WHITE, canvasPoint);
                        } else {
                            renderResizeTextLocation(graphics, str, 15, 1, Color.WHITE, canvasPoint);
                        }
                    } else {
                        Color col = (this.bloat.getBloatUpTimer() > 37) ? Color.RED : Color.WHITE;
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, String.valueOf(this.bloat.getBloatUpTimer()), col, canvasPoint);
                        } else {
                            renderResizeTextLocation(graphics, String.valueOf(this.bloat.getBloatUpTimer()), 15, 1, col, canvasPoint);
                        }
                    }
                }
            } else if (this.bloat.bloatVar == 0 &&
                    this.config.bloatEntryTimer() && this.bloat != null) {
                Point canvasPoint = this.bloat.getBloatNPC().getCanvasTextLocation(graphics, String.valueOf(this.bloat.getBloatUpTimer()), 60);
                Color col = Color.WHITE;
                if (this.config.fontStyle()) {
                    renderTextLocation(graphics, String.valueOf(this.bloat.getBloatUpTimer()), col, canvasPoint);
                } else {
                    renderResizeTextLocation(graphics, String.valueOf(this.bloat.getBloatUpTimer()), 15, 1, col, canvasPoint);
                }
            }
            if (this.bloat != null && (
                    this.bloat.getBloatState() == 2 || this.bloat.getBloatState() == 3) && this.config.bloatStompMode() != SpoonTobConfig.bloatStompMode.OFF)
                renderStompSafespots(graphics);
        }
        return null;
    }

    private void renderStompSafespots(Graphics2D graphics) {
        if (this.bloat.getBloatDown() != null) {
            BloatSafespot safespot = this.bloat.getBloatDown().getBloatSafespot();
            safespot.getSafespotLines().forEach(line -> {
                Color color = this.config.bloatStompColor();
                if (this.config.bloatStompMode() == SpoonTobConfig.bloatStompMode.RAVE)
                    color = this.plugin.raveUtils.getColor(line.hashCode(), true);
                drawLine(graphics, line, color, this.config.bloatStompWidth());
            });
        }
    }

    protected void drawLine(Graphics2D graphics, @Nullable SSLine safespotLine, @Nonnull Color lineColor, int lineStroke) {
        if (safespotLine != null) {
            Point pointA = safespotLine.getTranslatedPointA(this.client);
            Point pointB = safespotLine.getTranslatedPointB(this.client);
            if (pointA != null && pointB != null) {
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setStroke(new BasicStroke(lineStroke));
                graphics.setColor(lineColor);
                graphics.drawLine(pointA.getX(), pointA.getY(), pointB.getX(), pointB.getY());
            }
        }
    }
}
