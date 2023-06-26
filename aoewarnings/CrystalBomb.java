package net.runelite.client.plugins.aoewarnings;

import java.time.Instant;
import net.runelite.api.GameObject;
import net.runelite.api.coords.WorldPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CrystalBomb {
    private static final Logger log = LoggerFactory.getLogger(CrystalBomb.class);
    private GameObject gameObject;
    private Instant plantedOn;
    private Instant lastClockUpdate;
    private int objectId;
    private int tickStarted;
    private WorldPoint worldLocation;

    CrystalBomb(GameObject gameObject, int startTick) {
        this.gameObject = gameObject;
        this.objectId = gameObject.getId();
        this.plantedOn = Instant.now();
        this.worldLocation = gameObject.getWorldLocation();
        this.tickStarted = startTick;
    }

    void bombClockUpdate() {
        this.lastClockUpdate = Instant.now();
    }

    GameObject getGameObject() {
        return this.gameObject;
    }

    Instant getPlantedOn() {
        return this.plantedOn;
    }

    Instant getLastClockUpdate() {
        return this.lastClockUpdate;
    }

    int getObjectId() {
        return this.objectId;
    }

    int getTickStarted() {
        return this.tickStarted;
    }

    WorldPoint getWorldLocation() {
        return this.worldLocation;
    }
}
