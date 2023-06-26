package net.runelite.client.plugins.spoontob.rooms.Maiden;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.util.Iterator;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig.redsTlMode;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class MaidenRedsOverlay extends RoomOverlay {
    @Inject
    private SpoonTobPlugin plugin;
    @Inject
    private SpoonTobConfig config;
    @Inject
    private Client client;
    @Inject
    private Maiden maiden;

    @Inject
    public MaidenRedsOverlay(Client client, SpoonTobConfig config, SpoonTobPlugin plugin) {
        super(config);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        this.setPosition(OverlayPosition.DYNAMIC);
        this.setPriority(OverlayPriority.HIGH);
        this.setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if ((this.config.redsTL() != redsTlMode.OFF || this.config.redsFreezeWarning()) && this.plugin.enforceRegion()) {
            boolean canFreeze = true;
            boolean n1Spawned = false;
            boolean n2Spawned = false;
            boolean threeSpawned = false;
            Iterator var6 = this.maiden.maidenCrabInfoList.iterator();

            MaidenCrabInfo mci;
            int size;
            while(var6.hasNext()) {
                mci = (MaidenCrabInfo)var6.next();
                if (this.maiden.maidenPhase == mci.phase) {
                    switch (mci.position) {
                        case "N1":
                            n1Spawned = true;
                            break;
                        case "N2":
                            n2Spawned = true;
                            break;
                        case "N3":
                        case "S3":
                            threeSpawned = true;
                    }
                }
            }

            var6 = this.maiden.maidenCrabInfoList.iterator();

            while(true) {
                while(true) {
                    Polygon tilePoly;
                    do {
                        LocalPoint lp;
                        do {
                            NPCComposition composition;
                            do {
                                if (!var6.hasNext()) {
                                    return null;
                                }

                                mci = (MaidenCrabInfo)var6.next();
                                composition = mci.crab.getComposition();
                            } while(composition == null);

                            size = composition.getSize();
                            lp = LocalPoint.fromWorld(this.client, mci.crab.getWorldLocation());
                        } while(lp == null);

                        lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
                        tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                    } while(tilePoly == null);

                    String spawn = mci.position;
                    WorldPoint maidenWp = this.maiden.getMaidenNPC().getWorldLocation();
                    int maidenX = maidenWp.getX();
                    NPCComposition maidenModel = this.maiden.getMaidenNPC().getTransformedComposition();
                    if (maidenModel != null) {
                        maidenX += maidenModel.getSize();
                    }

                    WorldPoint healerWp = mci.crab.getWorldLocation();
                    int healerX = healerWp.getX();
                    int deltaX = Math.max(0, healerX - maidenX);
                    if (threeSpawned) {
                        if (n1Spawned && n2Spawned) {
                            if (spawn.equals("N1")) {
                                if (deltaX < 4 && mci.frozenTicks > 0) {
                                    canFreeze = false;
                                }
                            } else if (spawn.equals("N2") && deltaX < 3) {
                                canFreeze = mci.frozenTicks <= 0;
                            }
                        } else if (n1Spawned) {
                            if (spawn.equals("N1") && deltaX < 1 && mci.frozenTicks > 0) {
                                canFreeze = false;
                            }
                        } else if (n2Spawned && spawn.equals("N2") && deltaX < 3 && mci.frozenTicks > 0) {
                            canFreeze = false;
                        }
                    }

                    if (!canFreeze && this.client.getVarbitValue(4070) == 1 && SpoonTobPlugin.partySize > 3 && this.config.redsFreezeWarning() && (mci.position.equals("N3") || mci.position.equals("S3")) && mci.phase == this.maiden.maidenPhase && mci.frozenTicks == -1) {
                        this.renderPoly(graphics, tilePoly, this.config.redsFreezeWarningColor(), this.config.redsFreezeWarningColor().getAlpha(), 50);
                    } else if ((this.config.redsTL() == redsTlMode.MAIDEN || this.config.redsTL() == redsTlMode.BOTH) && TheatreRegions.inRegion(this.client, TheatreRegions.MAIDEN)) {
                        this.renderPoly(graphics, tilePoly, this.config.redsTLColor(), this.config.redsTLColor().getAlpha(), 0);
                    }
                }
            }
        } else {
            return null;
        }
    }

    private void renderPoly(Graphics2D graphics, Shape polygon, Color color, int outlineOpacity, int fillOpacity) {
        if (polygon != null) {
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), outlineOpacity));
            graphics.setStroke(new BasicStroke(1.0F));
            graphics.draw(polygon);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), fillOpacity));
            graphics.fill(polygon);
        }

    }
}
