package net.runelite.client.plugins.spoontob.rooms.Xarpus;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.PanelComponent;
import org.apache.commons.lang3.tuple.Pair;

public class XarpusOverlay extends RoomOverlay {
    @Inject
    private Xarpus xarpus;

    @Inject
    private SpoonTobPlugin plugin;

    PanelComponent panelComponent = new PanelComponent();

    @Inject
    protected XarpusOverlay(SpoonTobConfig config) {
        super(config);
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.entryInstanceTimer() == SpoonTobConfig.instancerTimerMode.OVERHEAD && this.xarpus.isInstanceTimerRunning() && !this.xarpus.isExhumedSpawned() && this.xarpus
                .isInXarpusRegion()) {
            Player player = this.client.getLocalPlayer();
            if (player != null) {
                Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
                if (point != null)
                    renderTextLocation(graphics, String.valueOf(this.xarpus.getInstanceTimer()), Color.CYAN, point);
            }
        }
        if (this.xarpus.isXarpusActive()) {
            NPC boss = this.xarpus.getXarpusNPC();
            boolean showp2 = (this.config.xarpusTicks() && Xarpus.P2_IDS.contains(Integer.valueOf(boss.getId())));
            boolean p3exception = (this.xarpus.isHM() && this.xarpus.isXarpusStare() && this.xarpus.isP3Active());
            boolean showp3 = (this.config.xarpusTicks() && Xarpus.P3_IDS.contains(Integer.valueOf(boss.getId())) && !p3exception);
            if (showp2 || showp3) {
                int tick = this.xarpus.getXarpusTicksUntilAttack();
                String ticksLeftStr = String.valueOf(tick);
                Point canvasPoint = boss.getCanvasTextLocation(graphics, ticksLeftStr, 130);
                if (this.config.fontStyle()) {
                    renderTextLocation(graphics, ticksLeftStr, Color.WHITE, canvasPoint);
                } else {
                    renderResizeTextLocation(graphics, ticksLeftStr, 14, 1, Color.WHITE, canvasPoint);
                }
            }
            if (Xarpus.P1_IDS.contains(Integer.valueOf(boss.getId())) &&
                    !this.xarpus.getXarpusExhumeds().isEmpty()) {
                Collection<Pair<GroundObject, Integer>> exhumeds = this.xarpus.getXarpusExhumeds().values();
                exhumeds.forEach(p -> {
                    GroundObject o = (GroundObject)p.getLeft();
                    int ticks = ((Integer)p.getRight()).intValue();
                    String text = String.valueOf(ticks);
                    int maxSafeTicks = 8;
                    int minSafeTicks = 1;
                    if (this.xarpus.isHM())
                        maxSafeTicks = 6;
                    if (this.config.xarpusExhumed() == SpoonTobConfig.exhumedMode.TILE || this.config.xarpusExhumed() == SpoonTobConfig.exhumedMode.BOTH) {
                        Polygon poly = o.getCanvasTilePoly();
                        if (poly != null) {
                            Color color = new Color(0, 255, 0, 130);
                            if (this.config.exhumedStepOffWarning() == SpoonTobConfig.stepOffMode.TILE || this.config.exhumedStepOffWarning() == SpoonTobConfig.stepOffMode.BOTH)
                                if (ticks <= minSafeTicks || ticks >= maxSafeTicks) {
                                    color = new Color(0, 255, 0, 130);
                                } else {
                                    color = new Color(255, 0, 0, 130);
                                }
                            graphics.setColor(color);
                            graphics.setStroke(new BasicStroke(1.0F));
                            graphics.draw(poly);
                        }
                    }
                    if (this.config.xarpusExhumed() == SpoonTobConfig.exhumedMode.BOTH || this.config.xarpusExhumed() == SpoonTobConfig.exhumedMode.TICKS) {
                        Point textLocation = o.getCanvasTextLocation(graphics, text, 0);
                        if (textLocation != null) {
                            Color color = Color.WHITE;
                            if (this.config.exhumedStepOffWarning() == SpoonTobConfig.stepOffMode.TICKS || this.config.exhumedStepOffWarning() == SpoonTobConfig.stepOffMode.BOTH) {
                                color = Color.RED;
                                if (ticks <= minSafeTicks || ticks >= maxSafeTicks)
                                    color = Color.GREEN;
                            }
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, text, color, textLocation);
                            } else {
                                renderResizeTextLocation(graphics, text, 12, 1, color, textLocation);
                            }
                        }
                    }
                });
            }
            if (this.config.exhumedOnXarpus() && this.xarpus.isExhumedSpawned() && Xarpus.P1_IDS.contains(Integer.valueOf(this.xarpus.getXarpusNPC().getId())) && this.xarpus.getExhumedCounter() != null) {
                String xarpusText = (this.xarpus.getExhumedCounter().getCount() == 0) ? "NOW!" : String.valueOf(this.xarpus.getExhumedCounter().getCount());
                if (xarpusText.length() >= 1) {
                    Point canvasPoint = this.xarpus.getXarpusNPC().getCanvasTextLocation(graphics, xarpusText, 320);
                    if (canvasPoint != null && !this.xarpus.getXarpusNPC().isDead())
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, xarpusText, Color.ORANGE, canvasPoint);
                        } else {
                            renderResizeTextLocation(graphics, xarpusText, 14, 1, Color.ORANGE, canvasPoint);
                        }
                }
            }
        }
        return null;
    }
    protected void renderXarpusPolygon(Graphics2D graphics, @Nullable Shape polygon, @Nonnull Color color) {
        if (polygon != null) {
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), this.config.xarpusLosColor().getAlpha()));
            graphics.setStroke(new BasicStroke(2.0F));
            graphics.draw(polygon);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), this.config.xarpusLosFill()));
            graphics.fill(polygon);
        }
    }
}
