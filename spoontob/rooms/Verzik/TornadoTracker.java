package net.runelite.client.plugins.spoontob.rooms.Verzik;

import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;

public class TornadoTracker {
    private NPC npc;

    private WorldPoint prevLoc;

    NPC getNpc() {
        return this.npc;
    }

    WorldPoint getPrevLoc() {
        return this.prevLoc;
    }

    void setPrevLoc(WorldPoint prevLoc) {
        this.prevLoc = prevLoc;
    }

    TornadoTracker(NPC npc) {
        this.npc = npc;
        this.prevLoc = null;
    }

    public int checkMovement(WorldPoint playerWp, WorldPoint nadoWp) {
        if (this.prevLoc == null || nadoWp == null || this.prevLoc.distanceTo(nadoWp) == 0)
            return -1;
        return playerWp.distanceTo(nadoWp) - playerWp.distanceTo(this.prevLoc);
    }
}
