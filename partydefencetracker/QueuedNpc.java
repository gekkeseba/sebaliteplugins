package net.runelite.client.plugins.partydefencetracker;

import java.util.ArrayList;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;

public class QueuedNpc {
    public int index;

    public ArrayList<QueuedSpec> queuedSpecs;

    int getIndex() {
        return this.index;
    }

    ArrayList<QueuedSpec> getQueuedSpecs() {
        return this.queuedSpecs;
    }

    QueuedNpc(int index) {
        this.index = index;
        this.queuedSpecs = new ArrayList<>();
    }

    public static class QueuedSpec {
        public SpecialWeapon weapon;

        public int hit;

        QueuedSpec(SpecialWeapon weapon, int hit) {
            this.weapon = weapon;
            this.hit = hit;
        }
    }
}
