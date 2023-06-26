package net.runelite.client.plugins.spoontob.util;

import java.awt.Color;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;

public class RaveUtils {
    @Inject
    private Client client;

    @Inject
    private SpoonTobConfig config;

    public Color getColor(int hashCode, boolean syncColor) {
        return getColor(hashCode, this.client.getGameCycle(), syncColor, this.config.raveSpeed());
    }

    public static Color getColor(int hashCode, int gameCycle, boolean syncColor, int colorSpeed) {
        if (syncColor)
            hashCode = 0;
        int clientTicks = colorSpeed / 20;
        return Color.getHSBColor(((hashCode + gameCycle) % clientTicks) / clientTicks, 1.0F, 1.0F);
    }

    public Color getColor(int hashCode, int gameCycle, boolean syncColor) {
        if (syncColor)
            hashCode = 0;
        int clientTicks = this.config.raveSpeed() / 20;
        return Color.getHSBColor(((hashCode + gameCycle) % clientTicks) / clientTicks, 1.0F, 1.0F);
    }

    public int colorToRs2hsb(Color color) {
        float[] hsbVals = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsbVals[2] = hsbVals[2] - Math.min(hsbVals[1], hsbVals[2] / 2.0F);
        int encode_hue = (int)(hsbVals[0] * 63.0F);
        int encode_saturation = (int)(hsbVals[1] * 7.0F);
        int encode_brightness = (int)(hsbVals[2] * 127.0F);
        return (encode_hue << 10) + (encode_saturation << 7) + encode_brightness;
    }

    public void recolorAllFaces(Model model, Color color) {
        if (model == null || color == null)
            return;
        int rs2hsb = colorToRs2hsb(color);
        int[] faceColors1 = model.getFaceColors1();
        int[] faceColors2 = model.getFaceColors2();
        int[] faceColors3 = model.getFaceColors3();
        int i;
        for (i = 0; i < faceColors1.length; i++)
            faceColors1[i] = rs2hsb;
        for (i = 0; i < faceColors2.length; i++)
            faceColors2[i] = rs2hsb;
        for (i = 0; i < faceColors3.length; i++)
            faceColors3[i] = rs2hsb;
    }
}
