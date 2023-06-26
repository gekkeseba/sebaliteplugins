package net.runelite.client.plugins.spoontob.rooms.Maiden;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

public class MaidenMaxHitOverlay extends OverlayPanel {
    private final Maiden maiden;

    private final SpoonTobConfig config;

    @Inject
    private MaidenMaxHitOverlay(Maiden maiden, SpoonTobConfig config) {
        this.maiden = maiden;
        this.config = config;
        setPriority(OverlayPriority.HIGH);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.panelComponent.setPreferredSize(new Dimension(85, 0));
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.maidenMaxHitOverlay() != SpoonTobConfig.MaidenMaxHit.OFF && this.maiden.getMaidenNPC() != null) {
            int noPrayerMaxHit = (int)Math.floor(this.maiden.getMaxHit());
            int prayerMaxHit = noPrayerMaxHit / 2;
            int elyMaxHit = prayerMaxHit - (int)Math.floor(prayerMaxHit * 0.25D);
            LineComponent reg = LineComponent.builder().left("Max Hit:").leftColor(Color.WHITE).right(Integer.toString(prayerMaxHit)).rightColor(Color.GREEN).build();
            LineComponent ely = LineComponent.builder().left("Ely Max Hit:").leftColor(Color.WHITE).right(Integer.toString(elyMaxHit)).rightColor(Color.GREEN).build();
            switch (this.config.maidenMaxHitOverlay()) {
                case REGULAR:
                    this.panelComponent.getChildren().add(reg);
                    return super.render(graphics);
                case ELY:
                    this.panelComponent.getChildren().add(ely);
                    return super.render(graphics);
                case BOTH:
                    this.panelComponent.getChildren().add(reg);
                    this.panelComponent.getChildren().add(ely);
                    return super.render(graphics);
            }
            throw new IllegalStateException("Invalid 'maidenMaxHit' config state -> state: " + this.config.maidenMaxHitOverlay().getName());
        }
        return null;
    }
}
