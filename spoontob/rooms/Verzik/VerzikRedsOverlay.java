package net.runelite.client.plugins.spoontob.rooms.Verzik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

public class VerzikRedsOverlay extends RoomOverlay {
    @Inject
    private SpoonTobPlugin plugin;

    @Inject
    private SpoonTobConfig config;

    @Inject
    private Client client;

    @Inject
    private Verzik verzik;

    @Inject
    public VerzikRedsOverlay(Client client, SpoonTobConfig config, SpoonTobPlugin plugin) {
        super(config);
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if ((this.config.redsTL() != SpoonTobConfig.redsTlMode.OFF || this.config.redsFreezeWarning()) && this.plugin.enforceRegion())
            for (NPC reds : this.client.getNpcs()) {
                if (reds.getName() != null && reds.getName().equalsIgnoreCase("nylocas matomenos")) {
                    NPCComposition composition = reds.getComposition();
                    int size = composition.getSize();
                    LocalPoint lp = LocalPoint.fromWorld(this.client, reds.getWorldLocation());
                    if (lp != null) {
                        lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
                        Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, lp, size);
                        if (tilePoly != null && (
                                this.config.redsTL() == SpoonTobConfig.redsTlMode.VERZIK || this.config.redsTL() == SpoonTobConfig.redsTlMode.BOTH) &&
                                TheatreRegions.inRegion(this.client, TheatreRegions.VERZIK))
                            renderPoly(graphics, tilePoly, this.config.redsTLColor(), this.config.redsTLColor().getAlpha(), 0);
                    }
                }
            }
        return null;
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
