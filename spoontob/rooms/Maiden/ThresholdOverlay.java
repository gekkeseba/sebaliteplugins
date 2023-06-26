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

public class ThresholdOverlay extends OverlayPanel {
    private final Maiden maiden;
    private final SpoonTobConfig config;

    @Inject
    private ThresholdOverlay(Maiden maiden, SpoonTobConfig config) {
        this.maiden = maiden;
        this.config = config;
        this.setPriority(OverlayPriority.HIGH);
        this.setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        this.panelComponent.setPreferredSize(new Dimension(100, 0));
    }

    public Dimension render(Graphics2D graphics) {
        if (this.config.maidenProcThreshold() && this.maiden.getMaidenNPC() != null && this.maiden.getMaidenNPC().getId() != 8363) {
            if (this.maiden.getRealMaidenHp() >= this.maiden.getThresholdHp()) {
                String maidenThresholdStr = Integer.toString(this.maiden.getRealMaidenHp() - this.maiden.getThresholdHp());
                this.panelComponent.getChildren().add(LineComponent.builder().left("DMG Left:").leftColor(Color.WHITE).right(maidenThresholdStr).rightColor(Color.GREEN).build());
            }

            return super.render(graphics);
        } else {
            return null;
        }
    }
}
