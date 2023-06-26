package net.runelite.client.plugins.aoewarnings;

import java.util.HashMap;
import java.util.Map;

public enum AoeProjectileInfo {
    LIZARDMAN_SHAMAN_AOE(1293, 5),
    CRAZY_ARCHAEOLOGIST_AOE(1260, 3),
    ICE_DEMON_RANGED_AOE(1324, 3),
    ICE_DEMON_ICE_BARRAGE_AOE(366, 3),
    VASA_AWAKEN_AOE(1327, 3),
    VASA_RANGED_AOE(1329, 3),
    TEKTON_METEOR_AOE(660, 3),
    VORKATH_BOMB(1481, 3),
    VORKATH_POISON_POOL(1483, 1),
    VORKATH_SPAWN(1484, 1),
    VORKATH_TICK_FIRE(1482, 1),
    GALVEK_MINE(1495, 3),
    GALVEK_BOMB(1491, 3),
    DAWN_FREEZE(1445, 3),
    DUSK_CEILING(1435, 3),
    VETION_LIGHTNING(280, 1),
    CHAOS_FANATIC(551, 1),
    JUSTICIAR_LEASH(1515, 1),
    MAGE_ARENA_BOSS_FREEZE(368, 1),
    CORPOREAL_BEAST(315, 1),
    CORPOREAL_BEAST_DARK_CORE(319, 3),
    OLM_FALLING_CRYSTAL(1357, 3),
    OLM_BURNING(1349, 1),
    OLM_FALLING_CRYSTAL_TRAIL(1352, 1),
    OLM_ACID_TRAIL(1354, 1),
    OLM_FIRE_LINE(1347, 1),
    WINTERTODT_SNOW_FALL(1310, 3),
    XARPUS_POISON_AOE(1555, 1),
    ADDY_DRAG_POISON(1486, 1),
    DRAKE_BREATH(1637, 1),
    CERB_FIRE(1247, 2),
    DEMONIC_GORILLA_BOULDER(856, 1),
    MARBLE_GARGOYLE_AOE(1453, 1),
    VERZIK_PURPLE_SPAWN(1586, 3),
    VERZIK_P1_ROCKS(1435, 1);

    private static final Map<Integer, AoeProjectileInfo> map = new HashMap();
    private final int id;
    private final int aoeSize;

    private AoeProjectileInfo(int id, int aoeSize) {
        this.id = id;
        this.aoeSize = aoeSize;
    }

    public static AoeProjectileInfo getById(int id) {
        return (AoeProjectileInfo)map.get(id);
    }

    public int getId() {
        return this.id;
    }

    public int getAoeSize() {
        return this.aoeSize;
    }

    static {
        AoeProjectileInfo[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            AoeProjectileInfo aoe = var0[var2];
            map.put(aoe.id, aoe);
        }

    }
}

