package net.runelite.client.plugins.spoontob.rooms.Nylocas;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Scene;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.util.TheatreInputListener;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.plugins.spoontob.util.WeaponMap;
import net.runelite.client.plugins.spoontob.util.WeaponStyle;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.InfoBoxComponent;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nylocas extends Room {
    private static final Logger log = LoggerFactory.getLogger(SpoonTobPlugin.class);

    @Inject
    private SkillIconManager skillIconManager;

    @Inject
    private MouseManager mouseManager;

    @Inject
    private TheatreInputListener theatreInputListener;

    @Inject
    private Client client;

    @Inject
    private NylocasOverlay nylocasOverlay;

    @Inject
    public NylocasAliveCounterOverlay nylocasAliveCounterOverlay;

    @Inject
    private NyloTimer nyloTimer;

    @Inject
    private NyloWaveSpawnInfobox waveSpawnInfobox;

    private static final int NPCID_NYLOCAS_PILLAR = 8358;

    private static final int NPCID_NYLOCAS_SM_PILLAR = 10790;

    private static final int NPCID_NYLOCAS_HM_PILLAR = 10811;

    private static final int NYLO_MAP_REGION = 13122;

    private static final int BLOAT_MAP_REGION = 13125;

    private static final String MAGE_NYLO = "Nylocas Hagios";

    private static final String RANGE_NYLO = "Nylocas Toxobolos";

    private static final String MELEE_NYLO = "Nylocas Ischyros";

    private static final String BOSS_NYLO = "Nylocas Vasilias";

    private static final String DEMIBOSS_NYLO = "Nylocas Prinkipas";

    protected static final Set<Integer> NYLO_BOSS_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8355), Integer.valueOf(8356), Integer.valueOf(8357),
            Integer.valueOf(10787), Integer.valueOf(10788), Integer.valueOf(10789), Integer.valueOf(10808), Integer.valueOf(10809), Integer.valueOf(10810) );

    protected static final Set<Integer> NYLO_DEMI_BOSS_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(10804), Integer.valueOf(10805), Integer.valueOf(10806));

    protected static final Set<Integer> MELEE_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8342), Integer.valueOf(8345), Integer.valueOf(8348), Integer.valueOf(8351),
            Integer.valueOf(10774), Integer.valueOf(10777), Integer.valueOf(10780), Integer.valueOf(10783),
                    Integer.valueOf(10791), Integer.valueOf(10794), Integer.valueOf(10797), Integer.valueOf(10800) );

    protected static final Set<Integer> RANGE_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8343), Integer.valueOf(8346), Integer.valueOf(8349), Integer.valueOf(8352),
            Integer.valueOf(10775), Integer.valueOf(10778), Integer.valueOf(10781), Integer.valueOf(10784),
                    Integer.valueOf(10792), Integer.valueOf(10795), Integer.valueOf(10798), Integer.valueOf(10801) );

    protected static final Set<Integer> MAGIC_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8344), Integer.valueOf(8347), Integer.valueOf(8350), Integer.valueOf(8353),
            Integer.valueOf(10776), Integer.valueOf(10779), Integer.valueOf(10782), Integer.valueOf(10785),
                    Integer.valueOf(10793), Integer.valueOf(10796), Integer.valueOf(10799), Integer.valueOf(10802) );

    protected static final Set<Integer> TRIDENT_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(22323), Integer.valueOf(25731), Integer.valueOf(22288), Integer.valueOf(11907),
            Integer.valueOf(11905), Integer.valueOf(22292), Integer.valueOf(12899) );

    public static Runnable getWave31Callback() {
        return wave31Callback;
    }

    public static void setWave31Callback(Runnable wave31Callback) {
        Nylocas.wave31Callback = wave31Callback;
    }

    private static Runnable wave31Callback = null;

    public static Runnable getEndOfWavesCallback() {
        return endOfWavesCallback;
    }

    public static void setEndOfWavesCallback(Runnable endOfWavesCallback) {
        Nylocas.endOfWavesCallback = endOfWavesCallback;
    }

    private static Runnable endOfWavesCallback = null;

    private boolean nyloActive;

    public boolean isNyloActive() {
        return this.nyloActive;
    }

    public int nyloWave = 0;

    private int varbit6447 = -1;

    private Instant nyloWaveStart;

    private NyloSelectionManager nyloSelectionManager;

    public Instant getNyloWaveStart() {
        return this.nyloWaveStart;
    }

    public NyloSelectionManager getNyloSelectionManager() {
        return this.nyloSelectionManager;
    }

    private HashMap<NPC, Integer> nylocasPillars = new HashMap<>();

    public HashMap<NPC, Integer> getNylocasPillars() {
        return this.nylocasPillars;
    }

    public ArrayList<NyloInfo> nylocasNpcs = new ArrayList<>();

    private HashSet<NPC> aggressiveNylocas = new HashSet<>();

    public HashSet<NPC> getAggressiveNylocas() {
        return this.aggressiveNylocas;
    }

    private HashMap<NyloNPC, NPC> currentWave = new HashMap<>();

    private int ticksSinceLastWave = 0;

    public int instanceTimer = 0;

    public int getInstanceTimer() {
        return this.instanceTimer;
    }

    private boolean isInstanceTimerRunning = false;

    public boolean isInstanceTimerRunning() {
        return this.isInstanceTimerRunning;
    }

    private boolean nextInstance = true;

    private int rangeBoss = 0;

    private int mageBoss = 0;

    private int meleeBoss = 0;

    private int rangeSplits = 0;

    private int mageSplits = 0;

    private int meleeSplits = 0;

    private int preRangeSplits = 0;

    private int preMageSplits = 0;

    private int preMeleeSplits = 0;

    private int postRangeSplits = 0;

    private int postMageSplits = 0;

    private int postMeleeSplits = 0;

    private int bossChangeTicks;

    private int lastBossId;

    private NPC nylocasBoss;

    private boolean nyloBossAlive;

    public int getBossChangeTicks() {
        return this.bossChangeTicks;
    }

    public NPC getNylocasBoss() {
        return this.nylocasBoss;
    }

    public int weaponId = 0;

    private static final Set<Point> spawnTiles = (Set<Point>)ImmutableSet.of(new Point(17, 24), new Point(17, 25), new Point(31, 9), new Point(32, 9), new Point(46, 24), new Point(46, 25));

    private final Map<NPC, Integer> splitsMap = new HashMap<>();

    public Map<NPC, Integer> getSplitsMap() {
        return this.splitsMap;
    }

    private final Set<NPC> bigNylos = new HashSet<>();

    public boolean showHint;

    public final ArrayList<Color> meleeNyloRaveColors = new ArrayList<>();

    public final ArrayList<Color> rangeNyloRaveColors = new ArrayList<>();

    public final ArrayList<Color> mageNyloRaveColors = new ArrayList<>();

    public String tobMode = "";

    public boolean minibossAlive = false;

    public NPC nyloMiniboss = null;

    public String nyloBossStyle = "";

    public int logTicks = 0;

    public int waveSpawnTicks = 0;

    public boolean stalledWave = false;

    private boolean mirrorMode;

    private boolean setAlive;

    private WeaponStyle weaponStyle;

    private boolean skipTickCheck = false;

    @Inject
    protected Nylocas(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
    }

    public void init() {
        InfoBoxComponent box = new InfoBoxComponent();
        box.setImage(this.skillIconManager.getSkillImage(Skill.ATTACK));
        NyloSelectionBox nyloMeleeOverlay = new NyloSelectionBox(box);
        nyloMeleeOverlay.setSelected(this.config.getHighlightMeleeNylo());
        box = new InfoBoxComponent();
        box.setImage(this.skillIconManager.getSkillImage(Skill.MAGIC));
        NyloSelectionBox nyloMageOverlay = new NyloSelectionBox(box);
        nyloMageOverlay.setSelected(this.config.getHighlightMageNylo());
        box = new InfoBoxComponent();
        box.setImage(this.skillIconManager.getSkillImage(Skill.RANGED));
        NyloSelectionBox nyloRangeOverlay = new NyloSelectionBox(box);
        nyloRangeOverlay.setSelected(this.config.getHighlightRangeNylo());
        this.nyloSelectionManager = new NyloSelectionManager(nyloMeleeOverlay, nyloMageOverlay, nyloRangeOverlay);
        this.nyloSelectionManager.setHidden(!this.config.nyloOverlay());
        this.nylocasAliveCounterOverlay.setHidden(!this.config.nyloAlivePanel());
        this.nylocasAliveCounterOverlay.setNyloAlive(0);
        this.nylocasAliveCounterOverlay.setMaxNyloAlive(12);
        this.nyloBossAlive = false;
        this.tobMode = "";
        this.minibossAlive = false;
        this.nyloMiniboss = null;
        this.nyloBossStyle = "";
        this.waveSpawnTicks = 0;
        this.stalledWave = false;
    }

    private void startupNyloOverlay() {
        this.mouseManager.registerMouseListener((MouseListener)this.theatreInputListener);
        if (this.nyloSelectionManager != null) {
            this.overlayManager.add(this.nyloSelectionManager);
            this.nyloSelectionManager.setHidden(!this.config.nyloOverlay());
        }
        if (this.nylocasAliveCounterOverlay != null) {
            this.overlayManager.add((Overlay)this.nylocasAliveCounterOverlay);
            this.nylocasAliveCounterOverlay.setHidden(!this.config.nyloAlivePanel());
        }
    }

    private void shutdownNyloOverlay() {
        this.mouseManager.unregisterMouseListener((MouseListener)this.theatreInputListener);
        if (this.nyloSelectionManager != null) {
            this.overlayManager.remove(this.nyloSelectionManager);
            this.nyloSelectionManager.setHidden(true);
        }
        if (this.nylocasAliveCounterOverlay != null) {
            this.overlayManager.remove((Overlay)this.nylocasAliveCounterOverlay);
            this.nylocasAliveCounterOverlay.setHidden(true);
        }
    }

    public void load() {
        this.overlayManager.add((Overlay)this.nylocasOverlay);
        this.overlayManager.add((Overlay)this.nyloTimer);
        this.overlayManager.add((Overlay)this.waveSpawnInfobox);
        this.bossChangeTicks = -1;
        this.lastBossId = -1;
        this.weaponStyle = null;
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.nylocasOverlay);
        this.overlayManager.remove((Overlay)this.nyloTimer);
        this.overlayManager.remove((Overlay)this.waveSpawnInfobox);
        shutdownNyloOverlay();
        this.nyloBossAlive = false;
        this.nyloWaveStart = null;
        this.nyloActive = false;
        this.tobMode = "";
        this.minibossAlive = false;
        this.nyloBossStyle = "";
        this.logTicks = 0;
        this.waveSpawnTicks = 0;
        this.stalledWave = false;
        this.weaponStyle = null;
        this.splitsMap.clear();
        this.bigNylos.clear();
    }

    private void resetNylo() {
        this.nyloBossAlive = false;
        this.nylocasPillars.clear();
        this.nylocasNpcs.clear();
        this.aggressiveNylocas.clear();
        setNyloWave(0);
        this.currentWave.clear();
        this.bossChangeTicks = -1;
        this.lastBossId = -1;
        this.nylocasBoss = null;
        this.weaponId = 0;
        this.weaponStyle = null;
        this.splitsMap.clear();
        this.bigNylos.clear();
        this.tobMode = "";
        this.minibossAlive = false;
        this.nyloMiniboss = null;
        this.nyloBossStyle = "";
        this.logTicks = 0;
        this.waveSpawnTicks = 0;
        this.stalledWave = false;
    }

    private void setNyloWave(int wave) {
        this.nyloWave = wave;
        this.nylocasAliveCounterOverlay.setWave(wave);
        if (wave >= 3)
            this.isInstanceTimerRunning = false;
        if (wave != 0)
            switch (this.tobMode) {
                case "hard":
                    this.ticksSinceLastWave = ((NylocasWave)NylocasWave.hmWaves.get(Integer.valueOf(wave))).getWaveDelay();
                    break;
                case "story":
                    this.ticksSinceLastWave = ((NylocasWave)NylocasWave.smWaves.get(Integer.valueOf(wave))).getWaveDelay();
                    break;
                case "normal":
                    this.ticksSinceLastWave = ((NylocasWave)NylocasWave.waves.get(Integer.valueOf(wave))).getWaveDelay();
                    break;
            }
        if (wave >= 20 && this.nylocasAliveCounterOverlay.getMaxNyloAlive() != 24)
            this.nylocasAliveCounterOverlay.setMaxNyloAlive(24);
        if (wave < 20 && this.nylocasAliveCounterOverlay.getMaxNyloAlive() != 12)
            this.nylocasAliveCounterOverlay.setMaxNyloAlive(12);
        if (wave == 31 && wave31Callback != null)
            wave31Callback.run();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged change) {
        if (change.getKey().equals("nyloOverlay")) {
            this.nyloSelectionManager.setHidden(!this.config.nyloOverlay());
        } else if (change.getKey().equals("nyloAliveCounter")) {
            this.nylocasAliveCounterOverlay.setHidden(!this.config.nyloAlivePanel());
        } else if (change.getKey().equals("showLowestPillar") && !this.config.showLowestPillar()) {
            this.client.clearHintArrow();
        } else if (change.getKey().equals("hidePillars")) {
            this.plugin.refreshScene();
            if (this.config.hidePillars() != SpoonTobConfig.hidePillarsMode.PILLARS)
                if (this.config.hidePillars() == SpoonTobConfig.hidePillarsMode.CLEAN);
            if (this.config.hideEggs());
        } else if (change.getKey().equals("hideEggs")) {
            this.plugin.refreshScene();
            if (this.config.hideEggs());
            if (this.config.hidePillars() != SpoonTobConfig.hidePillarsMode.PILLARS)
                if (this.config.hidePillars() == SpoonTobConfig.hidePillarsMode.CLEAN);
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        int id = npc.getId();
        switch (npc.getId()) {
            case 8342:
            case 8343:
            case 8344:
            case 8345:
            case 8346:
            case 8347:
            case 8348:
            case 8349:
            case 8350:
            case 8351:
            case 8352:
            case 8353:
            case 10774:
            case 10775:
            case 10776:
            case 10777:
            case 10778:
            case 10779:
            case 10780:
            case 10781:
            case 10782:
            case 10783:
            case 10784:
            case 10785:
            case 10791:
            case 10792:
            case 10793:
            case 10794:
            case 10795:
            case 10796:
            case 10797:
            case 10798:
            case 10799:
            case 10800:
            case 10801:
            case 10802:
            case 10803:
            case 10804:
            case 10805:
            case 10806:
                if (this.nyloActive) {
                    if (npc.getId() == 10804) {
                        this.minibossAlive = true;
                        this.nyloMiniboss = npc;
                        this.bossChangeTicks = 10;
                    } else {
                        this.nylocasNpcs.add(new NyloInfo(npc));
                    }
                    if (this.minibossAlive) {
                        this.nylocasAliveCounterOverlay.setNyloAlive(this.nylocasNpcs.size() + 3);
                    } else {
                        this.nylocasAliveCounterOverlay.setNyloAlive(this.nylocasNpcs.size());
                    }
                    NyloNPC nyloNPC = matchNpc(npc);
                    if (nyloNPC != null) {
                        this.currentWave.put(nyloNPC, npc);
                        if (this.currentWave.size() > 2)
                            matchWave();
                    }
                }
                this.setAlive = true;
                break;
            case 8354:
            case 8355:
            case 8356:
            case 8357:
            case 10786:
            case 10787:
            case 10788:
            case 10789:
            case 10807:
            case 10808:
            case 10809:
            case 10810:
                this.showHint = false;
                this.isInstanceTimerRunning = false;
                this.nyloBossStyle = "melee";
                this.client.clearHintArrow();
                this.nyloBossAlive = true;
                this.lastBossId = id;
                this.nylocasBoss = npc;
                this.meleeBoss = 0;
                this.mageBoss = 0;
                this.rangeBoss = 0;
                if (npc.getId() == 8355 || npc.getId() == 10787 || npc.getId() == 10808) {
                    if (npc.getId() == 10787) {
                        this.bossChangeTicks = 15;
                    } else {
                        this.bossChangeTicks = 10;
                    }
                    this.meleeBoss++;
                }
                break;
            case 8358:
            case 10790:
            case 10811:
                this.nyloActive = true;
                this.showHint = true;
                if (this.nylocasPillars.size() > 3)
                    this.nylocasPillars.clear();
                if (!this.nylocasPillars.containsKey(npc))
                    this.nylocasPillars.put(npc, Integer.valueOf(100));
                if (npc.getId() == 10811) {
                    this.tobMode = "hard";
                } else if (npc.getId() == 10790) {
                    this.tobMode = "story";
                } else {
                    this.tobMode = "normal";
                }
                this.mageSplits = 0;
                this.rangeSplits = 0;
                this.meleeSplits = 0;
                this.preRangeSplits = 0;
                this.preMageSplits = 0;
                this.preMeleeSplits = 0;
                this.postRangeSplits = 0;
                this.postMageSplits = 0;
                this.postMeleeSplits = 0;
                break;
        }
        if (this.nyloActive) {
            switch (id) {
                case 8345:
                case 8346:
                case 8347:
                case 10777:
                case 10778:
                case 10779:
                case 10794:
                case 10795:
                case 10796:
                    this.bigNylos.add(npc);
                    break;
            }
            WorldPoint worldPoint = WorldPoint.fromLocalInstance(this.client, npc.getLocalLocation());
            Point spawnLoc = new Point(worldPoint.getRegionX(), worldPoint.getRegionY());
            if (!spawnTiles.contains(spawnLoc) &&
                    npc.getName() != null)
                if (npc.getName().contains("Hagios") && (id == 8344 || id == 10776 || id == 10793)) {
                    this.mageSplits++;
                    if (this.nyloWave < 20) {
                        this.preMageSplits++;
                    } else {
                        this.postMageSplits++;
                    }
                } else if (npc.getName().contains("Toxobolos") && (id == 8343 || id == 10775 || id == 10792)) {
                    this.rangeSplits++;
                    if (this.nyloWave < 20) {
                        this.preRangeSplits++;
                    } else {
                        this.postRangeSplits++;
                    }
                } else if (npc.getName().contains("Ischyros") && (id == 8342 || id == 10774 || id == 10791)) {
                    this.meleeSplits++;
                    if (this.nyloWave < 20) {
                        this.preMeleeSplits++;
                    } else {
                        this.postMeleeSplits++;
                    }
                }
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        NPC npc = event.getNpc();
        int id = npc.getId();
        if (NYLO_BOSS_IDS.contains(Integer.valueOf(id)) || NYLO_DEMI_BOSS_IDS.contains(Integer.valueOf(id))) {
            if (id == 10787 || id == 10788 || id == 10789) {
                this.bossChangeTicks = 16;
            } else {
                this.bossChangeTicks = 11;
            }
            this.lastBossId = id;
            if (NYLO_DEMI_BOSS_IDS.contains(Integer.valueOf(id)))
                this.nyloMiniboss = npc;
        }
        if (id == 8355 || id == 10787 || id == 10808) {
            this.meleeBoss++;
            this.nyloBossStyle = "melee";
        } else if (id == 8356 || id == 10788 || id == 10809) {
            this.mageBoss++;
            this.nyloBossStyle = "mage";
        } else if (id == 8357 || id == 10789 || id == 10810) {
            this.rangeBoss++;
            this.nyloBossStyle = "range";
        }
    }

    private void matchWave() {
        HashSet<NyloNPC> potentialWave = null;
        Set<NyloNPC> currentWaveKeySet = this.currentWave.keySet();
        for (int wave = this.nyloWave + 1; wave <= 31; wave++) {
            boolean matched = true;
            switch (this.tobMode) {
                case "hard":
                    potentialWave = ((NylocasWave)NylocasWave.hmWaves.get(Integer.valueOf(wave))).getWaveData();
                    break;
                case "story":
                    potentialWave = ((NylocasWave)NylocasWave.smWaves.get(Integer.valueOf(wave))).getWaveData();
                    break;
                case "normal":
                    potentialWave = ((NylocasWave)NylocasWave.waves.get(Integer.valueOf(wave))).getWaveData();
                    break;
            }
            for (NyloNPC nyloNpc : potentialWave) {
                if (!currentWaveKeySet.contains(nyloNpc)) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                setNyloWave(wave);
                this.stalledWave = false;
                if (this.ticksSinceLastWave > 0) {
                    this.waveSpawnTicks = this.ticksSinceLastWave;
                } else {
                    this.waveSpawnTicks = 4;
                }
                for (NyloNPC nyloNPC : potentialWave) {
                    if (nyloNPC.isAggressive())
                        this.aggressiveNylocas.add(this.currentWave.get(nyloNPC));
                }
                this.currentWave.clear();
                return;
            }
        }
    }

    private NyloNPC matchNpc(NPC npc) {
        WorldPoint p = WorldPoint.fromLocalInstance(this.client, npc.getLocalLocation());
        Point point = new Point(p.getRegionX(), p.getRegionY());
        NylocasSpawnPoint spawnPoint = NylocasSpawnPoint.getLookupMap().get(point);
        if (spawnPoint == null)
            return null;
        NylocasType nylocasType = NylocasType.getLookupMap().get(Integer.valueOf(npc.getId()));
        if (nylocasType == null)
            return null;
        return new NyloNPC(nylocasType, spawnPoint);
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        int id = npc.getId();
        switch (id) {
            case 8342:
            case 8343:
            case 8344:
            case 8345:
            case 8346:
            case 8347:
            case 8348:
            case 8349:
            case 8350:
            case 8351:
            case 8352:
            case 8353:
            case 10774:
            case 10775:
            case 10776:
            case 10777:
            case 10778:
            case 10779:
            case 10780:
            case 10781:
            case 10782:
            case 10783:
            case 10784:
            case 10785:
            case 10791:
            case 10792:
            case 10793:
            case 10794:
            case 10795:
            case 10796:
            case 10797:
            case 10798:
            case 10799:
            case 10800:
            case 10801:
            case 10802:
            case 10804:
            case 10805:
            case 10806:
                if (this.nylocasNpcs.removeIf(n -> (n.nylo != null && n.nylo == npc)) || NYLO_DEMI_BOSS_IDS.contains(Integer.valueOf(id))) {
                    if (NYLO_DEMI_BOSS_IDS.contains(Integer.valueOf(id))) {
                        this.nyloMiniboss = null;
                        this.minibossAlive = false;
                        this.bossChangeTicks = -1;
                    }
                    if (this.minibossAlive) {
                        this.nylocasAliveCounterOverlay.setNyloAlive(this.nylocasNpcs.size() + 3);
                    } else {
                        this.nylocasAliveCounterOverlay.setNyloAlive(this.nylocasNpcs.size());
                    }
                }
                this.aggressiveNylocas.remove(npc);
                if (this.nyloWave == 31 && this.nylocasNpcs.size() == 0) {
                    if ((this.config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.WAVES || this.config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOTH) && this.config
                            .splitMsgTiming() == SpoonTobConfig.splitsMsgTiming.CLEANUP) {
                        if (this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.CAP || this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH)
                            this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Pre-cap splits: <col=00FFFF>" + this.preMageSplits + "</col> - <col=00FF00>" + this.preRangeSplits + "</col> - <col=ff0000>" + this.preMeleeSplits + "</col> Post-cap splits: <col=00FFFF>" + this.postMageSplits + "</col> - <col=00FF00>" + this.postRangeSplits + "</col> - <col=ff0000>" + this.postMeleeSplits, null);
                        if (this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.TOTAL || this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH)
                            this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Small splits: <col=00FFFF>" + this.mageSplits + "</col> - <col=00FF00>" + this.rangeSplits + "</col> - <col=ff0000>" + this.meleeSplits + "</col> ", null);
                    }
                    if (endOfWavesCallback != null)
                        endOfWavesCallback.run();
                }
                this.setAlive = false;
                break;
            case 8354:
            case 8355:
            case 8356:
            case 8357:
            case 10786:
            case 10787:
            case 10788:
            case 10789:
            case 10807:
            case 10808:
            case 10809:
            case 10810:
                this.nyloBossAlive = false;
                this.nylocasBoss = null;
                break;
            case 8358:
            case 10790:
            case 10811:
                if (this.nylocasPillars.containsKey(npc))
                    this.nylocasPillars.remove(npc);
                if (this.nylocasPillars.size() < 1) {
                    this.nyloWaveStart = null;
                    this.nyloActive = false;
                }
                break;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int[] varps = this.client.getVarps();
        int newVarbit6447 = this.client.getVarbitValue(varps, 6447);
        if (isInNyloRegion() && newVarbit6447 != 0 && newVarbit6447 != this.varbit6447) {
            this.nyloWaveStart = Instant.now();
            if (this.nylocasAliveCounterOverlay != null)
                this.nylocasAliveCounterOverlay.setNyloWaveStart(this.nyloWaveStart);
        }
        if (TheatreRegions.inRegion(this.client, TheatreRegions.NYLOCAS))
            this.nyloActive = (this.client.getVarbitValue(6447) != 0);
        this.varbit6447 = newVarbit6447;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            if (isInNyloRegion()) {
                startupNyloOverlay();
                if (this.config.hidePillars() == SpoonTobConfig.hidePillarsMode.PILLARS) {
                    removeGameObjectsFromScene((Set<Integer>)ImmutableSet.of(Integer.valueOf(32862)), 0);
                } else if (this.config.hidePillars() == SpoonTobConfig.hidePillarsMode.CLEAN) {
                    removeGameObjectsFromScene((Set<Integer>)ImmutableSet.of(Integer.valueOf(32862), Integer.valueOf(32876), Integer.valueOf(32899)), 0);
                }
                if (this.config.hideEggs())
                    removeGameObjectsFromScene((Set<Integer>)ImmutableSet.of(Integer.valueOf(32939), Integer.valueOf(32937), Integer.valueOf(2739), Integer.valueOf(32865)), 0);
            } else {
                if (!this.nyloSelectionManager.isHidden() || !this.nylocasAliveCounterOverlay.isHidden())
                    shutdownNyloOverlay();
                resetNylo();
                this.isInstanceTimerRunning = false;
            }
            this.nextInstance = true;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.nyloActive) {
            if (this.skipTickCheck) {
                this.skipTickCheck = false;
            } else {
                if (this.client.getLocalPlayer() == null || this.client.getLocalPlayer().getPlayerComposition() == null)
                    return;
                int equippedWeapon = ((Integer)ObjectUtils.defaultIfNull(Integer.valueOf(this.client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON)), Integer.valueOf(-1))).intValue();
                this.weaponStyle = (WeaponStyle)WeaponMap.StyleMap.get(Integer.valueOf(equippedWeapon));
            }
            if (this.waveSpawnTicks >= 0) {
                this.waveSpawnTicks--;
                if (this.waveSpawnTicks < 0 && this.nylocasAliveCounterOverlay.getNyloAlive() >= this.nylocasAliveCounterOverlay.getMaxNyloAlive()) {
                    this.waveSpawnTicks = 3;
                    this.stalledWave = true;
                }
            }
            this.meleeNyloRaveColors.clear();
            this.rangeNyloRaveColors.clear();
            this.mageNyloRaveColors.clear();
            for (int i = this.nylocasNpcs.size() - 1; i >= 0; i--) {
                NyloInfo ni = this.nylocasNpcs.get(i);
                ni.ticks--;
                if (ni.ticks < 0 || ni.nylo.isDead() || !ni.alive) {
                    this.nylocasNpcs.remove(ni);
                } else if (MELEE_IDS.contains(Integer.valueOf(ni.nylo.getId()))) {
                    this.meleeNyloRaveColors.add(Color.getHSBColor((new Random()).nextFloat(), 1.0F, 1.0F));
                } else if (RANGE_IDS.contains(Integer.valueOf(ni.nylo.getId()))) {
                    this.rangeNyloRaveColors.add(Color.getHSBColor((new Random()).nextFloat(), 1.0F, 1.0F));
                } else if (MAGIC_IDS.contains(Integer.valueOf(ni.nylo.getId()))) {
                    this.mageNyloRaveColors.add(Color.getHSBColor((new Random()).nextFloat(), 1.0F, 1.0F));
                }
            }
            for (NPC pillar : this.nylocasPillars.keySet()) {
                int healthPercent = pillar.getHealthRatio();
                if (healthPercent > -1)
                    this.nylocasPillars.replace(pillar, Integer.valueOf(healthPercent));
            }
            boolean foundPillar = false;
            for (NPC npc : this.client.getNpcs()) {
                if (npc.getId() == 8358 || npc.getId() == 10790 || npc.getId() == 10811) {
                    foundPillar = true;
                    break;
                }
            }
            NPC minNPC = null;
            int minHealth = 100;
            if (foundPillar) {
                for (NPC npc : this.nylocasPillars.keySet()) {
                    int health = (npc.getHealthRatio() > -1) ? npc.getHealthRatio() : ((Integer)this.nylocasPillars.get(npc)).intValue();
                    this.nylocasPillars.replace(npc, Integer.valueOf(health));
                    if (health < minHealth) {
                        minHealth = health;
                        minNPC = npc;
                    }
                }
                if (minNPC != null && this.config.showLowestPillar() && this.showHint)
                    this.client.setHintArrow(minNPC);
            } else {
                this.nylocasPillars.clear();
            }
            if ((this.instanceTimer + 1) % 4 == 1 && this.nyloWave < 31 && this.ticksSinceLastWave < 2 &&
                    this.config.nyloStallMessage() && this.nylocasAliveCounterOverlay.getNyloAlive() >= this.nylocasAliveCounterOverlay.getMaxNyloAlive())
                this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Stalled wave: <col=FF0000>" + this.nyloWave + " </col>Time:<col=FF0000> " + this.nylocasAliveCounterOverlay
                        .getFormattedTime() + " </col>Nylos alive:<col=FF0000> " + this.nylocasAliveCounterOverlay.getNyloAlive() + "/" + this.nylocasAliveCounterOverlay
                        .getMaxNyloAlive(), "", false);
            this.ticksSinceLastWave = Math.max(0, this.ticksSinceLastWave - 1);
            if (this.nylocasBoss != null && this.nyloBossAlive) {
                this.bossChangeTicks--;
                if (this.nylocasBoss.getId() != this.lastBossId) {
                    this.lastBossId = this.nylocasBoss.getId();
                    if (this.nylocasBoss.getId() == 10787 || this.nylocasBoss.getId() == 10788 || this.nylocasBoss.getId() == 10789) {
                        this.bossChangeTicks = 15;
                    } else {
                        this.bossChangeTicks = 10;
                    }
                }
            } else if (this.minibossAlive && this.nyloMiniboss != null) {
                this.bossChangeTicks--;
            }
            if (!this.splitsMap.isEmpty()) {
                this.splitsMap.values().removeIf(value -> (value.intValue() <= 1));
                this.splitsMap.replaceAll((key, value) -> Integer.valueOf(value.intValue() - 1));
            }
        }
        this.instanceTimer = (this.instanceTimer + 1) % 4;
    }

    @Subscribe
    protected void onClientTick(ClientTick event) {
        List<Player> players = this.client.getPlayers();
        for (Player player : players) {
            if (player.getWorldLocation() != null) {
                LocalPoint lp = player.getLocalLocation();
                WorldPoint wp = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(), 5, 33, 0);
                LocalPoint lp1 = LocalPoint.fromWorld(this.client, wp.getX(), wp.getY());
                if (lp1 != null) {
                    Point base = new Point(lp1.getSceneX(), lp1.getSceneY());
                    Point point = new Point(lp.getSceneX() - base.getX(), lp.getSceneY() - base.getY());
                    if (isInBloatRegion() && point.getX() == -1 && (point.getY() == -1 || point.getY() == -2 || point.getY() == -3) && this.nextInstance) {
                        this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Nylo instance timer started.", "");
                        this.instanceTimer = 3;
                        this.isInstanceTimerRunning = true;
                        this.nextInstance = false;
                    }
                }
            }
        }
    }

    @Subscribe
    private void onAnimationChanged(AnimationChanged event) {
        if (!this.bigNylos.isEmpty() && event.getActor() instanceof NPC) {
            NPC npc = (NPC)event.getActor();
            int anim = npc.getAnimation();
            if (this.bigNylos.contains(npc)) {
                if (anim == 8005 || anim == 7991 || anim == 7998) {
                    this.splitsMap.putIfAbsent(npc, Integer.valueOf(6));
                    this.bigNylos.remove(npc);
                }
                if (anim == 8006 || anim == 7992 || anim == 8000) {
                    this.splitsMap.putIfAbsent(npc, Integer.valueOf(4));
                    this.bigNylos.remove(npc);
                }
            }
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        String mes = event.getMessage();
        if (mes.contains("Wave 'The Nylocas'") && mes.contains("complete!<br>Duration: <col=ff0000>")) {
            if ((this.config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.WAVES || this.config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOTH) && this.config
                    .splitMsgTiming() == SpoonTobConfig.splitsMsgTiming.FINISHED) {
                if (this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.CAP || this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH)
                    this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Pre-cap splits: <col=00FFFF>" + this.preMageSplits + "</col> - <col=00FF00>" + this.preRangeSplits + "</col> - <col=ff0000>" + this.preMeleeSplits + "</col> Post-cap splits: <col=00FFFF>" + this.postMageSplits + "</col> - <col=00FF00>" + this.postRangeSplits + "</col> - <col=ff0000>" + this.postMeleeSplits, null);
                if (this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.TOTAL || this.config.smallSplitsType() == SpoonTobConfig.smallSplitsMode.BOTH)
                    this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Small splits: <col=00FFFF>" + this.mageSplits + "</col> - <col=00FF00>" + this.rangeSplits + "</col> - <col=ff0000>" + this.meleeSplits + "</col> ", null);
            }
            if (this.config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOSS || this.config.nyloSplitsMsg() == SpoonTobConfig.nyloSplitsMessage.BOTH)
                this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Boss phases: <col=00FFFF>" + this.mageBoss + "</col> - <col=00FF00>" + this.rangeBoss + "</col> - <col=ff0000>" + this.meleeBoss + "</col> ", null);
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equalsIgnoreCase("wield")) {
            WeaponStyle newStyle = (WeaponStyle)WeaponMap.StyleMap.get(Integer.valueOf(event.getItemId()));
            if (newStyle != null) {
                this.skipTickCheck = true;
                this.weaponStyle = newStyle;
            }
        } else if ((this.config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOSS || this.config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOTH) && this.nylocasBoss != null && event
                .getMenuTarget().contains("Nylocas Vasilias") && event.getMenuOption().equalsIgnoreCase("attack") && this.weaponStyle != null) {
            switch (this.weaponStyle) {
                case TRIDENTS:
                case MAGIC:
                    if (this.nylocasBoss.getId() != 8356 && this.nylocasBoss.getId() != 10788 && this.nylocasBoss
                            .getId() != 10809)
                        event.consume();
                    break;
                case MELEE:
                    if (this.nylocasBoss.getId() != 8355 && this.nylocasBoss.getId() != 10787 && this.nylocasBoss
                            .getId() != 10808)
                        event.consume();
                    break;
                case RANGE:
                    if (this.nylocasBoss.getId() != 8357 && this.nylocasBoss.getId() != 10789 && this.nylocasBoss
                            .getId() != 10810)
                        event.consume();
                    break;
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (this.nyloActive) {
            String target = event.getTarget();
            String option = event.getOption();
            MenuEntry nEntry = event.getMenuEntry();
            MenuEntry nbEntry = event.getMenuEntry();
            if (this.config.nyloRecolorMenu() && (option.equalsIgnoreCase("attack") || event.getType() == MenuAction.WIDGET_TARGET_ON_NPC.getId())) {
                MenuEntry[] entries = this.client.getMenuEntries();
                MenuEntry toEdit = entries[entries.length - 1];
                String strippedTarget = Text.removeTags(target);
                String timeAliveString = "";
                NPC npc = this.client.getCachedNPCs()[toEdit.getIdentifier()];
                if (npc != null && npc.getComposition() != null) {
                    boolean isBig = (npc.getComposition().getSize() > 1);
                    for (NyloInfo ni : this.nylocasNpcs) {
                        if (this.config.nyloTicksMenu() && ni.nylo == npc) {
                            if (this.config.nyloTimeAliveCountStyle() == SpoonTobConfig.nylotimealive.COUNTUP) {
                                int i = 52 - ni.ticks;
                                timeAliveString = ColorUtil.prependColorTag(" - " + i, new Color(255 * i / 52, 255 * (52 - i) / 52, 0));
                                break;
                            }
                            int timeAlive = ni.ticks;
                            timeAliveString = ColorUtil.prependColorTag(" - " + timeAlive, new Color(255 * (52 - timeAlive) / 52, 255 * timeAlive / 52, 0));
                            break;
                        }
                    }
                    if (strippedTarget.contains("Nylocas Hagios")) {
                        if (isBig) {
                            toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 190, 190)) + ColorUtil.prependColorTag(strippedTarget, new Color(0, 190, 190)));
                        } else {
                            toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 255, 255)) + ColorUtil.prependColorTag(strippedTarget, new Color(0, 255, 255)));
                        }
                    } else if (strippedTarget.contains("Nylocas Ischyros")) {
                        if (isBig) {
                            toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(190, 150, 150)) + ColorUtil.prependColorTag(strippedTarget, new Color(190, 150, 150)));
                        } else {
                            toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(255, 188, 188)) + ColorUtil.prependColorTag(strippedTarget, new Color(255, 188, 188)));
                        }
                    } else if (strippedTarget.contains("Nylocas Toxobolos")) {
                        if (isBig) {
                            toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 190, 0)) + ColorUtil.prependColorTag(strippedTarget, new Color(0, 190, 0)));
                        } else {
                            toEdit.setTarget(ColorUtil.prependColorTag(strippedTarget, new Color(0, 255, 0)) + ColorUtil.prependColorTag(strippedTarget, new Color(0, 255, 0)));
                        }
                    }
                    this.client.setMenuEntries(entries);
                }
            }
            if ((this.config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.WAVES || this.config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOTH) && option
                    .equalsIgnoreCase("attack") && this.weaponStyle != null)
                switch (this.weaponStyle) {
                    case TRIDENTS:
                        if (target.contains("Nylocas Ischyros") || target.contains("Nylocas Toxobolos"))
                            nEntry.setDeprioritized(true);
                        break;
                    case MAGIC:
                        if (this.config.manualCast()) {
                            if (target.contains("Nylocas Ischyros") || target.contains("Nylocas Toxobolos") || target.contains("Nylocas Hagios"))
                                nEntry.setDeprioritized(true);
                            break;
                        }
                        if (target.contains("Nylocas Ischyros") || target.contains("Nylocas Toxobolos"))
                            nEntry.setDeprioritized(true);
                        break;
                    case MELEE:
                        if (target.contains("Nylocas Toxobolos") || target.contains("Nylocas Hagios"))
                            nEntry.setDeprioritized(true);
                        break;
                    case RANGE:
                        if (target.contains("Nylocas Ischyros") || target.contains("Nylocas Hagios"))
                            nEntry.setDeprioritized(true);
                        break;
                    case CHINS:
                        if (!this.config.ignoreChins() && (target.contains("Nylocas Ischyros") || target.contains("Nylocas Hagios")))
                            nEntry.setDeprioritized(true);
                        break;
                }
            if ((this.config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOSS || this.config.wheelchairNylo() == SpoonTobConfig.wheelchairMode.BOTH) && this.nyloMiniboss != null && target
                    .contains("Nylocas Prinkipas") && option.equalsIgnoreCase("attack") && this.weaponStyle != null)
                switch (this.weaponStyle) {
                    case TRIDENTS:
                    case MAGIC:
                        if (this.nyloMiniboss.getId() != 10805)
                            nbEntry.setDeprioritized(true);
                        break;
                    case MELEE:
                        if (this.nyloMiniboss.getId() != 10804)
                            nbEntry.setDeprioritized(true);
                        break;
                    case RANGE:
                        if (this.nyloMiniboss.getId() != 10806)
                            nbEntry.setDeprioritized(true);
                        break;
                }
        }
    }

    static String stripColor(String str) {
        return str.replaceAll("(<col=[0-9a-f]+>|</col>)", "");
    }

    @Subscribe
    public void onMenuOpened(MenuOpened menu) {
        if (this.config.nyloRecolorMenu() && this.nyloActive && !this.nyloBossAlive)
            this.client.setMenuEntries((MenuEntry[])Arrays.<MenuEntry>stream(menu.getMenuEntries()).filter(s -> !s.getOption().equals("Examine")).toArray(x$0 -> new MenuEntry[x$0]));
    }

    public void removeGameObjectsFromScene(Set<Integer> objectIDs, int plane) {
        Scene scene = this.client.getScene();
        Tile[][] tiles = scene.getTiles()[plane];
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                Tile tile = tiles[x][y];
                if (tile != null &&
                        objectIDs != null) {
                    Objects.requireNonNull(scene);
                    Arrays.<GameObject>stream(tile.getGameObjects()).filter(obj -> (obj != null && objectIDs.contains(Integer.valueOf(obj.getId())))).findFirst().ifPresent(scene::removeGameObject);
                }
            }
        }
    }

    boolean isInNyloRegion() {
        return (this.client.isInInstancedRegion() && (this.client.getMapRegions()).length > 0 && this.client.getMapRegions()[0] == 13122);
    }

    private boolean isInBloatRegion() {
        return (this.client.isInInstancedRegion() && (this.client.getMapRegions()).length > 0 && this.client.getMapRegions()[0] == 13125);
    }
}
