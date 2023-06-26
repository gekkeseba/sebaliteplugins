package net.runelite.client.plugins.partydefencetracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("defencetracker")
public interface DefenceTrackerConfig extends Config {
    @Range(max = 50, min = 1)
    @ConfigItem(name = "Low Defence Threshold", keyName = "lowDef", description = "Sets when you want the defence to appear as yellow (low defence).", position = 1)
    default int lowDef() {
        return 10;
    }

    @ConfigItem(keyName = "vulnerability", name = "Show Vulnerability", description = "Displays an infobox when you successfully land vulnerability", position = 2)
    default boolean vulnerability() {
        return true;
    }

    @ConfigItem(keyName = "redKeris", name = "Show Red Keris", description = "Displays an infobox when you successfully land a Red Keris (Corruption) special attack", position = 3)
    default boolean redKeris() {
        return true;
    }
}
