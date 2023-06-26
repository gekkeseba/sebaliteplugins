package net.runelite.client.plugins.spoontob.rooms.Nylocas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class NylocasAliveCounterOverlay extends OverlayPanel {
    private static final String prefix = "Nylocas alive: ";

    private final PanelComponent panelComponent = new PanelComponent();

    private LineComponent waveComponent;

    private SpoonTobPlugin plugin;

    private SpoonTobConfig config;

    private Instant nyloWaveStart;

    public void setNyloWaveStart(Instant nyloWaveStart) {
        this.nyloWaveStart = nyloWaveStart;
    }

    private int nyloAlive = 0;

    public int getNyloAlive() {
        return this.nyloAlive;
    }

    private int maxNyloAlive = 12;

    public int getMaxNyloAlive() {
        return this.maxNyloAlive;
    }

    private int wave = 0;

    public int getWave() {
        return this.wave;
    }

    private boolean hidden = false;

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    @Inject
    private NylocasAliveCounterOverlay(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super((Plugin)plugin);
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(OverlayPriority.HIGH);
        refreshPanel();
    }

    public void setNyloAlive(int aliveCount) {
        this.nyloAlive = aliveCount;
        refreshPanel();
    }

    public void setMaxNyloAlive(int maxAliveCount) {
        this.maxNyloAlive = maxAliveCount;
        refreshPanel();
    }

    public void setWave(int wave) {
        this.wave = wave;
        refreshPanel();
    }

    private void refreshPanel() {
        LineComponent lineComponent = LineComponent.builder().left("Alive: ").right("" + this.nyloAlive + "/" + this.nyloAlive).build();
        if (this.nyloAlive >= this.maxNyloAlive) {
            lineComponent.setRightColor(Color.ORANGE);
        } else {
            lineComponent.setRightColor(Color.GREEN);
        }
        this

                .waveComponent = LineComponent.builder().left("Wave: " + this.wave).build();
        this.panelComponent.getChildren().clear();
        this.panelComponent.getChildren().add(this.waveComponent);
        this.panelComponent.getChildren().add(lineComponent);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.nyloAlivePanel() && !isHidden()) {
            this.panelComponent.setPreferredSize(new Dimension(graphics.getFontMetrics().stringWidth("Alive: 24/24") + 10, 0));
            return this.panelComponent.render(graphics);
        }
        return null;
    }

    public String getFormattedTime() {
        Duration duration = Duration.between(this.nyloWaveStart, Instant.now());
        LocalTime localTime = LocalTime.ofSecondOfDay(duration.getSeconds());
        return localTime.format(DateTimeFormatter.ofPattern("mm:ss"));
    }
}
