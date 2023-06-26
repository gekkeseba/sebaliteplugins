package net.runelite.client.plugins.spoontob.rooms.Maiden;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.util.TheatrePerspective;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

public class MaidenOverlay extends RoomOverlay {
    @Inject
    private Maiden maiden;

    @Inject
    private SpoonTobPlugin plugin;

    @Inject
    private Client client;

    protected static final BiFunction<Integer, Integer, Color> rgbMod;

    private final ModelOutlineRenderer modelOutlineRenderer;

    @Inject
    protected MaidenOverlay(SpoonTobConfig config, ModelOutlineRenderer modelOutlineRenderer) {
        super(config);
        this.modelOutlineRenderer = modelOutlineRenderer;
    }

    static {
        rgbMod = ((max, current) -> new Color(255 * (max.intValue() - current.intValue()) / max.intValue(), 255 * current.intValue() / max.intValue(), 0));
    }

    public Dimension render(Graphics2D graphics) {
        if (this.maiden.isMaidenActive() && this.maiden.getMaidenNPC() != null) {
            if (this.config.fuckBluelite()) {
                LocalPoint lp = null;
                if (this.maiden != null)
                    lp = this.maiden.getMaidenNPC().getLocalLocation();
                if (lp != null) {
                    LocalPoint lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() - 960);
                    List<Polygon> polyList = new ArrayList<>();
                    Polygon poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    Polygon poly2 = TheatrePerspective.getLine(this.client, lp2, "westMiddle");
                    Polygon poly3 = TheatrePerspective.getLine(this.client, lp2, "swnw");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() - 800);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "nwne");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() - 640);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "swnw");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() - 480);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "kUp");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "kDown");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() - 192);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "B1");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "B2");
                    Polygon poly4 = TheatrePerspective.getLine(this.client, lp2, "B3");
                    Polygon poly5 = TheatrePerspective.getLine(this.client, lp2, "B4");
                    Polygon poly6 = TheatrePerspective.getLine(this.client, lp2, "B5");
                    Polygon poly7 = TheatrePerspective.getLine(this.client, lp2, "B6");
                    Polygon poly8 = TheatrePerspective.getLine(this.client, lp2, "B7");
                    Polygon poly9 = TheatrePerspective.getLine(this.client, lp2, "B8");
                    Polygon poly10 = TheatrePerspective.getLine(this.client, lp2, "B9");
                    Polygon poly11 = TheatrePerspective.getLine(this.client, lp2, "B10");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    polyList.add(poly4);
                    polyList.add(poly5);
                    polyList.add(poly6);
                    polyList.add(poly7);
                    polyList.add(poly8);
                    polyList.add(poly9);
                    polyList.add(poly10);
                    polyList.add(poly11);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() - 32);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    polyList.add(poly);
                    polyList.add(poly2);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() + 128);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "nwne");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() + 288);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "swnw");
                    poly4 = TheatrePerspective.getLine(this.client, lp2, "E");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    polyList.add(poly4);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() + 448);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    polyList.add(poly);
                    polyList.add(poly2);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() + 608);
                    poly = TheatrePerspective.getLine(this.client, lp2, "I");
                    polyList.add(poly);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() + 768);
                    poly = TheatrePerspective.getLine(this.client, lp2, "I");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "swnw");
                    polyList.add(poly);
                    polyList.add(poly2);
                    lp2 = new LocalPoint(lp.getX() + 1280, lp.getY() + 928);
                    poly = TheatrePerspective.getLine(this.client, lp2, "swse");
                    poly2 = TheatrePerspective.getLine(this.client, lp2, "sene");
                    poly3 = TheatrePerspective.getLine(this.client, lp2, "swnw");
                    poly4 = TheatrePerspective.getLine(this.client, lp2, "E");
                    polyList.add(poly);
                    polyList.add(poly2);
                    polyList.add(poly3);
                    polyList.add(poly4);
                    graphics.setColor(this.maiden.c);
                    graphics.setStroke(new BasicStroke(3.0F));
                    for (Polygon p : polyList)
                        graphics.draw(p);
                }
            }
            if (this.config.maidenBlood() != SpoonTobConfig.maidenBloodSplatMode.OFF)
                for (int i = 0; i < this.maiden.getMaidenBloodSplatters().size(); i++) {
                    WorldPoint wp = this.maiden.getMaidenBloodSplatters().get(i);
                    Color color = this.config.bloodTossColour();
                    if (this.config.maidenBlood() == SpoonTobConfig.maidenBloodSplatMode.RAVEST) {
                        color = this.plugin.raveUtils.getColor(i * 50, false);
                    } else if (this.config.maidenBlood() == SpoonTobConfig.maidenBloodSplatMode.RAVE) {
                        color = this.plugin.raveUtils.getColor(this.maiden.getMaidenBloodSplatters().hashCode(), true);
                    }
                    drawTile(graphics, wp, color, 1, this.config.bloodTossColour().getAlpha(), this.config.bloodTossFill());
                }
            if (this.config.bloodTossTicks())
                for (int i = 0; i < this.maiden.getMaidenBloodSplatterProj().size(); i++) {
                    String text = String.valueOf(((MaidenBloodSplatInfo)this.maiden.getMaidenBloodSplatterProj().get(i)).projectile.getRemainingCycles() / 30);
                    Point canvasPoint = Perspective.getCanvasTextLocation(this.client, graphics, ((MaidenBloodSplatInfo)this.maiden.getMaidenBloodSplatterProj().get(i)).lp, text, 0);
                    if (canvasPoint != null) {
                        Color col = Color.WHITE;
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, text, col, canvasPoint);
                        } else {
                            renderSteroidsTextLocation(graphics, text, 14, 1, col, canvasPoint);
                        }
                    }
                }
            if (this.config.maidenSpawns()) {
                for (WorldPoint point : this.maiden.getMaidenBloodSpawnLocations())
                    drawTile(graphics, point, this.config.bloodSpawnsColor(), 2, 180, 20);
                if (this.config.maidenSpawnsTrail())
                    for (WorldPoint point : this.maiden.getMaidenBloodSpawnTrailingLocations())
                        drawTile(graphics, point, this.config.bloodSpawnsColor(), 1, 120, 10);
            }
            if (this.config.maidenTickCounter() && !this.maiden.getMaidenNPC().isDead()) {
                String text = String.valueOf(this.maiden.ticksUntilAttack);
                Point canvasPoint = this.maiden.getMaidenNPC().getCanvasTextLocation(graphics, text, 30);
                if (canvasPoint != null) {
                    Color col = this.maiden.maidenSpecialWarningColor();
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, text, col, canvasPoint);
                    } else {
                        renderResizeTextLocation(graphics, text, 14, 1, col, canvasPoint);
                    }
                }
            }
            if ((this.config.maidenFreezeTimer() == SpoonTobConfig.maidenFreezeTimerMode.TILE || this.config.maidenScuffedCrab()) && this.maiden.maidenCrabInfoList.size() > 0) {
                int maidenX = 0;
                if (this.maiden.getMaidenNPC() != null) {
                    WorldPoint maidenWp = this.maiden.getMaidenNPC().getWorldLocation();
                    maidenX = maidenWp.getX();
                    NPCComposition maidenModel = this.maiden.getMaidenNPC().getTransformedComposition();
                    if (maidenModel != null)
                        maidenX += maidenModel.getSize();
                }
                for (MaidenCrabInfo mci : this.maiden.maidenCrabInfoList) {
                    if (!mci.crab.isDead()) {
                        int healerX = mci.crab.getWorldLocation().getX();
                        int deltaX = Math.max(0, healerX - maidenX);
                        if (deltaX > 0) {
                            NPCComposition npcComposition = mci.crab.getTransformedComposition();
                            if (npcComposition != null) {
                                int size = npcComposition.getSize();
                                LocalPoint lp = mci.crab.getLocalLocation();
                                Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                                if (mci.frozenTicks != -1) {
                                    renderPoly(graphics, rgbMod.apply(Integer.valueOf(32), Integer.valueOf(mci.frozenTicks)), tilePoly);
                                    continue;
                                }
                                if (this.config.maidenScuffedCrab() && mci.scuffed && this.maiden.crabTicksSinceSpawn > 0)
                                    this.modelOutlineRenderer.drawOutline(mci.crab, 2, this.config.maidenScuffedCrabColor(), 4);
                            }
                        }
                    }
                }
            }
            if ((this.config.showMaidenCrabsDistance() || this.config.showMaidenCrabHp() || this.config.maidenFreezeTimer() == SpoonTobConfig.maidenFreezeTimerMode.TICKS) && this.maiden.maidenCrabInfoList
                    .size() > 0)
                renderCrabInfo(graphics);
            if (this.config.bloodSpawnFreezeTimer() && this.maiden.frozenBloodSpawns.size() > 0)
                this.maiden.frozenBloodSpawns.forEach((npc, ticks) -> {
                    if (ticks.intValue() >= 0) {
                        String text = String.valueOf(ticks);
                        Point canvasPoint = npc.getCanvasTextLocation(graphics, text, 30);
                        if (canvasPoint != null) {
                            Color col = Color.WHITE;
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, text, col, canvasPoint);
                            } else {
                                renderSteroidsTextLocation(graphics, text, 14, 1, col, canvasPoint);
                            }
                        }
                    }
                });
        }
        return null;
    }

    private void renderCrabInfo(Graphics2D graphics) {
        ArrayList<NPC> prevCrabs = new ArrayList<>();
        for (MaidenCrabInfo mci : this.maiden.maidenCrabInfoList) {
            String text = "";
            String distanceLine = "";
            Color distanceColor = this.config.distanceColor();
            Color color = Color.GREEN;
            if (mci.hpRatio != 0) {
                Point drawPoint;
                if (this.config.showMaidenCrabHp()) {
                    double crabHealthPcent = mci.hpRatio / mci.hpScale * 100.0D;
                    if (this.config.oldHpThreshold()) {
                        color = this.plugin.oldHitpointsColor(crabHealthPcent);
                    } else {
                        color = this.plugin.calculateHitpointsColor(crabHealthPcent);
                    }
                    String crabHp = String.valueOf(crabHealthPcent);
                    text = crabHp.substring(0, crabHp.indexOf(".")) + "%";
                }
                if (this.config.maidenFreezeTimer() == SpoonTobConfig.maidenFreezeTimerMode.TICKS && mci.frozenTicks >= 0)
                    if (!text.equals("")) {
                        text = text + " : " + text;
                    } else {
                        text = String.valueOf(mci.frozenTicks);
                    }
                if (this.config.showMaidenCrabsDistance()) {
                    WorldPoint maidenWp = this.maiden.getMaidenNPC().getWorldLocation();
                    int maidenX = maidenWp.getX();
                    NPCComposition maidenModel = this.maiden.getMaidenNPC().getTransformedComposition();
                    if (maidenModel != null)
                        maidenX += maidenModel.getSize();
                    WorldPoint healerWp = mci.crab.getWorldLocation();
                    int healerX = healerWp.getX();
                    int deltaX = Math.max(0, healerX - maidenX);
                    if (this.config.singleLineDistance()) {
                        if (mci.frozenTicks == -1)
                            if (!text.equals("")) {
                                text = text + " : " + text;
                            } else {
                                color = Color.WHITE;
                                text = Integer.toString(deltaX);
                            }
                    } else if (this.config.showFrozenDistance() || mci.frozenTicks == -1) {
                        distanceLine = Integer.toString(deltaX);
                    }
                }
                int offsetTimes = 0;
                NPC firstFreeze = null;
                for (NPC crab : prevCrabs) {
                    LocalPoint lp = crab.getLocalLocation();
                    if (lp.getX() == mci.crab.getLocalLocation().getX() && lp.getY() == mci.crab.getLocalLocation().getY()) {
                        offsetTimes++;
                        if (firstFreeze == null)
                            firstFreeze = crab;
                    }
                }
                if (offsetTimes != 0) {
                    drawPoint = firstFreeze.getCanvasTextLocation(graphics, text, 0);
                    if (drawPoint != null) {
                        int x = drawPoint.getX();
                        int y = drawPoint.getY() - 15 * offsetTimes;
                        drawPoint = new Point(x, y);
                    }
                } else {
                    drawPoint = mci.crab.getCanvasTextLocation(graphics, text, 0);
                }
                if (drawPoint != null)
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, text, color, drawPoint);
                        if (!distanceLine.equals("")) {
                            if (text.contains(":")) {
                                drawPoint = new Point(drawPoint.getX() + 15, drawPoint.getY() - 10);
                            } else {
                                drawPoint = new Point(drawPoint.getX() + 5, drawPoint.getY() - 10);
                            }
                            renderTextLocation(graphics, distanceLine, distanceColor, drawPoint);
                        }
                    } else {
                        renderResizeTextLocation(graphics, text, 11, 1, color, drawPoint);
                        if (!distanceLine.equals("")) {
                            if (text.contains(":")) {
                                drawPoint = new Point(drawPoint.getX() + 15, drawPoint.getY() - 10);
                            } else {
                                drawPoint = new Point(drawPoint.getX() + 5, drawPoint.getY() - 10);
                            }
                            renderResizeTextLocation(graphics, distanceLine, 11, 1, distanceColor, drawPoint);
                        }
                    }
                prevCrabs.add(mci.crab);
            }
        }
    }
}
