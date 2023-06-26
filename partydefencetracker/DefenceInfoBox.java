package net.runelite.client.plugins.partydefencetracker;


import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class DefenceInfoBox extends InfoBox {
    @Inject
    private final DefenceTrackerConfig config;

    private long count;

    public String toString() {
        return "DefenceInfoBox(config=" + this.config + ", count=" + getCount() + ")";
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public DefenceInfoBox(BufferedImage image, Plugin plugin, long count, DefenceTrackerConfig config) {
        super(image, plugin);
        this.count = count;
        this.config = config;
    }

    public String getText() {
        return Long.toString(getCount());
    }

    public Color getTextColor() {
        if (this.count == 0L)
            return Color.GREEN;
        if (this.count >= 1L && this.count <= this.config.lowDef())
            return Color.YELLOW;
        return Color.WHITE;
    }
}
