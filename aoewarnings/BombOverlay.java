package net.runelite.client.plugins.aoewarnings;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class BombOverlay extends Overlay {
    private static final Logger log = LoggerFactory.getLogger(BombOverlay.class);

    private static final String SAFE = "#00cc00";

    private static final String CAUTION = "#ffff00";

    private static final String WARNING = "#ff9933";

    private static final String DANGER = "#ff6600";

    private static final String LETHAL = "#cc0000";

    private static final int BOMB_AOE = 7;

    private static final int BOMB_DETONATE_TIME = 8;

    private static final double ESTIMATED_TICK_LENGTH = 0.6D;

    private static final NumberFormat TIME_LEFT_FORMATTER = DecimalFormat.getInstance(Locale.US);

    private final Client client;

    private final AoeWarningPlugin plugin;

    private final AoeWarningConfig config;

    static {
        ((DecimalFormat)TIME_LEFT_FORMATTER).applyPattern("#0.0");
    }

    @Inject
    public BombOverlay(Client client, AoeWarningPlugin plugin, AoeWarningConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.bombDisplay())
            drawDangerZone(graphics);
        return null;
    }

    private void drawDangerZone(Graphics2D graphics) {
        WorldPoint loc = this.client.getLocalPlayer().getWorldLocation();
        Map<WorldPoint, Integer> aoeTiles = new HashMap<>();
        this.plugin.getBombs().forEach(bomb -> {
            LocalPoint localLoc = LocalPoint.fromWorld(this.client, bomb.getWorldLocation());
            WorldPoint worldLoc = bomb.getWorldLocation();
            if (localLoc == null)
                return;
            double distance_x = Math.abs(worldLoc.getX() - loc.getX());
            double distance_y = Math.abs(worldLoc.getY() - loc.getY());
            Color color_code = Color.decode("#00cc00");
            if (distance_x < 1.0D && distance_y < 1.0D) {
                color_code = Color.decode("#cc0000");
            } else if (distance_x < 2.0D && distance_y < 2.0D) {
                color_code = Color.decode("#ff6600");
            } else if (distance_x < 3.0D && distance_y < 3.0D) {
                color_code = Color.decode("#ff9933");
            } else if (distance_x < 4.0D && distance_y < 4.0D) {
                color_code = Color.decode("#ffff00");
            }
            LocalPoint CenterPoint = new LocalPoint(localLoc.getX(), localLoc.getY());
            Polygon poly = Perspective.getCanvasTileAreaPoly(this.client, CenterPoint, 7);
            if (this.config.bombHeatmap())
                for (int x = -3; x < 4; x++) {
                    for (int y = -3; y < 4; y++) {
                        WorldPoint aoeTile = new WorldPoint(worldLoc.getX() + x, worldLoc.getY() + y, loc.getPlane());
                        int severity = 1;
                        int abs_x = Math.abs(x);
                        int abs_y = Math.abs(y);
                        if (abs_x < 1 && abs_y < 1) {
                            severity = 4;
                        } else if (abs_x < 2 && abs_y < 2) {
                            severity = 3;
                        } else if (abs_x < 3 && abs_y < 3) {
                            severity = 2;
                        }
                        if (!aoeTiles.containsKey(aoeTile)) {
                            aoeTiles.put(aoeTile, Integer.valueOf(severity));
                        } else {
                            aoeTiles.put(aoeTile, Integer.valueOf(((Integer)aoeTiles.get(aoeTile)).intValue() + severity));
                        }
                    }
                }
            if (poly != null) {
                graphics.setColor(color_code);
                graphics.setStroke(new BasicStroke(1.0F));
                graphics.drawPolygon(poly);
                graphics.setColor(new Color(0, 0, 0, 10));
                graphics.fillPolygon(poly);
            }
            Instant now = Instant.now();
            double timeLeft = (8 - this.client.getTickCount() - bomb.getTickStarted()) * 0.6D - (now.toEpochMilli() - bomb.getLastClockUpdate().toEpochMilli()) / 1000.0D;
            timeLeft = Math.max(0.0D, timeLeft);
            String bombTimerString = TIME_LEFT_FORMATTER.format(timeLeft);
            int textWidth = graphics.getFontMetrics().stringWidth(bombTimerString);
            int textHeight = graphics.getFontMetrics().getAscent();
            Point canvasPoint = Perspective.localToCanvas(this.client, localLoc.getX(), localLoc.getY(), worldLoc.getPlane());
            if (canvasPoint != null) {
                Point canvasCenterPoint = new Point(canvasPoint.getX() - textWidth / 2, canvasPoint.getY() + textHeight / 2);
                OverlayUtil.renderTextLocation(graphics, canvasCenterPoint, bombTimerString, color_code);
            }
        });
        aoeTiles.forEach((tile, count) -> {
            LocalPoint localPoint = LocalPoint.fromWorld(this.client, tile);
            if (localPoint == null)
                return;
            Color color = Color.decode("#00cc00");
            if (count.intValue() == 2)
                color = Color.decode("#ffff00");
            if (count.intValue() == 3)
                color = Color.decode("#ff9933");
            if (count.intValue() == 4)
                color = Color.decode("#ff6600");
            if (count.intValue() >= 5)
                color = Color.decode("#cc0000");
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), this.config.bombHeatmapOpacity()));
            graphics.fill(Perspective.getCanvasTilePoly(this.client, localPoint));
        });
    }
}
