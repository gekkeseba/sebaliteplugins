package net.runelite.client.plugins.spoontob.rooms.Xarpus;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GroundObjectSpawned;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.OverheadTextChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.AlternateSprites;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.infobox.Counter;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.tuple.Pair;

public class Xarpus extends Room {
    private static BufferedImage EXHUMED_COUNT_ICON;

    private static final int GROUNDOBJECT_ID_EXHUMED = 32743;

    protected static final Set<Integer> P0_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8338), Integer.valueOf(10766), Integer.valueOf(10770));

    protected static final Set<Integer> P1_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8339), Integer.valueOf(10767), Integer.valueOf(10771));

    protected static final Set<Integer> P2_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8340), Integer.valueOf(10768), Integer.valueOf(10772));

    protected static final Set<Integer> P3_IDS = (Set<Integer>)ImmutableSet.of(
            Integer.valueOf(8341), Integer.valueOf(10769), Integer.valueOf(10773));

    @Inject
    private XarpusOverlay xarpusOverlay;

    @Inject
    private XarpusCounterPanel xarpusPanel;

    @Inject
    private XarpusTimer xarpusTimer;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private Client client;

    int exhumedCount = 0;

    int healCount = 0;

    private boolean xarpusStarted = false;

    private static Clip clip;

    private ExhumedInfobox exhumedCounter;

    private static BufferedImage HEALED_COUNT_ICON;

    private Counter xarpusHealedCounter;

    private boolean xarpusActive;

    public boolean xarpusStare;

    public ExhumedInfobox getExhumedCounter() {
        return this.exhumedCounter;
    }

    public Counter getXarpusHealedCounter() {
        return this.xarpusHealedCounter;
    }

    public boolean isXarpusActive() {
        return this.xarpusActive;
    }

    public boolean isXarpusStare() {
        return this.xarpusStare;
    }

    private final Map<Long, Pair<GroundObject, Integer>> xarpusExhumeds = new HashMap<>();

    private int xarpusTicksUntilAttack;

    private NPC xarpusNPC;

    public Map<Long, Pair<GroundObject, Integer>> getXarpusExhumeds() {
        return this.xarpusExhumeds;
    }

    public int getXarpusTicksUntilAttack() {
        return this.xarpusTicksUntilAttack;
    }

    public NPC getXarpusNPC() {
        return this.xarpusNPC;
    }

    private boolean exhumedSpawned = false;

    public boolean isExhumedSpawned() {
        return this.exhumedSpawned;
    }

    private int instanceTimer = 0;

    public int getInstanceTimer() {
        return this.instanceTimer;
    }

    private boolean isInstanceTimerRunning = false;

    public boolean isInstanceTimerRunning() {
        return this.isInstanceTimerRunning;
    }

    private boolean nextInstance = true;

    private boolean isHM = false;

    public boolean isHM() {
        return this.isHM;
    }

    private boolean isP3Active = false;

    public boolean isP3Active() {
        return this.isP3Active;
    }

    @Inject
    protected Xarpus(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
    }

    public void init() {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(SpoonTobPlugin.class.getResourceAsStream("/spoontob/sheesh.wav")));
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip)AudioSystem.getLine(info);
            clip.open(stream);
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            if (control != null)
                control.setValue((this.config.sheeshVolume() / 2 - 45));
        } catch (Exception var6) {
            clip = null;
        }
        EXHUMED_COUNT_ICON = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(AlternateSprites.class, "1067-POISON.png"), 26, 26);
        HEALED_COUNT_ICON = ImageUtil.resizeCanvas(ImageUtil.loadImageResource(SpoonTobPlugin.class, "/spoontob/healsplat.png"), 26, 26);
    }

    public void load() {
        this.overlayManager.add((Overlay)this.xarpusOverlay);
        this.overlayManager.add((Overlay)this.xarpusTimer);
        this.overlayManager.add((Overlay)this.xarpusPanel);
    }

    public void unload() {
        this.overlayManager.remove((Overlay)this.xarpusOverlay);
        this.overlayManager.remove((Overlay)this.xarpusPanel);
        this.overlayManager.remove((Overlay)this.xarpusTimer);
        this.xarpusStarted = false;
        this.healCount = 0;
        this.infoBoxManager.removeInfoBox(this.exhumedCounter);
        this.exhumedCounter = null;
        this.infoBoxManager.removeInfoBox((InfoBox)this.xarpusHealedCounter);
        this.xarpusHealedCounter = null;
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("spoontob")) {
            if (event.getKey().equals("sheeshVolume") &&
                    clip != null) {
                FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                if (control != null)
                    control.setValue((this.config.sheeshVolume() / 2 - 45));
            }
            if (event.getKey().equals("exhumedIB"))
                if (this.config.exhumedIB()) {
                    this.infoBoxManager.addInfoBox(this.exhumedCounter);
                } else {
                    this.infoBoxManager.removeInfoBox(this.exhumedCounter);
                }
            if (event.getKey().equals("xarpusHealingCount"))
                if (this.config.xarpusHealingCount()) {
                    this.infoBoxManager.addInfoBox((InfoBox)this.xarpusHealedCounter);
                } else {
                    this.infoBoxManager.removeInfoBox((InfoBox)this.xarpusHealedCounter);
                }
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        this.isHM = false;
        this.isP3Active = false;
        NPC npc = npcSpawned.getNpc();
        switch (npc.getId()) {
            case 10770:
            case 10771:
            case 10772:
            case 10773:
                this.isHM = true;
            case 8338:
            case 8339:
            case 8340:
            case 8341:
            case 10766:
            case 10767:
            case 10768:
            case 10769:
                this.xarpusActive = true;
                this.xarpusNPC = npc;
                this.xarpusStare = false;
                this.xarpusTicksUntilAttack = 9;
                this.healCount = 0;
                this.exhumedSpawned = false;
                break;
        }
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8338:
            case 8339:
            case 8340:
            case 8341:
            case 10766:
            case 10767:
            case 10768:
            case 10769:
            case 10770:
            case 10771:
            case 10772:
            case 10773:
                this.xarpusActive = false;
                this.xarpusNPC = null;
                this.xarpusStare = false;
                this.xarpusTicksUntilAttack = 9;
                this.xarpusExhumeds.clear();
                this.xarpusStarted = false;
                this.isInstanceTimerRunning = false;
                this.healCount = 0;
                this.exhumedSpawned = false;
                this.infoBoxManager.removeInfoBox(this.exhumedCounter);
                this.exhumedCounter = null;
                this.exhumedCount = -1;
                removeCounter();
                break;
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        NPC npc = event.getNpc();
        if (this.xarpusActive && (
                P2_IDS.contains(Integer.valueOf(npc.getId())) || P3_IDS.contains(Integer.valueOf(npc.getId())))) {
            this.infoBoxManager.removeInfoBox(this.exhumedCounter);
            this.exhumedCounter = null;
        }
    }

    @Subscribe
    public void onGroundObjectSpawned(GroundObjectSpawned event) {
        if (this.xarpusActive) {
            GroundObject o = event.getGroundObject();
            if (o.getId() == 32743) {
                long hash = o.getHash();
                if (this.xarpusExhumeds.containsKey(Long.valueOf(hash)))
                    return;
                this.exhumedSpawned = true;
                if (this.exhumedCounter == null) {
                    switch (SpoonTobPlugin.partySize) {
                        case 5:
                            this.exhumedCount = this.isHM ? 24 : 18;
                            break;
                        case 4:
                            this.exhumedCount = this.isHM ? 20 : 15;
                            break;
                        case 3:
                            this.exhumedCount = this.isHM ? 16 : 12;
                            break;
                        case 2:
                            this.exhumedCount = this.isHM ? 13 : 9;
                            break;
                        default:
                            this.exhumedCount = this.isHM ? 9 : 7;
                            break;
                    }
                    this.exhumedCounter = new ExhumedInfobox(EXHUMED_COUNT_ICON, (Plugin)this.plugin, this.exhumedCount - 1);
                    if (this.config.exhumedIB()) {
                        this.infoBoxManager.addInfoBox(this.exhumedCounter);
                        this.exhumedCounter.setTooltip(ColorUtil.wrapWithColorTag((this.exhumedCounter.getCount() > 0) ? ("Exhumeds Left: " +
                                this.exhumedCounter.getCount()) : "NOW", (this.exhumedCounter.getCount() <= 1) ? Color.RED : Color.WHITE));
                    }
                } else {
                    this.exhumedCounter.setCount(this.exhumedCounter.getCount() - 1);
                    this.exhumedCounter.setTooltip(ColorUtil.wrapWithColorTag((this.exhumedCounter.getCount() > 0) ? ("Exhumeds Left: " +
                            this.exhumedCounter.getCount()) : "NOW", (this.exhumedCounter.getCount() <= 1) ? Color.RED : Color.WHITE));
                }
                this.xarpusExhumeds.put(Long.valueOf(hash), Pair.of(o, Integer.valueOf(this.isHM ? 9 : 11)));
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (this.xarpusActive) {
            if (!this.xarpusExhumeds.isEmpty()) {
                this.xarpusExhumeds.replaceAll((k, v) -> Pair.of(v.getLeft(), Integer.valueOf(((Integer)v.getRight()).intValue() - 1)));
                this.xarpusExhumeds.values().removeIf(p -> (((Integer)p.getRight()).intValue() <= 0));
            }
            if (this.xarpusNPC.getOverheadText() != null && !this.xarpusStare) {
                this.xarpusStare = true;
                this.xarpusTicksUntilAttack = 9;
            }
            if (this.xarpusStare) {
                this.xarpusTicksUntilAttack--;
                if (this.xarpusTicksUntilAttack <= 0) {
                    this.xarpusTicksUntilAttack = 8;
                    this.isP3Active = true;
                }
                this.infoBoxManager.removeInfoBox(this.exhumedCounter);
            } else if (P2_IDS.contains(Integer.valueOf(this.xarpusNPC.getId()))) {
                this.xarpusTicksUntilAttack--;
                if (this.xarpusTicksUntilAttack <= 0)
                    this.xarpusTicksUntilAttack = 4;
            }
        }
        this.instanceTimer = (this.instanceTimer + 1) % 4;
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if (event.getActor() != null && event.getActor().getName() != null &&
                event.getActor().getName().toLowerCase().contains("xarpus") && event.getHitsplat().getHitsplatType() == 6) {
            this.healCount += event.getHitsplat().getAmount();
            addCounter();
            updateCounter();
        }
    }

    private void updateCounter() {
        if (this.xarpusHealedCounter != null)
            this.xarpusHealedCounter.setCount(this.healCount);
    }

    private void addCounter() {
        if (this.config.xarpusHealingCount() && this.xarpusHealedCounter == null) {
            this.xarpusHealedCounter = new Counter(HEALED_COUNT_ICON, (Plugin)this.plugin, this.healCount);
            this.xarpusHealedCounter.setTooltip("Xarpus Heals");
            this.infoBoxManager.addInfoBox((InfoBox)this.xarpusHealedCounter);
        }
    }

    private void removeCounter() {
        if (this.xarpusHealedCounter != null) {
            this.infoBoxManager.removeInfoBox((InfoBox)this.xarpusHealedCounter);
            this.healCount = 0;
            this.xarpusHealedCounter = null;
        }
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        if ((this.client.getVarbitValue(4605) == 1 || this.client.getVarbitValue(this.client.getVarps(), 6447) == 2) && !this.xarpusStarted && this.isInstanceTimerRunning) {
            this.isInstanceTimerRunning = false;
            this.xarpusStarted = true;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
            this.nextInstance = true;
    }

    @Subscribe
    protected void onClientTick(ClientTick event) {
        if (this.client.getLocalPlayer() == null)
            return;
        List<Player> players = this.client.getPlayers();
        for (Player player : players) {
            if (player.getWorldLocation() != null) {
                WorldPoint wpPlayer = player.getWorldLocation();
                LocalPoint lpPlayer = LocalPoint.fromWorld(this.client, wpPlayer.getX(), wpPlayer.getY());
                if (lpPlayer == null)
                    continue;
                WorldPoint wpChest = WorldPoint.fromRegion(player.getWorldLocation().getRegionID(), 17, 5, player.getWorldLocation().getPlane());
                LocalPoint lpChest = LocalPoint.fromWorld(this.client, wpChest.getX(), wpChest.getY());
                if (lpChest != null) {
                    Point point = new Point(lpChest.getSceneX() - lpPlayer.getSceneX(), lpChest.getSceneY() - lpPlayer.getSceneY());
                    if (isInSotetsegRegion() && point.getY() == 1 && (point.getX() == 1 || point.getX() == 2 || point.getX() == 3) && this.nextInstance) {
                        this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", "Xarpus instance timer started", "", false);
                        this.instanceTimer = 2;
                        this.isInstanceTimerRunning = true;
                        this.nextInstance = false;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event) {
        if (this.xarpusActive && this.xarpusNPC != null)
            if (event.getSoundId() == 4005 && getXarpusNPC().getId() >= 10770 && getXarpusNPC().getId() <= 10773 && this.config.muteXarpusHmEarrape()) {
                event.consume();
            } else if (event.getSoundId() == 4007 && this.config.sheesh()) {
                event.consume();
            }
    }

    @Subscribe
    public void onOverheadTextChanged(OverheadTextChanged event) {
        if (event.getActor() instanceof NPC && this.config.sheesh() && this.xarpusActive) {
            NPC npc = (NPC)event.getActor();
            if (npc.getId() == this.xarpusNPC.getId()) {
                event.getActor().setOverheadText("Sheeeeeesh!");
                clip.setFramePosition(0);
                clip.start();
            }
        }
    }

    public boolean isInXarpusRegion() {
        return (this.client.getMapRegions() != null && (this.client.getMapRegions()).length > 0 && Arrays.stream(this.client.getMapRegions()).anyMatch(s -> (s == 12612)));
    }

    protected boolean isInSotetsegRegion() {
        return (this.client.getMapRegions() != null && (this.client.getMapRegions()).length > 0 && Arrays.stream(this.client.getMapRegions()).anyMatch(s -> (s == 13123 || s == 13379)));
    }

    public static class ExhumedInfobox extends InfoBox {
        private int count;

        public String toString() {
            return "Xarpus.ExhumedInfobox(count=" + getCount() + ")";
        }

        public int getCount() {
            return this.count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public ExhumedInfobox(BufferedImage image, Plugin plugin, int count) {
            super(image, plugin);
            this.count = count;
        }

        public String getText() {
            return Integer.toString(getCount());
        }

        public Color getTextColor() {
            if (this.count <= 1)
                return Color.RED;
            return Color.WHITE;
        }
    }
}
