package net.runelite.client.plugins.spoontob;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.Renderable;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.callback.Hooks;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.spoontob.rooms.Bloat.Bloat;
import net.runelite.client.plugins.spoontob.rooms.Maiden.Maiden;
import net.runelite.client.plugins.spoontob.rooms.Maiden.MaidenRedsOverlay;
import net.runelite.client.plugins.spoontob.rooms.Nylocas.NyloInfo;
import net.runelite.client.plugins.spoontob.rooms.Nylocas.Nylocas;
import net.runelite.client.plugins.spoontob.rooms.Sotetseg.Sotetseg;
import net.runelite.client.plugins.spoontob.rooms.Verzik.Verzik;
import net.runelite.client.plugins.spoontob.rooms.Xarpus.Xarpus;
import net.runelite.client.plugins.spoontob.util.CustomGameObject;
import net.runelite.client.plugins.spoontob.util.RaveUtils;
import net.runelite.client.plugins.spoontob.util.TheatreInputListener;
import net.runelite.client.plugins.spoontob.util.TheatreRegions;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(name = "Spoontob", description = " useful 1 plugin", tags = {"bb2"}, enabledByDefault = false)
public class SpoonTobPlugin extends Plugin {
    private Room[] rooms = null;

    @Inject
    private EventBus eventBus;

    @Inject
    private Maiden maiden;

    @Inject
    private Bloat bloat;

    @Inject
    private Nylocas nylocas;

    @Inject
    private Sotetseg sotetseg;

    @Inject
    private Xarpus xarpus;

    @Inject
    private Verzik verzik;

    @Inject
    private Client client;

    @Inject
    private TheatreInputListener theatreInputListener;

    @Inject
    private ClientThread clientThread;

    @Inject
    private SpoonTobConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private MiscOverlay miscOverlay;

    @Inject
    private SituationalTickOverlay tickOverlay;

    @Inject
    private MaidenRedsOverlay redsOverlay;

    @Inject
    public RaveUtils raveUtils;

    @Inject
    private Hooks hooks;

    private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

    public Color c;

    private Color rave;

    private final Set<CustomGameObject> customizedGameObjects = new LinkedHashSet<>();

    private final ArrayListMultimap<String, Integer> optionIndexes = ArrayListMultimap.create();

    private String roomCompleteMsg = "";

    public GameObject bankLootChest = null;

    public Color raveBankChestColor = Color.WHITE;

    public Color flowColor = new Color(75, 25, 150, 255);

    private boolean raveRedUp = true;

    private boolean raveGreenUp = true;

    private boolean raveBlueUp = true;

    public ArrayList<Integer> hiddenIndices;

    public HashMap<Player, Integer> situationalTicksList = new HashMap<>();

    public HashMap<Player, Integer> getSituationalTicksList() {
        return this.situationalTicksList;
    }

    public int situationalTicks = 0;

    public static int partySize;

    public void configure(Binder binder) {
        binder.bind(TheatreInputListener.class);
    }

    @Provides
    SpoonTobConfig getConfig(ConfigManager configManager) {
        return (SpoonTobConfig)configManager.getConfig(SpoonTobConfig.class);
    }

    protected void startUp() {
        this.situationalTicksList.clear();
        this.overlayManager.add(this.miscOverlay);
        this.overlayManager.add(this.tickOverlay);
        this.bankLootChest = null;
        this.roomCompleteMsg = "";
        this.raveBankChestColor = Color.WHITE;
        if (this.rooms == null) {
            this.rooms = new Room[] { (Room)this.maiden, (Room)this.bloat, (Room)this.nylocas, (Room)this.sotetseg, (Room)this.xarpus, (Room)this.verzik };
            for (Room room : this.rooms)
                room.init();
        }
        for (Room room : this.rooms) {
            room.load();
            this.eventBus.register(room);
        }
        this.hooks.registerRenderableDrawListener(this.drawListener);
        this.hiddenIndices = new ArrayList<>();
    }

    protected void shutDown() {
        this.situationalTicksList.clear();
        this.overlayManager.remove(this.miscOverlay);
        this.overlayManager.remove(this.tickOverlay);
        this.bankLootChest = null;
        modifyCustomObjList(true, true);
        this.roomCompleteMsg = "";
        this.raveBankChestColor = Color.WHITE;
        for (Room room : this.rooms) {
            this.eventBus.unregister(room);
            room.unload();
        }
        this.hooks.unregisterRenderableDrawListener(this.drawListener);
        clearHiddenNpcs();
        this.hiddenIndices = null;
        this.situationalTicks = 0;
    }

    public void refreshScene() {
        this.clientThread.invokeLater(() -> this.client.setGameState(GameState.LOADING));
    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.config.removeFRCFlag() && this.client.getGameState() == GameState.LOGGED_IN && !this.client.isMenuOpen() && (
                TheatreRegions.inRegion(this.client, TheatreRegions.LOOT_ROOM) || isLootingNonLootRoomChest())) {
            MenuEntry[] entries = this.client.getMenuEntries();
            for (MenuEntry entry : entries) {
                if (entry.getOption().equals("Bank-all")) {
                    entry.setForceLeftClick(true);
                    break;
                }
            }
            this.client.setMenuEntries(entries);
        }
        if (this.client.getGameState() != GameState.LOGGED_IN || this.client.isMenuOpen())
            return;
        MenuEntry[] menuEntries = this.client.getMenuEntries();
        int idx = 0;
        this.optionIndexes.clear();
        for (MenuEntry entry : menuEntries) {
            String option = Text.removeTags(entry.getOption()).toLowerCase();
            this.optionIndexes.put(option, Integer.valueOf(idx++));
        }
        idx = 0;
        for (MenuEntry entry : menuEntries)
            swapMenuEntry(idx++, entry);
        flowColor();
    }

    private void swapMenuEntry(int index, MenuEntry menuEntry) {
        int eventId = menuEntry.getIdentifier();
        String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
        String target = Text.removeTags(menuEntry.getTarget()).toLowerCase();
        String[] FreezeSpells = { "ice barrage", "ice burst", "ice blitz", "ice rush", "entangle", "snare", "bind", "blood barrage", "blood barrage", "smoke barrage" };
        String[] LunarSpells = { "energy transfer", "heal other", "vengeance other" };
        MenuEntry[] newEntries = this.client.getMenuEntries();
        if (this.config.removeCastToB() && enforceRegion())
            for (String spell : FreezeSpells) {
                if (target.startsWith(spell + " ->") && (menuEntry
                        .getType().getId() != 8 || target.contains("greater skeletal thrall") || target.contains("greater zombified  thrall") || target.contains("greater ghostly thrall"))) {
                    delete(menuEntry, newEntries);
                    return;
                }
            }
        if (option.equals("value") && this.config.swapTobBuys() && enforceRegion())
            swap("buy-1", option, target, index);
    }

    private void swap(String optionA, String optionB, String target, int index) {
        swap(optionA, optionB, target, index, true);
    }

    private void swap(String optionA, String optionB, String target, int index, boolean strict) {
        MenuEntry[] menuEntries = this.client.getMenuEntries();
        int thisIndex = findIndex(menuEntries, index, optionB, target, strict);
        int optionIdx = findIndex(menuEntries, thisIndex, optionA, target, strict);
        if (thisIndex >= 0 && optionIdx >= 0)
            swap(this.optionIndexes, menuEntries, optionIdx, thisIndex);
    }

    private int findIndex(MenuEntry[] entries, int limit, String option, String target, boolean strict) {
        if (strict) {
            List<Integer> indexes = this.optionIndexes.get(option);
            for (int i = indexes.size() - 1; i >= 0; i--) {
                int idx = ((Integer)indexes.get(i)).intValue();
                MenuEntry entry = entries[idx];
                String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();
                if (idx <= limit && entryTarget.equals(target))
                    return idx;
            }
        } else {
            for (int i = limit; i >= 0; i--) {
                MenuEntry entry = entries[i];
                String entryOption = Text.removeTags(entry.getOption()).toLowerCase();
                String entryTarget = Text.removeTags(entry.getTarget()).toLowerCase();
                if (entryOption.contains(option.toLowerCase()) && entryTarget.equals(target))
                    return i;
            }
        }
        return -1;
    }

    private void swap(ArrayListMultimap<String, Integer> optionIndexes, MenuEntry[] entries, int index1, int index2) {
        MenuEntry entry = entries[index1];
        entries[index1] = entries[index2];
        entries[index2] = entry;
        this.client.setMenuEntries(entries);
        optionIndexes.clear();
        int idx = 0;
        for (MenuEntry menuEntry : entries) {
            String option = Text.removeTags(menuEntry.getOption()).toLowerCase();
            optionIndexes.put(option, Integer.valueOf(idx++));
        }
    }

    private void delete(MenuEntry entry, MenuEntry[] newEntries) {
        for (int i = newEntries.length - 1; i >= 0; i--) {
            if (newEntries[i].equals(entry))
                newEntries = (MenuEntry[])ArrayUtils.remove((Object[])newEntries, i);
        }
        this.client.setMenuEntries(newEntries);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (this.config.verzikTeleportCrystalHelper() && Text.removeTags(event.getTarget()).contains("Verzik's crystal shard -> ") && event.getOption().equalsIgnoreCase("use")) {
            boolean keepEntry = false;
            for (Player player : this.client.getPlayers()) {
                if (player.getName() != null && event.getTarget().replaceAll("[^A-Za-z0-9]", " ").contains(player.getName()))
                    keepEntry = true;
            }
            if (!keepEntry) {
                MenuEntry[] entries = this.client.getMenuEntries();
                MenuEntry[] newEntries = new MenuEntry[entries.length - 1];
                System.arraycopy(entries, 0, newEntries, 0, newEntries.length);
                this.client.setMenuEntries(newEntries);
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (enforceRegion()) {
            partySize = 0;
            for (int i = 330; i < 335; i++) {
                if (this.client.getVarcStrValue(i) != null && !this.client.getVarcStrValue(i).equals(""))
                    partySize++;
            }
        }
        this.raveBankChestColor = Color.getHSBColor((new Random()).nextFloat(), 1.0F, 1.0F);
        if (enforceRegion()) {
            Random random = new Random();
            float hue = random.nextFloat();
            this.rave = Color.getHSBColor(hue, 0.9F, 1.0F);
            modifyCustomObjList(false, false);
            ArrayList<Player> toRemove0 = new ArrayList<>();
            for (Player n : this.situationalTicksList.keySet()) {
                int i = ((Integer)this.situationalTicksList.get(n)).intValue();
                if (i - 1 == 0) {
                    toRemove0.add(n);
                    continue;
                }
                this.situationalTicksList.put(n, Integer.valueOf(i - 1));
            }
            for (Player n : toRemove0)
                this.situationalTicksList.remove(n);
        } else if (this.config.recolorBarriers() != SpoonTobConfig.barrierMode.OFF &&
                !this.client.isInInstancedRegion()) {
            Player you = this.client.getLocalPlayer();
            if (you != null) {
                WorldPoint wp = you.getWorldLocation();
                if (wp.getRegionID() == 14642)
                    modifyCustomObjList(true, true);
            }
        }
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals("spoontob"))
            if (event.getKey().equals("recolorBarriers") || event.getKey().equals("barriersColor")) {
                modifyCustomObjList(true, false);
                modifyCustomObjList(false, false);
            } else if (event.getKey().equals("lootReminder") && (
                    this.config.lootReminder() == SpoonTobConfig.lootReminderMode.OFF || this.config.lootReminder() == SpoonTobConfig.lootReminderMode.DUMB) &&
                    this.client.hasHintArrow()) {
                this.client.clearHintArrow();
            }
    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject obj = event.getGameObject();
        int id = obj.getId();
        if (id == 32755 || id == 33028) {
            this.customizedGameObjects.add(new CustomGameObject(obj, id));
            modifyCustomObjList(false, false);
        } else if (id == 41437) {
            this.bankLootChest = obj;
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        if (event.getGameObject().getId() == 41437)
            this.bankLootChest = null;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (this.config.lootReminder() != SpoonTobConfig.lootReminderMode.OFF && this.bankLootChest != null && this.client.getLocalPlayer() != null)
            if (this.client.isInInstancedRegion()) {
                if (WorldPoint.fromLocalInstance(this.client, this.client.getLocalPlayer().getLocalLocation()).getRegionID() != 14642) {
                    this.bankLootChest = null;
                    if (this.client.hasHintArrow())
                        this.client.clearHintArrow();
                }
            } else if (this.client.getLocalPlayer().getWorldLocation().getRegionID() != 14642) {
                this.bankLootChest = null;
                if (this.client.hasHintArrow())
                    this.client.clearHintArrow();
            }
    }

    private void modifyCustomObjList(boolean restore, boolean clear) {
        if (!this.customizedGameObjects.isEmpty())
            if (restore) {
                List<CustomGameObject> objs = new ArrayList<>(this.customizedGameObjects);
                Lists.reverse(objs).forEach(CustomGameObject::restore);
                if (clear)
                    this.customizedGameObjects.clear();
            } else if (this.config.recolorBarriers() == SpoonTobConfig.barrierMode.COLOR) {
                this.customizedGameObjects.forEach(o -> o.setFaceColorValues(this.config.barriersColor()));
            } else if (this.config.recolorBarriers() == SpoonTobConfig.barrierMode.RAVE) {
                this.customizedGameObjects.forEach(o -> o.setFaceColorValues(this.rave));
            }
    }

    private boolean isLootingNonLootRoomChest() {
        if (this.client.isInInstancedRegion())
            return false;
        Player you = this.client.getLocalPlayer();
        if (you == null)
            return false;
        WorldPoint wp = you.getWorldLocation();
        if (wp.getRegionID() != 14642)
            return false;
        Widget widget = this.client.getWidget(1507328);
        return (widget != null && !widget.isHidden());
    }

    public boolean crossedLine(int region, Point start, Point end, boolean vertical) {
        if (inRegion(new int[] { region }))
            for (Player p : this.client.getPlayers()) {
                WorldPoint wp = p.getWorldLocation();
                if (vertical) {
                    for (int j = start.getY(); j < end.getY() + 1; j++) {
                        if (wp.getRegionY() == j && wp.getRegionX() == start.getX())
                            return true;
                    }
                    continue;
                }
                for (int i = start.getX(); i < end.getX() + 1; i++) {
                    if (wp.getRegionX() == i && wp.getRegionY() == start.getY())
                        return true;
                }
            }
        return false;
    }

    public boolean enforceRegion() {
        return inRegion(new int[] { 12611, 12612, 12613, 12687, 13122, 13123, 13125, 13379 });
    }

    public boolean inRegion(int... regions) {
        if (this.client.getMapRegions() != null)
            for (int i : this.client.getMapRegions()) {
                for (int j : regions) {
                    if (i == j)
                        return true;
                }
            }
        return false;
    }

    public void setHiddenNpc(NPC npc, boolean hidden) {
        if (hidden) {
            this.hiddenIndices.add(Integer.valueOf(npc.getIndex()));
        } else if (this.hiddenIndices.contains(Integer.valueOf(npc.getIndex()))) {
            this.hiddenIndices.remove(Integer.valueOf(npc.getIndex()));
        }
    }

    public void clearHiddenNpcs() {
        this.hiddenIndices.clear();
    }

    @VisibleForTesting
    boolean shouldDraw(Renderable renderable, boolean drawingUI) {
        if (renderable instanceof NPC) {
            NPC npc = (NPC)renderable;
            return !this.hiddenIndices.contains(Integer.valueOf(npc.getIndex()));
        }
        return true;
    }

    private void SocketDeathIntegration(int passedIndex) {
        for (NyloInfo ni : this.nylocas.nylocasNpcs) {
            if (passedIndex == ni.nylo.getIndex())
                ni.alive = false;
        }
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (event.getActor() instanceof Player && event.getActor() != null) {
            Player player = (Player)event.getActor();
            int anim = player.getAnimation();
            int hammerBop = 401;
            int godBop = 7045;
            int clawSpec = 7514;
            int clawBop = 393;
            int whip = 1658;
            int blade = 390;
            int rapier = 8145;
            int chalyBop = 440;
            int chalySpec = 1203;
            int scy = 8056;
            int bggsSpec = 7643;
            int bggsSpec2 = 7642;
            int hammerSpec = 1378;
            int trident = 1167;
            int surge = 7855;
            int ticks = 0;
            if (anim == scy)
                ticks = 5;
            if (anim == clawBop || anim == whip || anim == clawSpec || anim == trident || anim == surge || anim == blade || anim == rapier)
                ticks = 4;
            if (anim == chalySpec || anim == chalyBop)
                ticks = 7;
            if (anim == hammerSpec || anim == hammerBop || anim == bggsSpec || anim == bggsSpec2 || anim == godBop || anim == 7516)
                ticks = 6;
            if (ticks != 0) {
                if (this.client.getLocalPlayer() != null && player == this.client.getLocalPlayer())
                    this.situationalTicks = ticks;
                this.situationalTicksList.put(player, Integer.valueOf(ticks + 1));
            }
        }
    }

    public Color calculateHitpointsColor(double hpPercent) {
        hpPercent = Math.max(Math.min(100.0D, hpPercent), 0.0D);
        double rMod = 130.0D * hpPercent / 100.0D;
        double gMod = 235.0D * hpPercent / 100.0D;
        double bMod = 125.0D * hpPercent / 100.0D;
        int r = (int)Math.min(255.0D, 255.0D - rMod);
        int g = Math.min(255, (int)(0.0D + gMod));
        int b = Math.min(255, (int)(0.0D + bMod));
        return new Color(r, g, b);
    }

    public Color oldHitpointsColor(double hpPercent) {
        hpPercent = Math.max(Math.min(100.0D, hpPercent), 0.0D);
        double rMod = 0.0D;
        double gMod = 0.0D;
        double bMod = 0.0D;
        if (hpPercent >= 75.0D) {
            rMod = 0.0D;
            gMod = 255.0D;
            bMod = 0.0D;
        } else if (hpPercent < 75.0D && hpPercent >= 50.0D) {
            rMod = 255.0D;
            gMod = 255.0D;
            bMod = 0.0D;
        } else if (hpPercent < 50.0D && hpPercent >= 30.0D) {
            rMod = 220.0D;
            gMod = 200.0D;
            bMod = 0.0D;
        } else if (hpPercent < 30.0D) {
            rMod = 255.0D;
            gMod = 102.0D;
            bMod = 102.0D;
        }
        int r = (int)rMod;
        int g = (int)gMod;
        int b = (int)bMod;
        return new Color(r, g, b);
    }

    public void flowColor() {
        int red = this.flowColor.getRed();
        red += this.raveRedUp ? 1 : -1;
        if (red == 255 || red == 0)
            this.raveRedUp = !this.raveRedUp;
        int green = this.flowColor.getGreen();
        green += this.raveGreenUp ? 1 : -1;
        if (green == 255 || green == 0)
            this.raveGreenUp = !this.raveGreenUp;
        int blue = this.flowColor.getBlue();
        blue += this.raveBlueUp ? 1 : -1;
        if (blue == 255 || blue == 0)
            this.raveBlueUp = !this.raveBlueUp;
        this.flowColor = new Color(red, green, blue, 255);
    }
}
