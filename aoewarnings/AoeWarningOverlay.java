package net.runelite.client.plugins.aoewarnings;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class AoeWarningOverlay extends Overlay {
    private static final int FILL_START_ALPHA = 25;

    private static final int OUTLINE_START_ALPHA = 255;

    private final Client client;

    private final AoeWarningPlugin plugin;

    private final AoeWarningConfig config;

    @Inject
    public AoeWarningOverlay(Client client, AoeWarningPlugin plugin, AoeWarningConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    public Dimension render(Graphics2D graphics) {
        WorldPoint lp = this.client.getLocalPlayer().getWorldLocation();
        this.plugin.getLightningTrail().forEach(o -> OverlayUtil.drawTiles(graphics, this.client, o, lp, new Color(0, 150, 200), 2, 150, 50));
        this.plugin.getAcidTrail().forEach(o -> OverlayUtil.drawTiles(graphics, this.client, o.getWorldLocation(), lp, new Color(69, 241, 44), 2, 150, 50));
        this.plugin.getCrystalSpike().forEach(o -> OverlayUtil.drawTiles(graphics, this.client, o.getWorldLocation(), lp, new Color(255, 0, 84), 2, 150, 50));
        this.plugin.getWintertodtSnowFall().forEach(o -> OverlayUtil.drawTiles(graphics, this.client, o.getWorldLocation(), lp, new Color(255, 0, 84), 2, 150, 50));
        Instant now = Instant.now();
        Set<ProjectileContainer> projectiles = this.plugin.getProjectiles();
        projectiles.forEach(proj -> {
            Color color;
            int fillAlpha;
            int outlineAlpha;
            if (proj.getTargetPoint() == null)
                return;
            if (now.isAfter(proj.getStartTime().plus(Duration.ofMillis(proj.getLifetime()))))
                return;
            if (proj.getProjectile().getId() == 366 || proj.getProjectile().getId() == 660)
                if (this.client.getVarbitValue(5432) == 0)
                    return;
            Polygon tilePoly = Perspective.getCanvasTileAreaPoly(this.client, proj.getTargetPoint(), proj.getAoeProjectileInfo().getAoeSize());
            if (tilePoly == null)
                return;
            double progress = (System.currentTimeMillis() - proj.getStartTime().toEpochMilli()) / proj.getLifetime();
            int tickProgress = proj.getFinalTick() - this.client.getTickCount();
            if (this.config.isFadeEnabled()) {
                fillAlpha = (int)((1.0D - progress) * 25.0D);
                outlineAlpha = (int)((1.0D - progress) * 255.0D);
            } else {
                fillAlpha = 25;
                outlineAlpha = 255;
            }
            if (tickProgress == 0) {
                color = Color.RED;
            } else {
                color = Color.WHITE;
            }
            if (fillAlpha < 0)
                fillAlpha = 0;
            if (outlineAlpha < 0)
                outlineAlpha = 0;
            if (fillAlpha > 255)
                fillAlpha = 255;
            if (outlineAlpha > 255)
                outlineAlpha = 255;
            if (this.config.isOutlineEnabled()) {
                graphics.setColor(new Color(ColorUtil.setAlphaComponent(this.config.overlayColor().getRGB(), outlineAlpha), true));
                graphics.drawPolygon(tilePoly);
            }
            if (this.config.tickTimers() && tickProgress >= 0)
                OverlayUtil.renderTextLocation(graphics, Integer.toString(tickProgress), this.config.textSize(), this.config.fontStyle().getFont(), color, centerPoint(tilePoly.getBounds()), this.config.shadows(), 0);
            graphics.setColor(new Color(ColorUtil.setAlphaComponent(this.config.overlayColor().getRGB(), fillAlpha), true));
            graphics.fillPolygon(tilePoly);
        });
        projectiles.removeIf(proj -> now.isAfter(proj.getStartTime().plus(Duration.ofMillis(proj.getLifetime()))));
        return null;
    }

    private Point centerPoint(Rectangle rect) {
        int x = (int)(rect.getX() + rect.getWidth() / 2.0D);
        int y = (int)(rect.getY() + rect.getHeight() / 2.0D);
        return new Point(x, y);
    }
}
