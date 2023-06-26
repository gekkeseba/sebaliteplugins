package net.runelite.client.plugins.spoontob.rooms.Sotetseg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RescaleOp;
import java.util.Random;
import javax.inject.Inject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.ImageUtil;

public class SotetsegOverlay extends RoomOverlay {
    @Inject
    private Sotetseg sotetseg;

    @Inject
    private SkillIconManager iconManager;

    @Inject
    protected SotetsegOverlay(SpoonTobConfig config) {
        super(config);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.sotetseg.isSotetsegActive()) {
            displaySotetsegCounters(graphics);
            if (this.config.sotetsegMaze()) {
                int counter = 1;
                for (Point p : this.sotetseg.getRedTiles()) {
                    WorldPoint wp = this.sotetseg.worldPointFromMazePoint(p);
                    drawTile(graphics, wp, Color.GREEN, 1, 255, 0);
                    LocalPoint lp = LocalPoint.fromWorld(this.client, wp);
                    if (lp != null && !this.sotetseg.isWasInUnderWorld()) {
                        Point textPoint = Perspective.getCanvasTextLocation(this.client, graphics, lp, String.valueOf(counter), 0);
                        if (textPoint != null)
                            renderTextLocation(graphics, String.valueOf(counter), Color.GREEN, textPoint);
                    }
                    counter++;
                }
                for (Point p : this.sotetseg.getGreenTiles()) {
                    WorldPoint wp = this.sotetseg.worldPointFromMazePoint(p);
                    drawTile(graphics, wp, Color.GREEN, 1, 255, 0);
                }
            }
            if (this.config.sotetsegShowOrbs() != SpoonTobConfig.soteOrbMode.OFF || this.config.sotetsegShowNuke() != SpoonTobConfig.soteDeathballOverlayMode.OFF)
                for (Projectile p : this.client.getProjectiles()) {
                    int id = p.getId();
                    Point point = Perspective.localToCanvas(this.client, new LocalPoint((int)p.getX(), (int)p.getY()), 0, Perspective.getTileHeight(this.client, new LocalPoint((int)p.getX(), (int)p.getY()), p.getFloor()) - (int)p.getZ());
                    String ticks = String.valueOf(p.getRemainingCycles() / 30);
                    if (point != null) {
                        if (this.config.sotetsegShowOrbs() == SpoonTobConfig.soteOrbMode.HATS || this.config.sotetsegShowOrbs() == SpoonTobConfig.soteOrbMode.BOTH) {
                            if (id == 1606 && p.getInteracting() == this.client.getLocalPlayer()) {
                                BufferedImage icon;
                                if (this.config.raveHats() == SpoonTobConfig.raveHatsMode.RAVE || this.config.raveHats() == SpoonTobConfig.raveHatsMode.TURBO) {
                                    icon = ImageUtil.loadImageResource(SpoonTobPlugin.class, "magic" + this.sotetseg.mageHatNum + ".png");
                                } else if (this.config.raveHats() == SpoonTobConfig.raveHatsMode.EPILEPSY) {
                                    icon = ImageUtil.loadImageResource(SpoonTobPlugin.class, "magic" + (new Random()).nextInt(8) + 1 + ".png");
                                } else {
                                    icon = this.sotetseg.mageIcon;
                                }
                                Point iconlocation = new Point(point.getX() - icon.getWidth() / 2, point.getY() - 30);
                                if (this.config.raveHats() == SpoonTobConfig.raveHatsMode.TURBO) {
                                    graphics.drawImage(icon, iconlocation.getX(), iconlocation.getY(), this.sotetseg.turboHatWidth, this.sotetseg.turboHatHeight, null);
                                } else {
                                    OverlayUtil.renderImageLocation(graphics, iconlocation, icon);
                                }
                                if (p.getInteracting() == this.client.getLocalPlayer())
                                    OverlayUtil.renderImageLocation(graphics, iconlocation, icon);
                            }
                            if (id == 1607 && p.getInteracting() == this.client.getLocalPlayer()) {
                                BufferedImage icon;
                                if (this.config.raveHats() == SpoonTobConfig.raveHatsMode.RAVE || this.config.raveHats() == SpoonTobConfig.raveHatsMode.TURBO) {
                                    icon = ImageUtil.loadImageResource(SpoonTobPlugin.class, "ranged" + this.sotetseg.rangeHatNum + ".png");
                                } else if (this.config.raveHats() == SpoonTobConfig.raveHatsMode.EPILEPSY) {
                                    icon = ImageUtil.loadImageResource(SpoonTobPlugin.class, "ranged" + (new Random()).nextInt(8) + 1 + ".png");
                                } else {
                                    icon = this.sotetseg.rangeIcon;
                                }
                                Point iconlocation = new Point(point.getX() - icon.getWidth() / 2, point.getY() - 30);
                                if (this.config.raveHats() == SpoonTobConfig.raveHatsMode.TURBO) {
                                    graphics.drawImage(icon, iconlocation.getX(), iconlocation.getY(), this.sotetseg.turboHatWidth, this.sotetseg.turboHatHeight, null);
                                } else {
                                    OverlayUtil.renderImageLocation(graphics, iconlocation, icon);
                                }
                                if (p.getInteracting() == this.client.getLocalPlayer())
                                    OverlayUtil.renderImageLocation(graphics, iconlocation, icon);
                            }
                        }
                        if (p.getInteracting() == this.client.getLocalPlayer() && (id == 1606 || id == 1607) && (this.config
                                .sotetsegShowOrbs() == SpoonTobConfig.soteOrbMode.TICKS || this.config.sotetsegShowOrbs() == SpoonTobConfig.soteOrbMode.BOTH))
                            if (this.config.fontStyle()) {
                                renderTextLocation(graphics, ticks, (id == 1606) ? Color.CYAN : Color.GREEN, point);
                            } else {
                                renderSteroidsTextLocation(graphics, ticks, 17, 1, (id == 1606) ? Color.CYAN : Color.GREEN, point);
                            }
                        if (id == 1604 && (this.config.sotetsegShowNuke() == SpoonTobConfig.soteDeathballOverlayMode.TICKS || this.config
                                .sotetsegShowNuke() == SpoonTobConfig.soteDeathballOverlayMode.BOTH)) {
                            Color color = Color.ORANGE;
                            if (this.config.deathTicksOnPlayer()) {
                                point = Perspective.getCanvasTextLocation(this.client, graphics, p.getInteracting().getLocalLocation(), ticks, this.config.deathballOffset());
                                if (this.config.fontStyle()) {
                                    renderTextLocation(graphics, ticks, Color.WHITE, point);
                                } else {
                                    renderSteroidsTextLocation(graphics, ticks, this.config.deathballSize(), 1, color, point);
                                }
                            } else if (this.config.fontStyle()) {
                                renderTextLocation(graphics, ticks, color, point);
                            } else {
                                renderSteroidsTextLocation(graphics, ticks, 20, 1, color, point);
                            }
                            if (this.config.displayDeathBall())
                                renderPoly(graphics, this.config.displayDeathBallColor(), p.getInteracting().getCanvasTilePoly());
                            Point imagelocation = new Point(point.getX() - Sotetseg.TACTICAL_NUKE_OVERHEAD.getWidth() / 2, point.getY() - 60);
                            if (this.config.sotetsegShowNuke() == SpoonTobConfig.soteDeathballOverlayMode.NUKE || this.config.sotetsegShowNuke() == SpoonTobConfig.soteDeathballOverlayMode.BOTH)
                                OverlayUtil.renderImageLocation(graphics, imagelocation, Sotetseg.TACTICAL_NUKE_OVERHEAD);
                        }
                    }
                }
        }
        return null;
    }

    private void displaySotetsegCounters(Graphics2D graphics) {
        if (this.sotetseg.sotetsegTicks > 0 && this.sotetseg.sotetsegNPC != null) {
            String text = "";
            String yuriText = "";
            if (this.config.deathballInfobox() == SpoonTobConfig.soteDeathballMode.OVERLAY || this.config.deathballInfobox() == SpoonTobConfig.soteDeathballMode.BOTH)
                if (this.config.deathballSingleLine()) {
                    if (this.sotetseg.sotetsegAttacksLeft == 0) {
                        text = text + "Nuke";
                    } else {
                        text = text + text;
                    }
                } else if (this.sotetseg.sotetsegAttacksLeft == 0) {
                    yuriText = "Nuke";
                } else {
                    yuriText = String.valueOf(this.sotetseg.sotetsegAttacksLeft);
                }
            if (this.config.showSotetsegAttackTicks())
                if (text.equals("")) {
                    text = text + text;
                } else {
                    text = text + " : " + text;
                }
            Point textLocation = this.sotetseg.sotetsegNPC.getCanvasTextLocation(graphics, text, 50);
            if (this.config.fontStyle()) {
                renderTextLocation(graphics, text, Color.WHITE, textLocation);
                if (!this.config.deathballSingleLine() && !yuriText.equals("")) {
                    Point yuriTextLocation = this.sotetseg.sotetsegNPC.getCanvasTextLocation(graphics, yuriText, 200);
                    renderTextLocation(graphics, yuriText, Color.ORANGE, yuriTextLocation);
                }
            } else {
                renderResizeTextLocation(graphics, text, 14, 1, Color.WHITE, textLocation);
                if (!this.config.deathballSingleLine() && !yuriText.equals("")) {
                    Point yuriTextLocation = this.sotetseg.sotetsegNPC.getCanvasTextLocation(graphics, yuriText, 200);
                    renderResizeTextLocation(graphics, yuriText, 14, 1, Color.ORANGE, yuriTextLocation);
                }
            }
        }
    }

    private String getSotetsegTicksString() {
        return Byte.toString(this.sotetseg.getSotetsegTicks());
    }

    public static BufferedImage fadeImage(Image img, float fade, float target) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bi = new BufferedImage(w, h, 2);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, (ImageObserver)null);
        float offset = target * (1.0F - fade);
        float[] scales = { fade, fade, fade, 1.0F };
        float[] offsets = { offset, offset, offset, 0.0F };
        RescaleOp rop = new RescaleOp(scales, offsets, (RenderingHints)null);
        g.drawImage(bi, rop, 0, 0);
        g.dispose();
        return bi;
    }

    public static void renderPolygon(Graphics2D graphics, Shape poly, Color color, Color color2, int width) {
        graphics.setColor(color);
        Stroke originalStroke = graphics.getStroke();
        graphics.setStroke(new BasicStroke(width));
        graphics.draw(poly);
        graphics.setColor(color2);
        graphics.fill(poly);
        graphics.setStroke(originalStroke);
    }
}
