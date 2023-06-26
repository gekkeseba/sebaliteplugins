package net.runelite.client.plugins.spoontob.rooms.Nylocas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.OverlayUtil;

public class NylocasOverlay extends RoomOverlay {
    @Inject
    private Nylocas nylocas;

    @Inject
    private SpoonTobPlugin plugin;

    @Inject
    protected NylocasOverlay(SpoonTobConfig config) {
        super(config);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.nylocas.isInNyloRegion() && this.client.getLocalPlayer() != null) {
            Player player = this.client.getLocalPlayer();
            Point point = player.getCanvasTextLocation(graphics, "#", player.getLogicalHeight() + 60);
            if (this.nylocas.isInstanceTimerRunning() && this.config.entryInstanceTimer() == SpoonTobConfig.instancerTimerMode.OVERHEAD && point != null)
                renderTextLocation(graphics, String.valueOf(this.nylocas.getInstanceTimer()), Color.CYAN, point);
            if (this.config.showPhaseChange() != SpoonTobConfig.nyloBossPhaseChange.OFF && this.nylocas.getBossChangeTicks() > 0)
                drawNylocas(graphics);
            if (this.nylocas.isNyloActive()) {
                if (this.config.nyloPillars()) {
                    HashMap<NPC, Integer> npcMap = this.nylocas.getNylocasPillars();
                    for (NPC npc : npcMap.keySet()) {
                        Color c;
                        int health = ((Integer)npcMap.get(npc)).intValue();
                        String healthStr = "" + health + "%";
                        WorldPoint p = npc.getWorldLocation();
                        LocalPoint lp = LocalPoint.fromWorld(this.client, p.getX() + 1, p.getY() + 1);
                        if (this.config.oldHpThreshold()) {
                            c = this.plugin.oldHitpointsColor(health);
                        } else {
                            c = this.plugin.calculateHitpointsColor(health);
                        }
                        if (lp != null) {
                            Point canvasPoint = Perspective.localToCanvas(this.client, lp, this.client.getPlane(), 65);
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, healthStr, c, canvasPoint);
                                continue;
                            }
                            renderResizeTextLocation(graphics, healthStr, 13, 1, c, canvasPoint);
                        }
                    }
                }
                if (this.config.showNylocasExplosions() != SpoonTobConfig.ExplosionWarning.OFF || this.config.getHighlightMageNylo() || this.config.getHighlightMeleeNylo() || this.config
                        .getHighlightRangeNylo() || this.config.nyloAggressiveOverlay() != SpoonTobConfig.aggroStyle.OFF) {
                    int meleeIndex = 0;
                    int rangeIndex = 0;
                    int mageIndex = 0;
                    for (NyloInfo ni : this.nylocas.nylocasNpcs) {
                        NPC npc = ni.nylo;
                        String name = npc.getName();
                        LocalPoint lp = npc.getLocalLocation();
                        if (ni.alive) {
                            if (this.nylocas.getAggressiveNylocas().contains(npc) && lp != null)
                                if (this.config.nyloAggressiveOverlay() == SpoonTobConfig.aggroStyle.TILE) {
                                    Polygon poly = getCanvasTileAreaPoly(this.client, lp, npc.getComposition().getSize(), -25);
                                    renderPoly(graphics, Color.RED, poly, this.config.nyloTileWidth());
                                } else if (this.config.nyloAggressiveOverlay() == SpoonTobConfig.aggroStyle.HULL) {
                                    Shape objectClickbox = npc.getConvexHull();
                                    if (objectClickbox != null) {
                                        Color color = Color.RED;
                                        graphics.setColor(color);
                                        graphics.setStroke(new BasicStroke((float)this.config.nyloTileWidth()));
                                        graphics.draw(objectClickbox);
                                        graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
                                        graphics.fill(objectClickbox);
                                    }
                                }
                            int ticksLeft = ni.ticks;
                            if (ticksLeft > -1 && ticksLeft <= this.config.nyloExplosionDisplayTicks()) {
                                int ticksAlive = ticksLeft;
                                if (this.config.nyloTimeAliveCountStyle() == SpoonTobConfig.nylotimealive.COUNTUP)
                                    ticksAlive = 52 - ticksLeft;
                                Point textLocation = npc.getCanvasTextLocation(graphics, String.valueOf(ticksAlive), 60);
                                if ((this.config.showNylocasExplosions() == SpoonTobConfig.ExplosionWarning.BOTH || this.config.showNylocasExplosions() == SpoonTobConfig.ExplosionWarning.TILE) && ticksLeft <= 6 && lp != null && npc
                                        .getComposition() != null)
                                    if (this.config.nyloExplosionType() == SpoonTobConfig.nyloExplosionType.TILE) {
                                        renderPoly(graphics, Color.YELLOW, getCanvasTileAreaPoly(this.client, lp, npc.getComposition().getSize(), -15), this.config.nyloTileWidth());
                                    } else {
                                        renderPoly(graphics, Color.YELLOW, getCanvasTileAreaPoly(this.client, lp, npc.getComposition().getSize() + 4, 0), this.config.nyloTileWidth());
                                    }
                                if (textLocation != null && (
                                        this.config.showNylocasExplosions() == SpoonTobConfig.ExplosionWarning.BOTH || this.config.showNylocasExplosions() == SpoonTobConfig.ExplosionWarning.TICKS))
                                    if ((ticksAlive >= 44 && this.config.nyloTimeAliveCountStyle() == SpoonTobConfig.nylotimealive.COUNTUP) || (ticksAlive <= 8 && this.config
                                            .nyloTimeAliveCountStyle() == SpoonTobConfig.nylotimealive.COUNTDOWN)) {
                                        if (this.config.fontStyle()) {
                                            renderTextLocation(graphics, String.valueOf(ticksAlive), Color.RED, textLocation);
                                        } else {
                                            renderSteroidsTextLocation(graphics, String.valueOf(ticksAlive), 13, 1, Color.RED, textLocation);
                                        }
                                    } else if (this.config.fontStyle()) {
                                        renderTextLocation(graphics, String.valueOf(ticksAlive), Color.WHITE, textLocation);
                                    } else {
                                        renderSteroidsTextLocation(graphics, String.valueOf(ticksAlive), 13, 1, Color.WHITE, textLocation);
                                    }
                            }
                            Color nyloColor = Color.WHITE;
                            if (name != null && lp != null) {
                                if (this.config.getHighlightMeleeNylo() && name.equals("Nylocas Ischyros")) {
                                    if (this.config.raveNylo()) {
                                        nyloColor = this.nylocas.meleeNyloRaveColors.get(meleeIndex);
                                        meleeIndex++;
                                    } else {
                                        nyloColor = new Color(255, 188, 188);
                                    }
                                } else if (this.config.getHighlightRangeNylo() && name.equals("Nylocas Toxobolos")) {
                                    if (this.config.raveNylo()) {
                                        nyloColor = this.nylocas.rangeNyloRaveColors.get(rangeIndex);
                                        rangeIndex++;
                                    } else {
                                        nyloColor = Color.GREEN;
                                    }
                                } else if (this.config.getHighlightMageNylo() && name.equals("Nylocas Hagios")) {
                                    if (this.config.raveNylo()) {
                                        nyloColor = this.nylocas.mageNyloRaveColors.get(mageIndex);
                                        mageIndex++;
                                    } else {
                                        nyloColor = Color.CYAN;
                                    }
                                }
                                if (nyloColor != Color.WHITE)
                                    renderPoly(graphics, nyloColor, Perspective.getCanvasTileAreaPoly(this.client, lp, npc.getComposition().getSize()), this.config.nyloTileWidth());
                            }
                        }
                    }
                }
                if ((this.config.waveSpawnTimer() == SpoonTobConfig.waveSpawnTimerMode.OVERLAY || this.config.waveSpawnTimer() == SpoonTobConfig.waveSpawnTimerMode.BOTH) && this.client
                        .getLocalPlayer() != null && this.nylocas.nyloWave < 31 && this.nylocas.waveSpawnTicks > -1) {
                    String text = String.valueOf(this.nylocas.waveSpawnTicks);
                    LocalPoint eastLp = LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(this.client.getLocalPlayer().getWorldLocation().getRegionID(), 42, 25, this.client.getLocalPlayer().getWorldLocation().getPlane()));
                    LocalPoint westLp = LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(this.client.getLocalPlayer().getWorldLocation().getRegionID(), 5, 25, this.client.getLocalPlayer().getWorldLocation().getPlane()));
                    LocalPoint southLp = LocalPoint.fromWorld(this.client, WorldPoint.fromRegion(this.client.getLocalPlayer().getWorldLocation().getRegionID(), 24, 6, this.client.getLocalPlayer().getWorldLocation().getPlane()));
                    Color color = this.config.waveSpawnTimerColor();
                    if (this.nylocas.stalledWave)
                        color = Color.RED;
                    if (this.config.fontStyle()) {
                        if (eastLp != null)
                            renderTextLocation(graphics, text, color, Perspective.getCanvasTextLocation(this.client, graphics, eastLp, text, 0));
                        if (westLp != null)
                            renderTextLocation(graphics, text, color, Perspective.getCanvasTextLocation(this.client, graphics, westLp, text, 0));
                        if (southLp != null)
                            renderTextLocation(graphics, text, color, Perspective.getCanvasTextLocation(this.client, graphics, southLp, text, 0));
                    } else {
                        if (eastLp != null)
                            renderResizeTextLocation(graphics, text, 14, 1, color, Perspective.getCanvasTextLocation(this.client, graphics, eastLp, text, 0));
                        if (westLp != null)
                            renderResizeTextLocation(graphics, text, 14, 1, color, Perspective.getCanvasTextLocation(this.client, graphics, westLp, text, 0));
                        if (southLp != null)
                            renderResizeTextLocation(graphics, text, 14, 1, color, Perspective.getCanvasTextLocation(this.client, graphics, southLp, text, 0));
                    }
                }
            }
            if (this.config.showBigSplits())
                this.nylocas.getSplitsMap().forEach((npc, ticks) -> {
                    Polygon poly = Perspective.getCanvasTileAreaPoly(this.client, npc.getLocalLocation(), 2);
                    if (poly != null)
                        renderPolygon(graphics, poly, this.config.bigsColor());
                    Point textLocation = Perspective.getCanvasTextLocation(this.client, graphics, npc.getLocalLocation(), "#", 0);
                    if (textLocation != null)
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, Integer.toString(ticks.intValue()), Color.WHITE, textLocation);
                        } else {
                            renderBigSplitsTextLocation(graphics, Integer.toString(ticks.intValue()), textLocation);
                        }
                });
        }
        return null;
    }

    public void drawNylocas(Graphics2D graphics) {
        NPC npc = null;
        if (this.nylocas.minibossAlive && this.nylocas.nyloMiniboss != null && this.config.showPhaseChange() == SpoonTobConfig.nyloBossPhaseChange.BOTH) {
            npc = this.nylocas.nyloMiniboss;
        } else if (this.nylocas.getNylocasBoss() != null) {
            npc = this.nylocas.getNylocasBoss();
        }
        if (npc != null) {
            LocalPoint lp = npc.getLocalLocation();
            if (lp != null) {
                String str = Integer.toString(this.nylocas.getBossChangeTicks());
                Point loc = Perspective.getCanvasTextLocation(this.client, graphics, lp, str, 0);
                if (loc != null)
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, str, Color.WHITE, loc);
                    } else {
                        renderResizeTextLocation(graphics, str, 14, 1, Color.WHITE, loc);
                    }
            }
        }
    }

    protected void renderPolygon(Graphics2D graphics, @Nullable Shape polygon, @Nonnull Color color) {
        if (polygon != null) {
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(2.0F));
            graphics.draw(polygon);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
            graphics.fill(polygon);
        }
    }

    protected void renderBigSplitsTextLocation(Graphics2D graphics, String txtString, Point canvasPoint) {
        graphics.setFont(new Font(FontManager.getRunescapeSmallFont().toString(), 1, 13));
        if (canvasPoint != null) {
            Point canvasCenterPoint = new Point(canvasPoint.getX(), canvasPoint.getY());
            Point canvasCenterPointShadow = new Point(canvasPoint.getX() + 1, canvasPoint.getY() + 1);
            OverlayUtil.renderTextLocation(graphics, canvasCenterPointShadow, txtString, Color.BLACK);
            OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, txtString, Color.WHITE);
        }
    }
}
