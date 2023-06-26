package net.runelite.client.plugins.spoontob.rooms.Sotetseg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Random;
import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GroundObject;
import net.runelite.api.InventoryID;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.Projectile;
import net.runelite.api.Skill;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sotetseg extends Room {
    private static final Logger log = LoggerFactory.getLogger(Sotetseg.class);

    @Inject
    private Client client;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private SotetsegOverlay sotetsegOverlay;

    @Inject
    private DeathBallPanel deathBallPanel;

    @Inject
    private SkillIconManager iconManager;

    static final int SOTETSEG_MAGE_ORB = 1606;

    static final int SOTETSEG_RANGE_ORB = 1607;

    static final int SOTETSEG_BIG_AOE_ORB = 1604;

    private static final int GROUNDOBJECT_ID_REDMAZE = 33035;

    private static final int GROUNDOBJECT_ID_BLACKMAZE = 33034;

    private static final int GROUNDOBJECT_ID_GREYMAZE = 33033;

    private static final int OVERWORLD_REGION_ID = 13123;

    private static final int UNDERWORLD_REGION_ID = 13379;

    public static Point getSwMazeSquareOverWorld() {
        return swMazeSquareOverWorld;
    }

    private static final Point swMazeSquareOverWorld = new Point(9, 22);

    public static Point getSwMazeSquareUnderWorld() {
        return swMazeSquareUnderWorld;
    }

    private static final Point swMazeSquareUnderWorld = new Point(42, 31);

    private boolean bigOrbPresent = false;

    private static Clip clip;

    static BufferedImage TACTICAL_NUKE_OVERHEAD;

    private static BufferedImage TACTICAL_NUKE_SHEET;

    private static BufferedImage TACTICAL_NUKE_SHEET_BLANK;

    private String currentTopic = null;

    private boolean sotetsegActive;

    public NPC sotetsegNPC;

    public boolean isSotetsegActive() {
        return this.sotetsegActive;
    }

    private int overWorldRegionID = -1;

    private boolean wasInUnderWorld = false;

    public boolean isWasInUnderWorld() {
        return this.wasInUnderWorld;
    }

    private LinkedHashSet<Point> redTiles = new LinkedHashSet<>();

    public LinkedHashSet<Point> getRedTiles() {
        return this.redTiles;
    }

    private HashSet<Point> greenTiles = new HashSet<>();

    public HashSet<Point> getGreenTiles() {
        return this.greenTiles;
    }

    public byte sotetsegTicks = -1;

    public byte getSotetsegTicks() {
        return this.sotetsegTicks;
    }

    public boolean ballOutNigga = false;

    public int turboHatWidth = 0;

    public int turboHatHeight = 0;

    public int sotetsegAttacksLeft = 10;

    private boolean offTick = false;

    public int mageHatNum = 0;

    public int rangeHatNum = 0;

    public BufferedImage mageIcon;

    public BufferedImage rangeIcon;

    @Inject
    protected Sotetseg(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
    }

    public void init() {
        TACTICAL_NUKE_SHEET = ImageUtil.loadImageResource(SpoonTobPlugin.class, "/spoontob/nuke_spritesheet.png");
        TACTICAL_NUKE_OVERHEAD = ImageUtil.loadImageResource(SpoonTobPlugin.class, "/spoontob/Tactical_Nuke_Care_Package_Icon_MW2.png");
        TACTICAL_NUKE_SHEET_BLANK = new BufferedImage(TACTICAL_NUKE_SHEET.getWidth(), TACTICAL_NUKE_SHEET.getHeight(), TACTICAL_NUKE_SHEET.getType());
        Graphics2D graphics = TACTICAL_NUKE_SHEET_BLANK.createGraphics();
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(0, 0, TACTICAL_NUKE_SHEET.getWidth(), TACTICAL_NUKE_SHEET.getHeight());
        graphics.dispose();
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(SpoonTobPlugin.class.getResourceAsStream("/spoontob/mw2_tactical_nuke.wav")));
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip)AudioSystem.getLine(info);
            clip.open(stream);
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (control != null)
                control.setValue(20.0F * (float)Math.log10((this.config.sotetsegAttacksSoundVolume() / 100.0F)));
        } catch (Exception var6) {
            clip = null;
        }
    }

    public void load() {
        this.overlayManager.add((Overlay)this.sotetsegOverlay);
        this.overlayManager.add((Overlay)this.deathBallPanel);
        loadImages(this.config.soteHatSize());
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.sotetsegOverlay);
        this.overlayManager.remove((Overlay)this.deathBallPanel);
    }

    private void loadImages(int imageSize) {
        this.mageIcon = ImageUtil.resizeImage(this.iconManager.getSkillImage(Skill.MAGIC, true), imageSize, imageSize);
        this.rangeIcon = ImageUtil.resizeImage(this.iconManager.getSkillImage(Skill.RANGED, true), imageSize, imageSize);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged change) {
        if (change.getGroup().equals("spoontob")) {
            FloatControl control;
            if (change.getKey().equals("SotetsegAttacksSoundsVolume") && clip != null && (control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN)) != null) {
                control.setValue(20.0F * (float)Math.log10((this.config.sotetsegAttacksSoundVolume() / 100.0F)));
            } else if (change.getKey().equals("soteHatSize")) {
                loadImages(this.config.soteHatSize());
            }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
            case 10864:
            case 10865:
            case 10867:
            case 10868:
                this.sotetsegNPC = npc;
                if (!this.sotetsegActive) {
                    this.sotetsegActive = true;
                    this.sotetsegAttacksLeft = 10;
                }
                break;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8387:
            case 8388:
            case 10864:
            case 10865:
            case 10867:
            case 10868:
                if (this.client.getPlane() != 3) {
                    this.sotetsegActive = false;
                    this.sotetsegNPC = null;
                    this.sotetsegTicks = -1;
                }
                if (npc.isDead())
                    this.sotetsegAttacksLeft = 10;
                break;
        }
    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved e) {
        if (this.sotetsegActive) {
            Projectile p = e.getProjectile();
            if (this.client.getGameCycle() < p.getStartCycle()) {
                WorldPoint soteWp;
                WorldPoint projWp;
                switch (p.getId()) {
                    case 1604:
                        this.sotetsegTicks = 11;
                        this.sotetsegAttacksLeft = 10;
                        break;
                    case 1606:
                        soteWp = WorldPoint.fromLocal(this.client, this.sotetsegNPC.getLocalLocation());
                        projWp = WorldPoint.fromLocal(this.client, p.getX1(), p.getY1(), this.client.getPlane());
                        if (this.sotetsegNPC.getAnimation() == 8139 && projWp.equals(soteWp))
                            this.sotetsegAttacksLeft--;
                        break;
                }
            }
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        Actor actor = event.getActor();
        if (actor instanceof NPC &&
                actor == this.sotetsegNPC) {
            int animation = event.getActor().getAnimation();
            if (animation == 8138 || animation == 8139)
                this.sotetsegTicks = 6;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.sotetsegActive) {
            int rng = (new Random()).nextInt(8) + 1;
            if (this.mageHatNum == rng) {
                if (this.mageHatNum == 8) {
                    this.mageHatNum = 7;
                } else {
                    this.mageHatNum++;
                }
            } else {
                this.mageHatNum = rng;
            }
            if (this.rangeHatNum == rng) {
                if (this.rangeHatNum == 8) {
                    this.rangeHatNum = 7;
                } else {
                    this.rangeHatNum++;
                }
            } else {
                this.rangeHatNum = rng;
            }
            this.turboHatWidth = (new Random()).nextInt(35) + 10;
            this.turboHatHeight = (new Random()).nextInt(25) + 10;
            if (this.sotetsegTicks >= 0)
                this.sotetsegTicks = (byte)(this.sotetsegTicks - 1);
            if (this.sotetsegNPC != null && (this.sotetsegNPC.getId() == 8388 || this.sotetsegNPC.getId() == 10865 || this.sotetsegNPC.getId() == 10868)) {
                if (!this.redTiles.isEmpty()) {
                    this.redTiles.clear();
                    this.offTick = false;
                }
                if (!this.greenTiles.isEmpty())
                    this.greenTiles.clear();
                if (isInOverWorld()) {
                    this.wasInUnderWorld = false;
                    if (this.client.getLocalPlayer() != null && this.client.getLocalPlayer().getWorldLocation() != null)
                        this.overWorldRegionID = this.client.getLocalPlayer().getWorldLocation().getRegionID();
                }
            }
            if (this.config.sotetsegShowNuke() != SpoonTobConfig.soteDeathballOverlayMode.OFF) {
                boolean foundBigOrb = false;
                for (Projectile p : this.client.getProjectiles()) {
                    if (p.getId() == 1604) {
                        foundBigOrb = true;
                        if (!this.bigOrbPresent && clip != null && this.config.sotetsegAttacksSound()) {
                            clip.setFramePosition(0);
                            clip.start();
                        }
                        break;
                    }
                }
                this.bigOrbPresent = foundBigOrb;
            }
            if (!this.bigOrbPresent)
                this.ballOutNigga = false;
            if (this.bigOrbPresent && !this.ballOutNigga) {
                this.sotetsegTicks = 10;
                this.ballOutNigga = true;
            }
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        if (this.sotetsegActive) {
            GroundObject o = event.getGroundObject();
            if (o.getId() == 33035) {
                Tile t = event.getTile();
                WorldPoint p = WorldPoint.fromLocal(this.client, t.getLocalLocation());
                Point point = new Point(p.getRegionX(), p.getRegionY());
                if (isInOverWorld())
                    this.redTiles.add(new Point(point.getX() - swMazeSquareOverWorld.getX(), point.getY() - swMazeSquareOverWorld.getY()));
                if (isInUnderWorld()) {
                    this.redTiles.add(new Point(point.getX() - swMazeSquareUnderWorld.getX(), point.getY() - swMazeSquareUnderWorld.getY()));
                    this.wasInUnderWorld = true;
                }
            }
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!isInOverWorld())
            return;
        if (this.client.getItemContainer(InventoryID.INVENTORY) == null)
            return;
        String target = Text.removeTags(event.getTarget()).toLowerCase();
        MenuEntry[] entries = this.client.getMenuEntries();
        if ((this.config.stamReq() == SpoonTobConfig.stamReqMode.XARPUS || this.config.stamReq() == SpoonTobConfig.stamReqMode.BOTH) && this.config.stamReq() != SpoonTobConfig.stamReqMode.OFF && target
                .contains("formidable passage") && !this.client.getItemContainer(InventoryID.INVENTORY).contains(12625))
            this.client.setMenuEntries(Arrays.<MenuEntry>copyOf(entries, entries.length - 1));
    }

    WorldPoint worldPointFromMazePoint(Point mazePoint) {
        if (this.overWorldRegionID == -1 && this.client.getLocalPlayer() != null)
            return WorldPoint.fromRegion(this.client
                    .getLocalPlayer().getWorldLocation().getRegionID(), mazePoint.getX() + getSwMazeSquareOverWorld().getX(), mazePoint
                    .getY() + getSwMazeSquareOverWorld().getY(), 0);
        return WorldPoint.fromRegion(this.overWorldRegionID, mazePoint
                .getX() + getSwMazeSquareOverWorld().getX(), mazePoint
                .getY() + getSwMazeSquareOverWorld().getY(), 0);
    }

    private boolean isInOverWorld() {
        return ((this.client.getMapRegions()).length > 0 && this.client.getMapRegions()[0] == 13123);
    }

    private boolean isInUnderWorld() {
        return ((this.client.getMapRegions()).length > 0 && this.client.getMapRegions()[0] == 13379);
    }
}
