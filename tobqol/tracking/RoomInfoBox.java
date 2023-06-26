/*
 * Copyright (c) 2022, Damen <gh: damencs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:

 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.

 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.tobqol.tracking;

import net.runelite.client.plugins.tobqol.TheatreQOLConfig;
import net.runelite.client.plugins.tobqol.TheatreQOLPlugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RoomInfoBox extends InfoBox
{
    private final TheatreQOLConfig config;
    private final String room;
    private final String time;
    private final String tooltip;

    public RoomInfoBox(
            BufferedImage image,
            TheatreQOLPlugin plugin,
            TheatreQOLConfig config,
            String room,
            String time,
            String tooltip
    )
    {
        super(image, plugin);

        this.config = config;
        this.room = room;
        this.time = time;
        this.tooltip = tooltip;

        setPriority(InfoBoxPriority.LOW);
    }

    @Override
    public String getName()
    {
        return room;
    }

    @Override
    public String getText()
    {
        return getTime(true);
    }

    @Override
    public Color getTextColor()
    {
        return Color.GREEN;
    }

    @Override
    public String getTooltip()
    {
        return tooltip;
    }

    @Override
    public boolean render()
    {
        return config.displayRoomTimes().isInfobox();
    }

    private String getTime(boolean simple)
    {
        return simple ? StringUtils.substringBefore(time, ".") : time;
    }
}
