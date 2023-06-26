package net.runelite.client.plugins.spoontob.rooms.Bloat;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.Polygon;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.Angle;
import net.runelite.api.coords.Direction;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.RoomOverlay;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.rooms.Bloat.stomp.BloatDown;
import net.runelite.client.plugins.spoontob.rooms.Bloat.stomp.def.BloatChunk;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.util.Text;

public class Bloat extends Room {
    private ClientThread clientThread;

    @Inject
    private BloatOverlay bloatOverlay;

    @Inject
    private Client client;

    protected static final int ROOM_STATE_VARBIT = 6447;

    protected static final Set<Integer> BLOAT_IDS = (Set<Integer>)ImmutableSet.of(Integer.valueOf(8359), Integer.valueOf(10812), Integer.valueOf(10813));

    protected int lastVarp6447 = 0;

    public int bloatVar = 0;

    private boolean bloatActive;

    private NPC bloatNPC;

    public boolean isBloatActive() {
        return this.bloatActive;
    }

    public NPC getBloatNPC() {
        return this.bloatNPC;
    }

    private int bloatDownCount = 0;

    public int getBloatDownCount() {
        return this.bloatDownCount;
    }

    private int bloatUpTimer = 0;

    public int getBloatUpTimer() {
        return this.bloatUpTimer;
    }

    private int bloatState = 0;

    public int getBloatState() {
        return this.bloatState;
    }

    private BloatDown bloatDown = null;

    private final HashMap<WorldPoint, Integer> bloathands;

    public BloatDown getBloatDown() {
        return this.bloatDown;
    }

    public HashMap<WorldPoint, Integer> getBloathands() {
        return this.bloathands;
    }

    public static final Set<Integer> topOfTankObjectIDs = (Set<Integer>)ImmutableSet.of(Integer.valueOf(32958), Integer.valueOf(32962), Integer.valueOf(32964), Integer.valueOf(32965), Integer.valueOf(33062));

    public static final Set<Integer> tankObjectIDs = (Set<Integer>)ImmutableSet.of(Integer.valueOf(32957), Integer.valueOf(32955), Integer.valueOf(32959), Integer.valueOf(32960), Integer.valueOf(32964), Integer.valueOf(33084));

    public static final Set<Integer> ceilingChainsObjectIDs = (Set<Integer>)ImmutableSet.of(Integer.valueOf(32949), Integer.valueOf(32950), Integer.valueOf(32951), Integer.valueOf(32952), Integer.valueOf(32953), Integer.valueOf(32954),Integer.valueOf(32970));

    public int handTicks = 4;

    public boolean handsFalling = false;

    private static Clip clip;

    private LocalPoint bloatPrevLoc = null;

    private String bloatDirection = "";

    @Inject
    protected Bloat(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
        this.bloathands = new HashMap<>();
    }

    public void load() {
        this.overlayManager.add((Overlay)this.bloatOverlay);
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(SpoonTobPlugin.class.getResourceAsStream("reverse.wav")));
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip)AudioSystem.getLine(info);
            clip.open(stream);
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (control != null)
                control.setValue((this.config.reverseVolume() / 2 - 45));
        } catch (Exception var6) {
            clip = null;
        }
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.bloatOverlay);
        this.bloatDownCount = 0;
        this.bloatState = 0;
        this.bloatUpTimer = 0;
        this.bloatDown = null;
        this.handTicks = 4;
        this.handsFalling = false;
        this.bloatPrevLoc = null;
        this.bloatDirection = "";
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        if (BLOAT_IDS.contains(Integer.valueOf(npc.getId()))) {
            this.bloatActive = true;
            this.bloatNPC = npc;
            this.bloatUpTimer = 0;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        if (BLOAT_IDS.contains(Integer.valueOf(npc.getId()))) {
            this.bloatActive = false;
            this.bloatNPC = null;
            this.bloatUpTimer = 0;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (this.client.getGameState() != GameState.LOGGED_IN || event.getActor() != this.bloatNPC)
            return;
        this.bloatUpTimer = 0;
    }

    @Subscribe
    protected void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectC) {
        if (this.bloatActive) {
            GraphicsObject graphicsObject = graphicsObjectC.getGraphicsObject();
            if (graphicsObject.getId() >= 1560 && graphicsObject.getId() <= 1590) {
                WorldPoint point = WorldPoint.fromLocal(this.client, graphicsObject.getLocation());
                if (!this.bloathands.containsKey(point)) {
                    this.bloathands.put(point, Integer.valueOf(4));
                    if (!this.handsFalling)
                        this.handsFalling = true;
                }
            }
        }
    }

    @Subscribe
    protected void onVarbitChanged(VarbitChanged event) {
        if (isInRegion()) {
            int varp6447 = this.client.getVarbitValue(this.client.getVarps(), 6447);
            if (varp6447 != this.lastVarp6447 && varp6447 > 0) {
                this.bloatUpTimer = 0;
                this.bloatVar = 1;
            }
            this.lastVarp6447 = varp6447;
            if (this.client.getVarbitValue(6447) == 0)
                this.bloatVar = 0;
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("spoontob"))
            if (event.getKey().equals("hideAnnoyingAssObjects")) {
                if (TheatreRegions.inRegion(this.client, TheatreRegions.BLOAT)) {
                    this.plugin.refreshScene();
                    if (this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.TANK || this.config
                            .hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.BOTH) {
                        removeGameObjectsFromScene(tankObjectIDs, this.client.getPlane());
                        removeGameObjectsFromScene(topOfTankObjectIDs, 1);
                        nullTopOfTankTiles();
                    }
                    if (this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.CHAINS || this.config
                            .hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.BOTH)
                        removeGameObjectsFromScene(ceilingChainsObjectIDs, 1);
                }
            } else if (event.getKey().equals("reverseVolume") &&
                    clip != null) {
                FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (control != null)
                    control.setValue((this.config.reverseVolume() / 2 - 45));
            }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN &&
                TheatreRegions.inRegion(this.client, TheatreRegions.BLOAT)) {
            if (this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.CHAINS || this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.BOTH)
                removeGameObjectsFromScene(ceilingChainsObjectIDs, 1);
            if (this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.TANK || this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.BOTH) {
                removeGameObjectsFromScene(tankObjectIDs, this.client.getPlane());
                removeGameObjectsFromScene(topOfTankObjectIDs, 1);
                nullTopOfTankTiles();
            }
        }
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        if (TheatreRegions.inRegion(this.client, TheatreRegions.BLOAT)) {
            if (this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.CHAINS || this.config
                    .hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.BOTH)
                removeGameObjectsFromScene(ceilingChainsObjectIDs, 1);
            if (this.config.hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.TANK || this.config
                    .hideAnnoyingAssObjects() == SpoonTobConfig.annoyingObjectHideMode.BOTH) {
                removeGameObjectsFromScene(tankObjectIDs, this.client.getPlane());
                removeGameObjectsFromScene(topOfTankObjectIDs, 1);
                nullTopOfTankTiles();
            }
        }
    }

    public void refreshScene() {
        this.clientThread.invokeLater(() -> this.client.setGameState(GameState.LOADING));
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

    private void nullTopOfTankTiles() {
        List<WorldPoint> wpl = (new WorldArea(3293, 4445, 6, 6, 1)).toWorldPointList();
        wpl.forEach(wp -> {
            Collection<WorldPoint> wpi = WorldPoint.toLocalInstance(this.client, wp);
            wpi.forEach(this::nullThisTile);
        });
    }

    public void nullThisTile(WorldPoint tile) {
        int plane = tile.getPlane();
        int sceneX = tile.getX() - this.client.getBaseX();
        int sceneY = tile.getY() - this.client.getBaseY();
        if (plane <= 3 && plane >= 0 && sceneX <= 103 && sceneX >= 0 && sceneY <= 103 && sceneY >= 0)
            this.client.getScene().getTiles()[plane][sceneX][sceneY] = null;
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.bloatActive) {
            if (this.handsFalling) {
                this.handTicks--;
                if (this.handTicks <= 0) {
                    this.handTicks = 4;
                    this.handsFalling = false;
                }
            }
            this.bloatDownCount++;
            this.bloatUpTimer++;
            this.bloathands.values().removeIf(v -> (v.intValue() <= 0));
            this.bloathands.replaceAll((k, v) -> Integer.valueOf(v.intValue() - 1));
            if (this.bloatNPC.getAnimation() == -1) {
                this.bloatDownCount = 0;
                if (this.bloatNPC.getHealthScale() == 0) {
                    this.bloatState = 2;
                } else if (this.bloatUpTimer >= 38) {
                    this.bloatState = 4;
                } else {
                    this.bloatState = 1;
                }
            } else if (this.bloatUpTimer >= 38) {
                this.bloatState = 4;
            } else if (25 < this.bloatDownCount && this.bloatDownCount < 35) {
                this.bloatState = 3;
            } else if (this.bloatDownCount < 26) {
                this.bloatState = 2;
            } else if (this.bloatNPC.getModelHeight() == 568) {
                this.bloatState = 2;
            } else if (this.bloatUpTimer >= 38) {
                this.bloatState = 4;
            } else {
                this.bloatState = 1;
            }
            if (this.bloatNPC != null)
                if (this.bloatNPC.getAnimation() == -1 && this.bloatDown != null) {
                    this.bloatDown = null;
                } else if (this.bloatNPC.getAnimation() != -1 && this.bloatDown == null && !this.bloatNPC.isDead()) {
                    WorldPoint sw = this.bloatNPC.getWorldLocation();
                    Direction dir = (new Angle(this.bloatNPC.getOrientation())).getNearestDirection();
                    Supplier<BloatChunk> chunk = () -> {
                        LocalPoint lp = LocalPoint.fromWorld(this.client, sw);
                        if (lp != null && this.client.isInInstancedRegion()) {
                            int zone = this.client.getInstanceTemplateChunks()[0][lp.getSceneX() >> 3][lp.getSceneY() >> 3];
                            return BloatChunk.getOccupiedChunk(zone);
                        }
                        return BloatChunk.UNKNOWN;
                    };
                    this.bloatDown = new BloatDown(this.client, sw, dir, chunk.get());
                }
            if (this.bloatActive && this.bloatNPC != null && this.config.bloatReverseNotifier() != SpoonTobConfig.bloatTurnMode.OFF) {
                LocalPoint lp = LocalPoint.fromWorld(this.client, this.bloatNPC.getWorldLocation());
                if (this.bloatPrevLoc != null && lp != null) {
                    boolean changed = false;
                    if (lp.getX() > this.bloatPrevLoc.getX()) {
                        if (this.bloatDirection.equals("W"))
                            changed = true;
                        this.bloatDirection = "E";
                    } else if (lp.getX() < this.bloatPrevLoc.getX()) {
                        if (this.bloatDirection.equals("E"))
                            changed = true;
                        this.bloatDirection = "W";
                    } else if (lp.getY() > this.bloatPrevLoc.getY()) {
                        if (this.bloatDirection.equals("S"))
                            changed = true;
                        this.bloatDirection = "N";
                    } else if (lp.getY() < this.bloatPrevLoc.getY()) {
                        if (this.bloatDirection.equals("N"))
                            changed = true;
                        this.bloatDirection = "S";
                    }
                    if (changed)
                        if (this.config.bloatReverseNotifier() == SpoonTobConfig.bloatTurnMode.SOUND) {
                            this.client.playSoundEffect(98, this.config.reverseVolume());
                        } else {
                            clip.setFramePosition(0);
                            clip.start();
                        }
                }
                this.bloatPrevLoc = lp;
            }
        }
    }

    Polygon getBloatTilePoly() {
        LocalPoint lp;
        if (this.bloatNPC == null)
            return null;
        int size = 1;
        NPCComposition composition = this.bloatNPC.getTransformedComposition();
        if (composition != null)
            size = composition.getSize();
        switch (this.bloatState) {
            case 1:
            case 4:
                lp = this.bloatNPC.getLocalLocation();
                if (lp == null)
                    return null;
                return RoomOverlay.getCanvasTileAreaPoly(this.client, lp, size, true);
            case 2:
            case 3:
                lp = LocalPoint.fromWorld(this.client, this.bloatNPC.getWorldLocation());
                if (lp == null)
                    return null;
                return RoomOverlay.getCanvasTileAreaPoly(this.client, lp, size, false);
        }
        return null;
    }

    Color getBloatStateColor() {
        Color col = this.config.bloatIndicatorColorUP();
        switch (this.bloatState) {
            case 2:
                col = this.config.bloatIndicatorColorDOWN();
                break;
            case 3:
                col = this.config.bloatIndicatorColorWARN();
                break;
            case 4:
                col = this.config.bloatIndicatorColorTHRESH();
                break;
        }
        return col;
    }

    private boolean isInRegion() {
        return (this.client.getMapRegions() != null && (this.client.getMapRegions()).length > 0 && Arrays.stream(this.client.getMapRegions()).anyMatch(s -> (s == 13125)));
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!isInRegion())
            return;
        if (this.client.getItemContainer(InventoryID.INVENTORY) == null)
            return;
        String target = Text.removeTags(event.getTarget()).toLowerCase();
        MenuEntry[] entries = this.client.getMenuEntries();
        if ((this.config.stamReq() == SpoonTobConfig.stamReqMode.NYLO || this.config.stamReq() == SpoonTobConfig.stamReqMode.BOTH) && target
                .contains("formidable passage") && !this.client.getItemContainer(InventoryID.INVENTORY).contains(12625))
            this.client.setMenuEntries(Arrays.<MenuEntry>copyOf(entries, entries.length - 1));
    }
}
