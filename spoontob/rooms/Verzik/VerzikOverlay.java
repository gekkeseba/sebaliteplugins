package net.runelite.client.plugins.spoontob.rooms.Verzik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.text.DecimalFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.GameObject;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;

public class VerzikOverlay extends RoomOverlay {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##0");

    @Inject
    private Verzik verzik;

    @Inject
    private SpoonTobPlugin plugin;

    @Inject
    protected VerzikOverlay(SpoonTobConfig config) {
        super(config);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.verzik.isVerzikActive() && this.verzik.getVerzikNPC() != null) {
            int id = this.verzik.getVerzikNPC().getId();
            if (Verzik.VERZIK_ACTIVE_IDS.contains(id)) {
                if (this.config.displayGreenBall() != SpoonTobConfig.greenBallMode.OFF || this.config.displayGreenBallTicks())
                    displayProjectiles(graphics);
                if (this.config.purpleAoe())
                    displayPurpleCrabAOE(graphics, this.verzik.getVerzikNPC());
            }
            if (this.config.verzikMelee() != SpoonTobConfig.meleeTileMode.OFF) {
                int size = 1;
                NPCComposition composition = this.verzik.getVerzikNPC().getTransformedComposition();
                if (composition != null)
                    size = composition.getSize();
                LocalPoint lp = LocalPoint.fromWorld(this.client, this.verzik.getVerzikNPC().getWorldLocation());
                if (lp != null) {
                    Polygon tilePoly = getCanvasTileAreaPoly(this.client, lp, size, false);
                    if (tilePoly != null && this.verzik.verzikPhase == Verzik.Phase.PHASE3)
                        if (this.config.verzikMelee() == SpoonTobConfig.meleeTileMode.TANK_NOTIFIER) {
                            if (this.verzik.getVerzikNPC().getInteracting() == this.client.getLocalPlayer()) {
                                renderPoly(graphics, this.config.p3AggroColor(), tilePoly);
                            } else {
                                renderPoly(graphics, this.config.verzikMeleeColor(), tilePoly);
                            }
                        } else {
                            renderPoly(graphics, this.config.verzikMeleeColor(), tilePoly);
                        }
                }
            }
            if (this.config.verzikTankTarget() && this.verzik.verzikPhase == Verzik.Phase.PHASE3 &&
                    this.verzik.getVerzikNPC() != null && this.verzik.getVerzikNPC().getInteracting() != null) {
                Actor actor = this.verzik.getVerzikNPC().getInteracting();
                Polygon tilePoly = getCanvasTileAreaPoly(this.client, actor.getLocalLocation(), 1, false);
                if (tilePoly != null)
                    renderPoly(graphics, this.config.p3AggroColor(), tilePoly);
            }
            if (this.config.showVerzikNados() != SpoonTobConfig.nadoMode.OFF && this.verzik.verzikPhase == Verzik.Phase.PHASE3)
                if (this.config.showVerzikNados() == SpoonTobConfig.nadoMode.ALL) {
                    for (NPC nado : this.client.getNpcs()) {
                        if (Verzik.NADO_IDS.contains(nado.getId())) {
                            if (this.config.showVerzikNadoStyle() == SpoonTobConfig.nadoStyle.TILE) {
                                renderNpcOverlay(graphics, nado, this.config.showVerzikNadoColor(), 2, this.config.showVerzikNadoColor().getAlpha(), this.config.verzikNadoOpacity());
                                continue;
                            }
                            if (this.config.showVerzikNadoStyle() == SpoonTobConfig.nadoStyle.TRUE_LOCATION)
                                renderNpcTLOverlay(graphics, nado, this.config.showVerzikNadoColor(), 2, this.config.showVerzikNadoColor().getAlpha(), this.config.verzikNadoOpacity());
                        }
                    }
                } else if (this.config.showVerzikNados() == SpoonTobConfig.nadoMode.PERSONAL && this.verzik.getPersonalNado() != null) {
                    if (this.config.showVerzikNadoStyle() == SpoonTobConfig.nadoStyle.TILE) {
                        renderNpcOverlay(graphics, this.verzik.getPersonalNado(), this.config.showVerzikNadoColor(), 2, this.config.showVerzikNadoColor().getAlpha(), this.config.verzikNadoOpacity());
                    } else if (this.config.showVerzikNadoStyle() == SpoonTobConfig.nadoStyle.TRUE_LOCATION) {
                        renderNpcTLOverlay(graphics, this.verzik.getPersonalNado(), this.config.showVerzikNadoColor(), 2, this.config.showVerzikNadoColor().getAlpha(), this.config.verzikNadoOpacity());
                    }
                }
            if (this.config.raveNados() != SpoonTobConfig.raveNadoMode.OFF && this.verzik.verzikPhase == Verzik.Phase.PHASE3) {
                int index = 0;
                for (NPC nado : this.client.getNpcs()) {
                    if (Verzik.NADO_IDS.contains(nado.getId())) {
                        Color color;
                        if (this.config.raveNados() == SpoonTobConfig.raveNadoMode.RAVE) {
                            color = this.plugin.raveUtils.getColor(nado.hashCode(), true);
                        } else {
                            color = this.plugin.raveUtils.getColor(index * 50, false);
                        }
                        renderTargetOverlay(graphics, nado, color);
                    }
                    index++;
                }
            }
            if (this.config.showVerzikRangeAttack())
                for (WorldPoint p : this.verzik.verzikRangeProjectiles.values()) {
                    LocalPoint point = LocalPoint.fromWorld(this.client, p);
                    if (point != null) {
                        Polygon poly = Perspective.getCanvasTilePoly(this.client, point);
                        graphics.setColor(new Color(this.config.verzikRangeAttacksColor().getRed(), this.config.verzikRangeAttacksColor().getGreen(), this.config.verzikRangeAttacksColor().getBlue(), this.config.verzikRangeAttacksColor().getAlpha()));
                        graphics.drawPolygon(poly);
                        graphics.setColor(new Color(this.config.verzikRangeAttacksColor().getRed(), this.config.verzikRangeAttacksColor().getGreen(), this.config.verzikRangeAttacksColor().getBlue(), this.config.verzikRangeAttacksFill()));
                        graphics.fillPolygon(poly);
                    }
                }
            if ((this.config.showVerzikYellows() == SpoonTobConfig.verzikYellowsMode.YELLOW || (this.config
                    .showVerzikYellows() == SpoonTobConfig.verzikYellowsMode.GROUPS && this.verzik.yellowGroups.size() == 0)) && this.verzik.yellowsOut) {
                String text = Integer.toString(this.verzik.yellowTimer);
                if (this.config.yellowTicksOnPlayer() && this.client.getLocalPlayer() != null) {
                    Point point = Perspective.getCanvasTextLocation(this.client, graphics, this.client.getLocalPlayer().getLocalLocation(), text, this.config.yellowsOffset());
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, text, Color.WHITE, point);
                    } else {
                        renderSteroidsTextLocation(graphics, text, this.config.yellowsSize(), 1, Color.WHITE, point);
                    }
                }
                for (WorldPoint wp : this.verzik.yellowsList) {
                    LocalPoint lp = LocalPoint.fromWorld(this.client, wp);
                    drawTile(graphics, wp, Color.YELLOW, 2, 255, 0);
                    if (!this.config.yellowTicksOnPlayer() && lp != null) {
                        Point point = Perspective.getCanvasTextLocation(this.client, graphics, lp, text, 0);
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, text, Color.WHITE, point);
                            continue;
                        }
                        renderResizeTextLocation(graphics, text, 12, 1, Color.WHITE, point);
                    }
                }
            }
            if (this.config.showVerzikRocks() && this.verzik.verzikPhase == Verzik.Phase.PHASE1)
                for (GraphicsObject object : this.client.getGraphicsObjects()) {
                    if (object.getId() == 1436) {
                        LocalPoint lp = object.getLocation();
                        drawTile(graphics, WorldPoint.fromLocal(this.client, lp), this.config.showVerzikRocksColor(), 2, 255, 50);
                    }
                }
            if (this.config.showVerzikAcid() && this.verzik.verzikPhase == Verzik.Phase.PHASE2 && this.client.getLocalPlayer() != null) {
                int index = 0;
                for (GameObject object : this.verzik.acidSpots) {
                    if (this.client.getLocalPlayer().getWorldLocation().distanceTo(object.getWorldLocation()) <= this.config.showVerzikAcidDistance()) {
                        LocalPoint lp = object.getLocalLocation();
                        String text = String.valueOf(this.verzik.acidSpotsTimer.get(index));
                        Point point = Perspective.getCanvasTextLocation(this.client, graphics, lp, text, 0);
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, text, Color.WHITE, point);
                        } else {
                            renderSteroidsTextLocation(graphics, text, 12, 1, Color.WHITE, point);
                        }
                        drawTile(graphics, WorldPoint.fromLocal(this.client, lp), this.config.showVerzikAcidColor(), 2, 255, 0);
                    }
                    index++;
                }
            }
            this.verzik.getVerzikAggros().forEach(k -> {
                if (this.config.verzikNyloAggroWarning() && k.getInteracting() != null && !k.isDead()) {
                    String targetText = "";
                    if (k.getInteracting().getName() != null)
                        if (k.getInteracting().getName().equalsIgnoreCase("wayabove") || k.getInteracting().getName().equalsIgnoreCase("oblv way")) {
                            targetText = "Wayaboob";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("flaw less") || k.getInteracting().getName().equalsIgnoreCase("oblv flaw") || k.getInteracting().getName().equalsIgnoreCase("flaaw less")) {
                            targetText = "Glennjamin";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("xelywood")) {
                            targetText = "Femboy";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("afka") || k.getInteracting().getName().equalsIgnoreCase("rattori") || k.getInteracting().getName().equalsIgnoreCase("sadgecry") || k.getInteracting().getName().equalsIgnoreCase("squish that")) {
                            targetText = "thisiswhyudonthavedust";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("messywalcott")) {
                            targetText = "Rat";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("divinesdream") || k.getInteracting().getName().equalsIgnoreCase("divine dream") || k.getInteracting().getName().equalsIgnoreCase("trio tob")) {
                            targetText = "Lil Bitch";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("null god")) {
                            targetText = "Click";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("noobtype")) {
                            targetText = "Sick Invite";
                        } else if (k.getInteracting().getName().equalsIgnoreCase("turbosmurf") || k.getInteracting().getName().equalsIgnoreCase("yukinon fan")) {
                            targetText = k.getInteracting().getName();
                        } else {
                            targetText = k.getInteracting().getName();
                        }
                    Point textLocation = k.getCanvasTextLocation(graphics, targetText, 80);
                    if (textLocation != null) {
                        Color color = Color.GREEN;
                        if (k.getInteracting().equals(this.client.getLocalPlayer()))
                            color = Color.RED;
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, targetText, color, textLocation);
                        } else {
                            renderResizeTextLocation(graphics, targetText, 14, 1, color, textLocation);
                        }
                    }
                }
                if (this.config.verzikNyloExplodeRange() == SpoonTobConfig.VerzikNyloSetting.ALL_CRABS || (this.config.verzikNyloExplodeRange() == SpoonTobConfig.VerzikNyloSetting.MY_CRABS && this.client.getLocalPlayer() != null && this.client.getLocalPlayer().equals(k.getInteracting()))) {
                    int size = 1;
                    int thick_size = 1;
                    NPCComposition composition = k.getTransformedComposition();
                    if (composition != null)
                        size = composition.getSize() + 2 * thick_size;
                    LocalPoint lp = LocalPoint.fromWorld(this.client, k.getWorldLocation());
                    if (lp != null) {
                        lp = new LocalPoint(lp.getX() - thick_size * 128, lp.getY() - thick_size * 128);
                        Polygon tilePoly = getCanvasTileAreaPoly(this.client, lp, size, false);
                        if (tilePoly != null)
                            renderPoly(graphics, this.config.verzikNyloExplodeTileColor(), tilePoly);
                    }
                }
            });
            if (this.config.redsHp() && this.verzik.redCrabs.size() > 0 && this.verzik.verzikPhase == Verzik.Phase.PHASE2) {
                int index = 0;
                for (NPC crab : this.verzik.redCrabs) {
                    Color textColor = Color.WHITE;
                    String text = "";
                    if (crab.getHealthRatio() > 0 || ((Integer) this.verzik.lastRatioList.get(index) != 0 && (Integer) this.verzik.lastHealthScaleList.get(index) != 0)) {
                        if (crab.getHealthRatio() > 0) {
                            this.verzik.lastRatioList.set(index, crab.getHealthRatio());
                            this.verzik.lastHealthScaleList.set(index, crab.getHealthScale());
                        }
                        float floatRatio = (float) (Integer) this.verzik.lastRatioList.get(index) / (Integer) this.verzik.lastHealthScaleList.get(index) * 100.0F;
                        if (this.config.oldHpThreshold()) {
                            textColor = this.plugin.oldHitpointsColor(floatRatio);
                        } else {
                            textColor = this.plugin.calculateHitpointsColor(floatRatio);
                        }
                        text = Float.toString(floatRatio).substring(0, 4) + "%";
                        Point textLoc = crab.getCanvasTextLocation(graphics, text, 50);
                        if (this.config.fontStyle()) {
                            renderTextLocation(graphics, text, textColor, textLoc);
                        } else {
                            renderResizeTextLocation(graphics, text, 14, 1, textColor, textLoc);
                        }
                    }
                    index++;
                }
            }
            if (this.config.showVerzikTicks() || this.config.showVerzikAttacks() != SpoonTobConfig.verzikAttacksMode.OFF || this.config.showVerzikTotalTicks()) {
                String tick_text = "";
                if (((this.config.showVerzikAttacks() == SpoonTobConfig.verzikAttacksMode.ALL && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS) || (this.config
                        .showVerzikAttacks() == SpoonTobConfig.verzikAttacksMode.P2 && this.verzik.verzikPhase == Verzik.Phase.PHASE2) || (this.config
                        .showVerzikAttacks() == SpoonTobConfig.verzikAttacksMode.REDS && this.verzik.verzikRedPhase)) && ((this.config
                        .showVerzikAttacks() == SpoonTobConfig.verzikAttacksMode.REDS && this.verzik.verzikRedPhase) || this.config.showVerzikAttacks() != SpoonTobConfig.verzikAttacksMode.REDS)) {
                    tick_text = tick_text + "[A] " + this.verzik.getVerzikAttackCount();
                    if (this.config.showVerzikTicks() || this.config.showVerzikTotalTicks())
                        tick_text = tick_text + " : ";
                }
                if (this.config.showVerzikTicks() && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS && this.verzik.verzikPhase == Verzik.Phase.PHASE1) {
                    tick_text = tick_text + this.verzik.getVerzikTicksUntilAttack();
                    if (this.config.showVerzikTotalTicks())
                        tick_text = tick_text + " : ";
                }
                if (this.config.showVerzikTicks() && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS && this.verzik.verzikPhase == Verzik.Phase.PHASE2 && !this.verzik.verzikRedPhase) {
                    tick_text = tick_text + this.verzik.getVerzikTicksUntilAttack();
                    if (this.config.showVerzikTotalTicks())
                        tick_text = tick_text + " : ";
                }
                if (this.config.showVerzikTicks() && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS && this.verzik.verzikPhase == Verzik.Phase.PHASE2 && this.verzik.verzikRedPhase) {
                    tick_text = tick_text + this.verzik.getVerzikTicksUntilAttack();
                    if (this.config.showVerzikTotalTicks())
                        tick_text = tick_text + " : ";
                }
                if (this.config.showVerzikTicks() && this.verzik.getVerzikSpecial() != Verzik.SpecialAttack.WEBS && this.verzik.verzikPhase == Verzik.Phase.PHASE3) {
                    tick_text = tick_text + this.verzik.getVerzikTicksUntilAttack();
                    if (this.config.showVerzikTotalTicks())
                        tick_text = tick_text + " : ";
                }
                if (this.config.showVerzikTotalTicks())
                    tick_text = tick_text + "(" + this.verzik.getVerzikTotalTicksUntilAttack() + ")";
                Point canvasPoint = this.verzik.getVerzikNPC().getCanvasTextLocation(graphics, tick_text, 60);
                if (canvasPoint != null) {
                    Color col = this.verzik.verzikSpecialWarningColor();
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, tick_text, col, canvasPoint);
                    } else {
                        renderResizeTextLocation(graphics, tick_text, 15, 1, col, canvasPoint);
                    }
                }
            }
            if (this.config.lightningInfobox() != SpoonTobConfig.lightningMode.OFF && (
                    this.config.lightningInfobox() == SpoonTobConfig.lightningMode.OVERLAY || this.config.lightningInfobox() == SpoonTobConfig.lightningMode.BOTH) && this.verzik.verzikPhase == Verzik.Phase.PHASE2) {
                String zapText;
                if (this.verzik.lightningAttacks > 0) {
                    zapText = Integer.toString(this.verzik.lightningAttacks);
                } else {
                    zapText = "ZAP";
                }
                Point canvasPoint = this.verzik.getVerzikNPC().getCanvasTextLocation(graphics, zapText, 270);
                if (canvasPoint != null && !this.verzik.getVerzikNPC().isDead())
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, zapText, Color.ORANGE, canvasPoint);
                    } else {
                        renderResizeTextLocation(graphics, zapText, 15, 1, Color.ORANGE, canvasPoint);
                    }
            }
            if (this.config.lightningAttackTick() && this.verzik.verzikPhase == Verzik.Phase.PHASE2)
                for (Projectile p : this.verzik.getVerzikLightningProjectiles().keySet()) {
                    Player localPlayer = this.client.getLocalPlayer();
                    if (localPlayer != null && p.getInteracting() == localPlayer) {
                        int ticks = (Integer) this.verzik.getVerzikLightningProjectiles().get(p);
                        String tickstring = String.valueOf(ticks);
                        Point point = Perspective.getCanvasTextLocation(this.client, graphics, localPlayer.getLocalLocation(), tickstring, this.config.zapOffset());
                        if (point != null) {
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, tickstring, (ticks > 0) ? Color.WHITE : Color.ORANGE, point);
                                continue;
                            }
                            renderSteroidsTextLocation(graphics, tickstring, this.config.zapSize(), 1, (ticks > 0) ? Color.WHITE : Color.ORANGE, point);
                        }
                    }
                }
        }
        return null;
    }

    private void displayProjectiles(Graphics2D graphics) {
        for (Projectile p : this.client.getProjectiles()) {
            Actor interacting = p.getInteracting();
            if (p.getId() == 1598 && interacting != null) {
                if (this.config.displayGreenBall() != SpoonTobConfig.greenBallMode.OFF) {
                    int size;
                    if (this.config.displayGreenBall() == SpoonTobConfig.greenBallMode.TILE) {
                        size = 1;
                    } else {
                        size = 3;
                    }
                    Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, interacting.getLocalLocation(), size);
                    renderPolygon(graphics, tilePoly, Color.GREEN);
                }
                if (this.config.displayGreenBallTicks()) {
                    String text = String.valueOf(p.getRemainingCycles() / 30);
                    LocalPoint lp = interacting.getLocalLocation();
                    Point point = Perspective.getCanvasTextLocation(this.client, graphics, lp, text, this.config.greenBallOffset());
                    Color color = Color.RED;
                    if (this.config.fontStyle()) {
                        renderTextLocation(graphics, text, color, point);
                        continue;
                    }
                    renderSteroidsTextLocation(graphics, text, this.config.greenBallSize(), 1, color, point);
                }
            }
        }
    }

    private void displayPurpleCrabAOE(Graphics2D graphics, NPC npc) {
        if (this.config.purpleAoe() && Verzik.P2_IDS.contains(npc.getId()) && this.verzik.getPurpleCrabProjectile().size() > 0)
            this.verzik.getPurpleCrabProjectile().forEach((point, ticks) -> {
                Point textLocation = Perspective.getCanvasTextLocation(this.client, graphics, point, "#", 0);
                if (this.config.fontStyle()) {
                    renderTextLocation(graphics, Integer.toString(ticks), Color.WHITE, textLocation);
                } else {
                    renderSteroidsTextLocation(graphics, Integer.toString(ticks), 13, 1, Color.WHITE, textLocation);
                }
                Polygon tileAreaPoly = Perspective.getCanvasTileAreaPoly(this.client, point, 3);
                renderPolygon(graphics, tileAreaPoly, new Color(106, 61, 255));
            });
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

    private void renderTargetOverlay(Graphics2D graphics, NPC actor, Color color) {
        Shape objectClickbox = actor.getConvexHull();
        if (objectClickbox != null) {
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            graphics.fill(actor.getConvexHull());
        }
    }
}
