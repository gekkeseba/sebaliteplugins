package net.runelite.client.plugins.partydefencetracker;


import java.awt.Color;
import java.awt.image.BufferedImage;
import net.runelite.client.ui.overlay.infobox.InfoBox;

public class RedKerisInfoBox extends InfoBox {
    private DefenceTrackerPlugin plugin;

    RedKerisInfoBox(BufferedImage image, DefenceTrackerPlugin plugin) {
        super(image, plugin);
        this.plugin = plugin;
    }

    public String getText() {
        return String.valueOf(this.plugin.getRedKerisTicks());
    }

    public Color getTextColor() {
        return (this.plugin.getRedKerisTicks() <= 3) ? Color.RED : Color.WHITE;
    }
}
