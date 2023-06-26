package net.runelite.client.plugins.spoontob;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

@Singleton
public class MiscOverlay extends Overlay {
    private final Client client;

    private final SpoonTobPlugin plugin;

    private final SpoonTobConfig config;

    @Inject
    private MiscOverlay(Client client, SpoonTobPlugin plugin, SpoonTobConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.lootReminder() != SpoonTobConfig.lootReminderMode.OFF && this.plugin.bankLootChest != null && this.client.getLocalPlayer() != null) {
            Color raveColor = this.plugin.raveUtils.getColor(this.plugin.bankLootChest.hashCode(), true);
            if (this.plugin.bankLootChest.getRenderable().getModel().getModelHeight() == 119) {
                if (this.config.lootReminder() == SpoonTobConfig.lootReminderMode.DUMBER || this.config.lootReminder() == SpoonTobConfig.lootReminderMode.DUMBEST || this.config
                        .lootReminder() == SpoonTobConfig.lootReminderMode.DUMBEREST) {
                    if (!this.client.hasHintArrow())
                        this.client.setHintArrow(this.plugin.bankLootChest.getWorldLocation());
                    if (this.config.lootReminder() == SpoonTobConfig.lootReminderMode.DUMBEST) {
                        graphics.setColor(new Color(raveColor.getRed(), raveColor.getGreen(), raveColor.getBlue(), this.config.lootReminderColor().getAlpha()));
                    } else if (this.config.lootReminder() == SpoonTobConfig.lootReminderMode.DUMBEREST) {
                        graphics.setColor(new Color(this.plugin.raveBankChestColor.getRed(), this.plugin.raveBankChestColor.getBlue(), this.plugin.raveBankChestColor.getGreen(), this.config.lootReminderColor().getAlpha()));
                    } else {
                        graphics.setColor(this.config.lootReminderColor());
                    }
                    if (this.plugin.bankLootChest.getConvexHull() != null)
                        graphics.fill(this.plugin.bankLootChest.getConvexHull());
                } else if (this.config.lootReminder() == SpoonTobConfig.lootReminderMode.DUMB) {
                    graphics.setColor(this.config.lootReminderColor());
                    if (this.plugin.bankLootChest.getConvexHull() != null)
                        graphics.fill(this.plugin.bankLootChest.getConvexHull());
                }
            } else {
                this.plugin.bankLootChest = null;
                this.client.clearHintArrow();
            }
        }
        return null;
    }
}
