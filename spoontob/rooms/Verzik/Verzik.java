package net.runelite.client.plugins.spoontob.rooms.Verzik;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.PlayerComposition;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.util.PoisonStyle;
import net.runelite.client.plugins.spoontob.util.PoisonWeaponMap;
import net.runelite.client.ui.overlay.Overlay;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Verzik extends Room {
    @Inject
    private Client client;

    @Inject
    private VerzikOverlay verzikOverlay;

    @Inject
    private LightningPanel lightningPanel;

    @Inject
    private PurpleCrabPanel purpleCrabPanel;

    @Inject
    private GreenBallPanel greenBallPanel;

    @Inject
    private YellowGrouperOverlay yellowGroupOverlay;

    @Inject
    private VerzikRedsOverlay redsOverlay;

    private PoisonStyle poisonStyle;

    private boolean skipTickCheck = false;

    private ClientThread clientThread;

    private static final Logger log = LoggerFactory.getLogger(SpoonTobPlugin.class);

    private static final int REGULAR_TORNADO = 8386;

    private static final int SM_TORNADO = 10846;

    private static final int HM_TORNADO = 10863;

    private static final int VERZIK_P1_MAGIC = 8109;

    private static final int VERZIK_P2_REG = 8114;

    private static final int VERZIK_P2_BOUNCE = 8116;

    private static final int VERZIK_ORGASM = 8117;

    private static final int p3_crab_attack_count = 5;

    private static final int p3_web_attack_count = 10;

    private static final int p3_yellow_attack_count = 15;

    private static final int p3_green_attack_count = 20;

    private static final int VERZIK_RANGE_BALL = 1583;

    private static final int VERZIK_LIGHTNING_BALL = 1585;

    private static final int VERZIK_YELLOW_OBJECT = 1595;

    private static final int VERZIK_YELLOW_BALL = 1596;

    private static final int VERZIK_GREEN_BALL = 1598;

    private static final int VERZIK_PURPLE_SPAWN = 1586;

    private static final int HM_ACID = 41747;

    protected static final Set<Integer> BEFORE_START_IDS = (Set<Integer>)ImmutableSet.of(
            8369, 10830, 10847);

    protected static final Set<Integer> P1_IDS = (Set<Integer>)ImmutableSet.of(
            8370, 10831, 10848);

    protected static final Set<Integer> P12_TRANSITION_IDS = (Set<Integer>)ImmutableSet.of(
            8371, 10832, 10849);

    protected static final Set<Integer> P2_IDS = (Set<Integer>)ImmutableSet.of(
            8372, 10833, 10850);

    protected static final Set<Integer> P23_TRANSITION_IDS = (Set<Integer>)ImmutableSet.of(
            8373, 10834, 10851);

    protected static final Set<Integer> P3_IDS = (Set<Integer>)ImmutableSet.of(
            8374, 10835, 10852);

    protected static final Set<Integer> DEAD_IDS = (Set<Integer>)ImmutableSet.of(
            8375, 10836, 10853);

    protected static final Set<Integer> NADO_IDS = (Set<Integer>)ImmutableSet.of(
            8386, 10846, 10863);

    protected static final Set<Integer> VERZIK_ACTIVE_IDS = ImmutableSet.of(
            8370, 8371, 8372, 8373, 8374,
            10831, 10832, 10833, 10834, 10835,
            10848, 10849, 10850, 10851, 10852
    );

    private NPC verzikNPC;

    private boolean verzikActive;

    public NPC getVerzikNPC() {
        return this.verzikNPC;
    }

    public boolean isVerzikActive() {
        return this.verzikActive;
    }

    private Map<NPC, Pair<Integer, Integer>> verzikReds = new HashMap<>();

    public Map<NPC, Pair<Integer, Integer>> getVerzikReds() {
        return this.verzikReds;
    }
    private HashSet<NPC> verzikAggros = new HashSet<>();

    public HashSet<NPC> getVerzikAggros() {
        return this.verzikAggros;
    }

    private int verzikTicksUntilAttack = -1;

    public int getVerzikTicksUntilAttack() {
        return this.verzikTicksUntilAttack;
    }

    private int verzikTotalTicksUntilAttack = 0;

    public int getVerzikTotalTicksUntilAttack() {
        return this.verzikTotalTicksUntilAttack;
    }

    private boolean verzikEnraged = false;

    public boolean isVerzikEnraged() {
        return this.verzikEnraged;
    }

    private boolean verzikFirstEnraged = false;

    private int verzikAttackCount;

    protected Phase verzikPhase;

    public int getVerzikAttackCount() {
        return this.verzikAttackCount;
    }

    public Phase getVerzikPhase() {
        return this.verzikPhase;
    }

    private boolean verzikTickPaused = true;

    protected boolean verzikRedPhase = false;

    private SpecialAttack verzikSpecial;

    private int verzikLastAnimation;

    public SpecialAttack getVerzikSpecial() {
        return this.verzikSpecial;
    }

    public final Map<Projectile, WorldPoint> verzikRangeProjectiles = new HashMap<>();

    public Map<Projectile, WorldPoint> getVerzikRangeProjectiles() {
        return this.verzikRangeProjectiles;
    }

    private final Map<LocalPoint, Integer> purpleCrabProjectile = new HashMap<>();

    public Map<LocalPoint, Integer> getPurpleCrabProjectile() {
        return this.purpleCrabProjectile;
    }

    private NPC purpleCrabNpc = null;

    public NPC getPurpleCrabNpc() {
        return this.purpleCrabNpc;
    }

    public int purpleAttacksLeft = 0;

    public ArrayList<Integer> WEAPONS = new ArrayList<>(Arrays.asList(12926, 12006, 12899, 22292, 5698));

    public ArrayList<Integer> SERPS = new ArrayList<>(Arrays.asList(12931, 13197, 13199));

    public int weaponId = 0;

    public int helmId = 0;

    public boolean yellowsOut;

    public int yellowTimer;

    public int hmYellowSpotNum;

    public ArrayList<NPC> redCrabs = new ArrayList<>();

    public ArrayList<Integer> lastRatioList = new ArrayList<>();

    public ArrayList<Integer> lastHealthScaleList = new ArrayList<>();

    public ArrayList<GameObject> acidSpots = new ArrayList<>();

    public ArrayList<Integer> acidSpotsTimer = new ArrayList<>();

    public int lightningAttacks;

    public int lightningAttacksDelay;

    private Map<Projectile, Integer> verzikLightningProjectiles = new HashMap<>();

    public ArrayList<ArrayList<WorldPoint>> yellowGroups;

    private ArrayList<WorldPoint> yellows;

    public ArrayList<WorldPoint> yellowsList;

    public Map<Projectile, Integer> getVerzikLightningProjectiles() {
        return this.verzikLightningProjectiles;
    }

    private NPC personalNado = null;

    public ArrayList<TornadoTracker> nadoList;

    private WorldPoint prevPlayerWp;

    private int nadosOut;

    public NPC getPersonalNado() {
        return this.personalNado;
    }

    private int personalNadoRespawn = 0;

    private ArrayList<String> partyMembersNames = new ArrayList<>();

    public int greenBallBounces = 0;

    public boolean greenBallOut = false;

    public int greenBallDelay = 0;

    List<GameObject> pillarsPendingRemoval;

    public List<WorldPoint> pillarLocations;

    public static final Predicate<Integer> valueIsZero;

    public static final BiFunction<Object, Integer, Integer> updateTicks;

    @Inject
    private Verzik(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
        this.verzikSpecial = SpecialAttack.NONE;
        this.verzikLastAnimation = -1;
        this.purpleCrabNpc = null;
        this.purpleAttacksLeft = 0;
        this.weaponId = 0;
        this.helmId = 0;
        this.poisonStyle = null;
        this.yellowsOut = false;
        this.yellowTimer = 14;
        this.hmYellowSpotNum = 1;
        this.lightningAttacks = 4;
        this.lightningAttacksDelay = 0;
        this.yellowGroups = new ArrayList<>();
        this.yellows = new ArrayList<>();
        this.yellowsList = new ArrayList<>();
        this.nadosOut = 0;
        this.nadoList = new ArrayList<>();
        this.personalNadoRespawn = 0;
        this.greenBallBounces = 0;
        this.greenBallOut = false;
        this.greenBallDelay = 0;
        this.pillarsPendingRemoval = new ArrayList<>();
        this.pillarLocations = new ArrayList<>();
    }

    public void load() {
        this.overlayManager.add((Overlay)this.verzikOverlay);
        this.overlayManager.add((Overlay)this.lightningPanel);
        this.overlayManager.add((Overlay)this.yellowGroupOverlay);
        this.overlayManager.add((Overlay)this.greenBallPanel);
        this.overlayManager.add((Overlay)this.purpleCrabPanel);
        this.overlayManager.add((Overlay)this.redsOverlay);
        this.poisonStyle = null;
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.verzikOverlay);
        this.overlayManager.remove((Overlay)this.lightningPanel);
        this.overlayManager.remove((Overlay)this.yellowGroupOverlay);
        this.overlayManager.remove((Overlay)this.greenBallPanel);
        this.overlayManager.remove((Overlay)this.purpleCrabPanel);
        this.overlayManager.remove((Overlay)this.redsOverlay);
        verzikCleanup();
        this.plugin.clearHiddenNpcs();
        this.poisonStyle = null;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 8369:
            case 8371:
            case 8373:
            case 8375:
            case 10830:
            case 10832:
            case 10834:
            case 10836:
            case 10847:
            case 10849:
            case 10851:
            case 10853:
                verzikSpawn(npc);
                break;
            case 8370:
            case 10831:
            case 10848:
                this.verzikPhase = Phase.PHASE1;
                verzikSpawn(npc);
                break;
            case 8372:
            case 10833:
            case 10850:
                this.verzikPhase = Phase.PHASE2;
                verzikSpawn(npc);
                this.lightningAttacks = 4;
                break;
            case 8374:
            case 10835:
            case 10852:
                this.verzikPhase = Phase.PHASE3;
                verzikSpawn(npc);
                break;
            case 8376:
            case 10837:
            case 10854:
                if (this.verzikNPC != null && this.verzikNPC.getInteracting() == null)
                    this.verzikSpecial = SpecialAttack.WEBS;
                break;
            case 8381:
            case 8382:
            case 8383:
            case 10841:
            case 10842:
            case 10843:
            case 10858:
            case 10859:
            case 10860:
                this.verzikAggros.add(npc);
                break;
            case 8385:
            case 10845:
            case 10862:
                this.verzikReds.putIfAbsent(npc, new MutablePair(npc.getHealthRatio(), npc.getHealthScale()));
                break;
            case 8386:
            case 10846:
            case 10863:
                if (this.personalNado == null && this.personalNadoRespawn == 0)
                    this.nadoList.add(new TornadoTracker(npc));
                if (!this.verzikEnraged) {
                    this.verzikEnraged = true;
                    this.verzikFirstEnraged = true;
                }
                this.nadosOut++;
                break;
        }
        if (npc.getName() != null && npc.getName().equals("Nylocas Athanatos"))
            this.purpleCrabNpc = npc;
        if (npc.getName() != null && npc.getName().equals("Nylocas Matomenos")) {
            this.redCrabs.add(npc);
            this.lastRatioList.add(Integer.valueOf(0));
            this.lastHealthScaleList.add(Integer.valueOf(0));
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        int id = event.getNpc().getId();
        if (DEAD_IDS.contains(id)) {
            verzikCleanup();
        } else if (P1_IDS.contains(id)) {
            this.partyMembersNames.clear();
            for (int i = 330; i < 335; i++) {
                if (this.client.getVarcStrValue(i) != null && !this.client.getVarcStrValue(i).equals(""))
                    this.partyMembersNames.add(this.client.getVarcStrValue(i).replaceAll("[^A-Za-z0-9_-]", " ").trim());
            }
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8369:
            case 8370:
            case 8371:
            case 8372:
            case 8373:
            case 8374:
            case 8375:
            case 10830:
            case 10831:
            case 10832:
            case 10833:
            case 10834:
            case 10835:
            case 10836:
            case 10847:
            case 10848:
            case 10849:
            case 10850:
            case 10851:
            case 10852:
            case 10853:
                verzikCleanup();
            case 8381:
            case 8382:
            case 8383:
            case 10841:
            case 10842:
            case 10843:
            case 10858:
            case 10859:
            case 10860:
                this.verzikAggros.remove(npc);
                break;
            case 8385:
            case 10845:
            case 10862:
                this.verzikReds.remove(npc);
                break;
            case 8386:
            case 10846:
            case 10863:
                if (this.personalNado == npc) {
                    this.personalNado = null;
                    this.personalNadoRespawn = 18;
                }
                this.nadoList.removeIf(tt -> (tt.getNpc() == npc));
                this.nadosOut--;
                if (this.plugin.hiddenIndices.contains(Integer.valueOf(npc.getIndex())))
                    this.plugin.setHiddenNpc(npc, false);
                break;
        }
        if (npc.getName() != null && npc.getName().equals("Nylocas Athanatos"))
            this.purpleCrabNpc = null;
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        int id = event.getProjectile().getId();
        Projectile p = event.getProjectile();
        int ticks = (int)Math.round(p.getRemainingCycles() / 30.0D);
        if (id == 1583) {
            WorldPoint wp = WorldPoint.fromLocal(this.client, event.getPosition());
            this.verzikRangeProjectiles.put(event.getProjectile(), wp);
            if (this.lightningAttacksDelay == 0) {
                this.lightningAttacks--;
                this.lightningAttacksDelay = 4;
            }
        } else if (id == 1586) {
            this.purpleCrabProjectile.put(event.getPosition(), Integer.valueOf(ticks));
            this.purpleAttacksLeft = 21;
        } else if (id == 1585 && this.lightningAttacks < 2) {
            this.lightningAttacks = 4;
            if (ticks > 0)
                this.verzikLightningProjectiles.putIfAbsent(p, Integer.valueOf(ticks));
        } else if (id == 1598) {
            if (!this.greenBallOut)
                this.greenBallOut = true;
            if (p.getRemainingCycles() == 0) {
                this.greenBallOut = false;
                this.greenBallBounces++;
                this.greenBallDelay = 3;
            }
        }
    }

    @Subscribe
    public void onGraphicsObjectCreated(GraphicsObjectCreated event) {
        if (event.getGraphicsObject().getId() == 1595 && !this.yellowsOut) {
            WorldPoint wp = WorldPoint.fromLocal(this.client, event.getGraphicsObject().getLocation());
            if (!this.yellows.contains(wp))
                this.yellows.add(wp);
            if (!this.yellowsList.contains(wp))
                this.yellowsList.add(wp);
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        if (this.config.showVerzikAcid() && event.getGameObject().getId() == 41747) {
            int index = this.acidSpots.indexOf(event.getGameObject());
            this.acidSpots.remove(event.getGameObject());
            if (index != -1)
                this.acidSpotsTimer.remove(index);
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("spoontob") &&
                event.getKey().equals("hideOtherNados") &&
                !this.config.hideOtherNados())
            this.plugin.clearHiddenNpcs();
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (this.verzikActive) {
            Actor actor = event.getActor();
            if (actor instanceof NPC) {
                NPC npc = (NPC)actor;
                if (npc.getName() != null && npc.getName().equals("Nylocas Matomenos") && npc.getAnimation() == 8097) {
                    int index = this.redCrabs.indexOf(npc);
                    if (index != -1) {
                        this.lastRatioList.remove(index);
                        this.lastHealthScaleList.remove(index);
                    }
                    this.redCrabs.remove(npc);
                }
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (this.verzikNPC != null) {
            String target = event.getTarget();
            Player player = this.client.getLocalPlayer();
            PlayerComposition playerComp = (player != null) ? player.getPlayerComposition() : null;
            MenuEntry pvEntry = event.getMenuEntry();
            if (this.config.hidePurple() && P2_IDS.contains(Integer.valueOf(this.verzikNPC.getId())) && target
                    .contains("Nylocas Athanatos") && event.getType() == MenuAction.NPC_SECOND_OPTION.getId() && this.poisonStyle != null) {
                switch (this.poisonStyle) {
                    case NOT:
                        if (playerComp != null && !this.SERPS.contains(Integer.valueOf(playerComp.getEquipmentId(KitType.HEAD))))
                            pvEntry.setDeprioritized(true);
                        break;
                }
            } else if (P3_IDS.contains(Integer.valueOf(this.verzikNPC.getId())) &&
                    this.config.hideAttackYellows() && this.verzikSpecial == SpecialAttack.YELLOWS && this.verzikTicksUntilAttack > 8 &&
                    target.contains("Verzik Vitur") && event.getType() == MenuAction.NPC_SECOND_OPTION.getId()) {
                pvEntry.setDeprioritized(true);
            }
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        Player player = this.client.getLocalPlayer();
        PlayerComposition playerComp = (player != null) ? player.getPlayerComposition() : null;
        if (event.getMenuOption().equalsIgnoreCase("wield")) {
            PoisonStyle newStyle = (PoisonStyle)PoisonWeaponMap.PoisonType.get(Integer.valueOf(event.getItemId()));
            if (newStyle != null) {
                this.skipTickCheck = true;
                this.poisonStyle = newStyle;
            }
        } else if (this.config.hidePurple() && this.verzikNPC != null && P2_IDS.contains(Integer.valueOf(this.verzikNPC.getId())) && event
                .getMenuTarget().contains("Nylocas Athanatos") && event.getMenuAction() == MenuAction.NPC_SECOND_OPTION && this.poisonStyle != null) {
            switch (this.poisonStyle) {
                case NOT:
                    if (playerComp == null || !this.SERPS.contains(Integer.valueOf(playerComp.getEquipmentId(KitType.HEAD))));
                    break;
            }
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (this.verzikNPC != null && event.getActor() instanceof Player && event.getActor().getName() != null)
            this.partyMembersNames.remove(event.getActor().getName());
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        if (event.getActor() != null && event.getActor().getName() != null && event.getActor() instanceof Player && this.verzikPhase == Phase.PHASE3) {
            Actor actor = event.getActor();
            if (actor.getGraphic() == 1602 && actor.getName().equals(this.client.getLocalPlayer().getName())) {
                this.personalNado = null;
                this.personalNadoRespawn = 18;
                if (this.nadoList.size() == 1)
                    this.nadoList.clear();
            }
        }
    }

    @Subscribe
    public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event) {
        if (event.getSource() != null && event.getSource().getName() != null && this.verzikNPC != null && this.config.muteVerzikSounds() && (
                event.getSoundId() == 3991 || event.getSoundId() == 3987))
            event.consume();
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.verzikActive) {
            if (this.skipTickCheck) {
                this.skipTickCheck = false;
            } else {
                if (this.client.getLocalPlayer() == null || this.client.getLocalPlayer().getPlayerComposition() == null)
                    return;
                int equippedWeapon = ((Integer)ObjectUtils.defaultIfNull(Integer.valueOf(this.client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON)), Integer.valueOf(-1))).intValue();
                int equippedHelm = ((Integer)ObjectUtils.defaultIfNull(Integer.valueOf(this.client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.HEAD)), Integer.valueOf(-1))).intValue();
                this.poisonStyle = (PoisonStyle)PoisonWeaponMap.PoisonType.get(Integer.valueOf(equippedWeapon));
                this.helmId = equippedHelm;
            }
            if (this.lightningAttacksDelay > 0)
                this.lightningAttacksDelay--;
            if (this.personalNadoRespawn > 0)
                this.personalNadoRespawn--;
            if (this.greenBallDelay > 0) {
                this.greenBallDelay--;
                if (this.greenBallDelay == 0 && !this.greenBallOut)
                    this.greenBallBounces = 0;
            }
            if (this.acidSpots.size() > 0 && this.acidSpotsTimer.size() > 0)
                for (int i = 0; i < this.acidSpotsTimer.size(); i++)
                    this.acidSpotsTimer.set(i, Integer.valueOf(((Integer)this.acidSpotsTimer.get(i)).intValue() - 1));
            if (!this.verzikRangeProjectiles.isEmpty())
                this.verzikRangeProjectiles.keySet().removeIf(projectile -> (projectile.getRemainingCycles() < 1));
            if (this.verzikPhase == Phase.PHASE3 &&
                    this.yellowsList.size() > 0)
                if (!this.yellowsOut) {
                    if (this.verzikNPC.getId() == 10852)
                        this.yellowGroups = findYellows(this.yellows);
                    this.yellowsOut = true;
                } else {
                    this.yellowTimer--;
                    if (this.yellowTimer <= 0)
                        if (this.verzikNPC.getId() == 10852) {
                            if (this.hmYellowSpotNum < 3) {
                                this.yellowTimer = 3;
                                this.hmYellowSpotNum++;
                                for (Player p : this.client.getPlayers()) {
                                    if (p.getName() != null && this.partyMembersNames.contains(p.getName())) {
                                        WorldPoint wp = WorldPoint.fromLocal(this.client, p.getLocalLocation());
                                        int index = 0;
                                        for (ArrayList<WorldPoint> yg : this.yellowGroups) {
                                            if (yg.contains(wp)) {
                                                ((ArrayList)this.yellowGroups.get(index)).remove(wp);
                                                break;
                                            }
                                            boolean exitLoop = false;
                                            for (int j = yg.size() - 1; j >= 0; j--) {
                                                if (((WorldPoint)yg.get(j)).distanceTo(wp) <= 1) {
                                                    ((ArrayList)this.yellowGroups.get(index)).remove(j);
                                                    exitLoop = true;
                                                    break;
                                                }
                                            }
                                            if (exitLoop)
                                                break;
                                            index++;
                                        }
                                        if (this.yellowsList.contains(wp)) {
                                            this.yellowsList.remove(wp);
                                            continue;
                                        }
                                        for (int i = this.yellowsList.size() - 1; i >= 0; i--) {
                                            if (((WorldPoint)this.yellowsList.get(i)).distanceTo(wp) <= 1) {
                                                this.yellowsList.remove(i);
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                this.yellowsOut = false;
                                this.yellowTimer = 14;
                                this.hmYellowSpotNum = 1;
                                this.yellows.clear();
                                this.yellowsList.clear();
                                this.yellowGroups.clear();
                            }
                        } else {
                            this.yellowsOut = false;
                            this.yellowTimer = 14;
                            this.yellows.clear();
                            this.yellowsList.clear();
                            this.yellowGroups.clear();
                        }
                }
            if (this.verzikPhase == Phase.PHASE3 && this.nadoList.size() > 0 && this.client.getLocalPlayer() != null) {
                boolean recalc = false;
                for (int i = this.nadoList.size() - 1; i >= 0; i--) {
                    if (((TornadoTracker)this.nadoList.get(i)).checkMovement(this.prevPlayerWp, ((TornadoTracker)this.nadoList.get(i)).getNpc().getWorldLocation()) != -1) {
                        this.nadoList.remove(i);
                        if (this.nadoList.size() == 0)
                            for (NPC npc : this.client.getNpcs()) {
                                if (NADO_IDS.contains(npc.getId()))
                                    this.nadoList.add(new TornadoTracker(npc));
                                this.personalNado = null;
                                recalc = true;
                                if (this.plugin.hiddenIndices.contains(npc.getIndex()))
                                    this.plugin.setHiddenNpc(npc, false);
                            }
                    } else {
                        ((TornadoTracker)this.nadoList.get(i)).setPrevLoc(((TornadoTracker)this.nadoList.get(i)).getNpc().getWorldLocation());
                    }
                }
                if (this.nadoList.size() == 1 && this.personalNado == null && !recalc)
                    this.personalNado = ((TornadoTracker)this.nadoList.get(0)).getNpc();
                this.prevPlayerWp = this.client.getLocalPlayer().getWorldLocation();
            }
            Function<Integer, Integer> adjust_for_enrage = i -> isVerzikEnraged() ? (i - 2) : i;
            if (this.verzikTickPaused) {
                switch (this.verzikNPC.getId()) {
                    case 8370:
                    case 10831:
                    case 10848:
                        this.verzikPhase = Phase.PHASE1;
                        this.verzikAttackCount = 0;
                        this.verzikTicksUntilAttack = 18;
                        this.verzikTickPaused = false;
                        break;
                    case 8372:
                    case 10832:
                    case 10850:
                        this.verzikPhase = Phase.PHASE2;
                        this.verzikAttackCount = 0;
                        this.verzikTicksUntilAttack = 3;
                        this.verzikTickPaused = false;
                        this.purpleAttacksLeft = 21;
                        break;
                    case 8374:
                    case 10835:
                    case 10852:
                        this.verzikPhase = Phase.PHASE3;
                        this.verzikAttackCount = 0;
                        this.verzikTicksUntilAttack = 6;
                        this.verzikTickPaused = false;
                        break;
                }
            } else if (this.verzikSpecial == SpecialAttack.WEBS) {
                this.verzikTotalTicksUntilAttack++;
                if (this.verzikNPC.getInteracting() != null) {
                    this.verzikSpecial = SpecialAttack.WEB_COOLDOWN;
                    this.verzikAttackCount = 10;
                    this.verzikTicksUntilAttack = 10;
                    this.verzikFirstEnraged = false;
                }
            } else {
                this.verzikTicksUntilAttack = Math.max(0, this.verzikTicksUntilAttack - 1);
                this.verzikTotalTicksUntilAttack++;
                int animationID = this.verzikNPC.getAnimation();
                if (animationID > -1 && this.verzikPhase == Phase.PHASE1 && this.verzikTicksUntilAttack < 5 && animationID != this.verzikLastAnimation)
                    if (animationID == 8109) {
                        this.verzikTicksUntilAttack = 14;
                        this.verzikAttackCount++;
                    }
                if (animationID > -1 && this.verzikPhase == Phase.PHASE2 && this.verzikTicksUntilAttack < 3 && animationID != this.verzikLastAnimation)
                    switch (animationID) {
                        case 8114:
                        case 8116:
                            this.verzikTicksUntilAttack = 4;
                            this.verzikAttackCount++;
                            this.purpleAttacksLeft--;
                            if (this.verzikAttackCount == 7 && this.verzikRedPhase)
                                this.verzikTicksUntilAttack = 8;
                            break;
                        case 8117:
                            this.verzikRedPhase = true;
                            this.verzikAttackCount = 0;
                            this.verzikTicksUntilAttack = 12;
                            break;
                    }
                this.verzikLastAnimation = animationID;
                if (this.verzikPhase == Phase.PHASE3) {
                    this.verzikAttackCount %= 20;
                    if (this.verzikTicksUntilAttack <= 0) {
                        this.verzikAttackCount++;
                        if (this.verzikAttackCount < 10) {
                            this.verzikSpecial = SpecialAttack.NONE;
                            this.verzikTicksUntilAttack = (Integer) adjust_for_enrage.apply(7);
                        } else if (this.verzikAttackCount < 15) {
                            this.verzikSpecial = SpecialAttack.NONE;
                            this.verzikTicksUntilAttack = (Integer) adjust_for_enrage.apply(7);
                        } else if (this.verzikAttackCount < 16) {
                            this.verzikSpecial = SpecialAttack.YELLOWS;
                            if (this.verzikNPC.getId() == 10852) {
                                this.verzikTicksUntilAttack = 27;
                            } else {
                                this.verzikTicksUntilAttack = 21;
                            }
                        } else if (this.verzikAttackCount < 20) {
                            this.verzikSpecial = SpecialAttack.NONE;
                            this.verzikTicksUntilAttack = (Integer) adjust_for_enrage.apply(7);
                        } else if (this.verzikAttackCount < 21) {
                            this.verzikSpecial = SpecialAttack.GREEN;
                            this.verzikTicksUntilAttack = 12;
                        } else {
                            this.verzikSpecial = SpecialAttack.NONE;
                            this.verzikTicksUntilAttack = ((Integer)adjust_for_enrage.apply(Integer.valueOf(7))).intValue();
                        }
                    }
                    if (this.verzikFirstEnraged) {
                        this.verzikFirstEnraged = false;
                        if (this.verzikSpecial != SpecialAttack.YELLOWS || this.verzikTicksUntilAttack <= 7)
                            this.verzikTicksUntilAttack = 5;
                    }
                }
            }
            if (this.purpleCrabProjectile.size() > 0) {
                this.purpleCrabProjectile.values().removeIf(valueIsZero);
                this.purpleCrabProjectile.replaceAll(updateTicks);
            }
            if (this.verzikPhase == Phase.PHASE2)
                for (Iterator<Projectile> it = this.verzikLightningProjectiles.keySet().iterator(); it.hasNext(); ) {
                    Projectile key = it.next();
                    this.verzikLightningProjectiles.replace(key, Integer.valueOf(((Integer)this.verzikLightningProjectiles.get(key)).intValue() - 1));
                    if (((Integer)this.verzikLightningProjectiles.get(key)).intValue() < 0)
                        it.remove();
                }
        }
    }

    Color verzikSpecialWarningColor() {
        Color col = Color.WHITE;
        if (this.verzikPhase != Phase.PHASE3)
            return col;
        switch (this.verzikAttackCount) {
            case 4:
                col = Color.MAGENTA;
                break;
            case 9:
                col = Color.ORANGE;
                break;
            case 14:
                col = Color.YELLOW;
                break;
            case 19:
                col = Color.GREEN;
                break;
        }
        return col;
    }

    private void verzikSpawn(NPC npc) {
        this.verzikEnraged = false;
        this.verzikRedPhase = false;
        this.verzikFirstEnraged = false;
        this.verzikTicksUntilAttack = 0;
        this.verzikAttackCount = 0;
        this.verzikNPC = npc;
        this.verzikActive = true;
        this.verzikTickPaused = true;
        this.verzikSpecial = SpecialAttack.NONE;
        this.verzikTotalTicksUntilAttack = 0;
        this.verzikLastAnimation = -1;
    }

    private void verzikCleanup() {
        this.verzikAggros.clear();
        this.verzikReds.clear();
        this.verzikEnraged = false;
        this.verzikFirstEnraged = false;
        this.verzikRedPhase = false;
        this.verzikActive = false;
        this.yellowsList.clear();
        this.yellowGroups.clear();
        this.yellowsOut = false;
        this.yellowTimer = 14;
        this.hmYellowSpotNum = 1;
        this.nadoList.clear();
        this.prevPlayerWp = null;
        this.personalNado = null;
        this.nadosOut = 0;
        this.verzikNPC = null;
        this.verzikPhase = null;
        this.verzikTickPaused = true;
        this.verzikSpecial = SpecialAttack.NONE;
        this.verzikTotalTicksUntilAttack = 0;
        this.verzikLastAnimation = -1;
        this.redCrabs.clear();
        this.lastRatioList.clear();
        this.lastHealthScaleList.clear();
        this.acidSpots.clear();
        this.acidSpotsTimer.clear();
        this.lightningAttacks = 4;
        this.lightningAttacksDelay = 0;
        this.greenBallBounces = 0;
        this.greenBallOut = false;
        this.greenBallDelay = 0;
        this.pillarsPendingRemoval = new ArrayList<>();
        this.pillarLocations = new ArrayList<>();
    }

    enum SpecialAttack {
        WEB_COOLDOWN, WEBS, YELLOWS, GREEN, NONE;
    }

    public enum Phase {
        PHASE1, PHASE2, PHASE3;
    }

    static {
        valueIsZero = (v -> (v.intValue() <= 0));
        updateTicks = ((k, v) -> Integer.valueOf(v.intValue() - 1));
    }

    public WorldPoint getNearestPoint(WorldPoint corner, ArrayList<WorldPoint> points) {
        double minDistance = 2.147483647E9D;
        WorldPoint point = new WorldPoint(corner.getX(), corner.getY(), corner.getPlane());
        for (WorldPoint p : points) {
            double distance = distanceBetween(p, corner);
            if (distance < minDistance) {
                minDistance = distance;
                point = p;
            }
        }
        return point;
    }

    public int isSetSpawn(WorldPoint p) {
        if (p.getRegionX() == 7 && p.getRegionY() == 11)
            return 1;
        if (p.getRegionX() == 16 && p.getRegionY() == 17)
            return 2;
        if (p.getRegionX() == 25 && p.getRegionY() == 11)
            return 3;
        if (p.getRegionX() == 7 && p.getRegionY() == 23)
            return 4;
        if (p.getRegionX() == 25 && p.getRegionY() == 23)
            return 5;
        return -1;
    }

    public WorldPoint getNextValidPoint(ArrayList<WorldPoint> points) {
        for (WorldPoint p : points) {
            if (isSetSpawn(p) != -1)
                return p;
        }
        return null;
    }

    public ArrayList<ArrayList<WorldPoint>> findYellows(ArrayList<WorldPoint> points) {
        ArrayList<ArrayList<WorldPoint>> groups = new ArrayList<>();
        while (points.size() > 0) {
            ArrayList<WorldPoint> group = new ArrayList<>();
            WorldPoint initial = getNextValidPoint(points);
            group.add(initial);
            points.remove(initial);
            WorldPoint second = getNearestPoint(initial, points);
            group.add(second);
            points.remove(second);
            WorldPoint third = getNearestPoint(initial, points);
            group.add(third);
            points.remove(third);
            groups.add(group);
        }
        return groups;
    }

    public double distanceBetween(WorldPoint a, WorldPoint b) {
        return Math.sqrt(Math.pow((a.getRegionX() - b.getRegionX()), 2.0D) + Math.pow((a.getRegionY() - b.getRegionY()), 2.0D));
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.config.hideOtherNados())
            for (NPC npc : this.client.getNpcs()) {
                if (npc != null && NADO_IDS.contains(Integer.valueOf(npc.getId())) &&
                        this.personalNado != null && this.personalNado.getIndex() != npc.getIndex() &&
                        !this.plugin.hiddenIndices.contains(Integer.valueOf(npc.getIndex())))
                    this.plugin.setHiddenNpc(npc, true);
            }
    }
}
