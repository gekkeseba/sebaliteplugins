package net.runelite.client.plugins.spoontob.rooms.Maiden;

import com.google.common.collect.ImmutableSet;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GraphicsObject;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.spoontob.Room;
import net.runelite.client.plugins.spoontob.SpoonTobConfig;
import net.runelite.client.plugins.spoontob.SpoonTobPlugin;
import net.runelite.client.plugins.spoontob.SpoonTobConfig.maidenBloodsMode;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.apache.commons.lang3.tuple.Pair;

public class Maiden extends Room {
    @Inject
    private Client client;
    @Inject
    private MaidenOverlay maidenOverlay;
    @Inject
    private ThresholdOverlay thresholdOverlay;
    @Inject
    private MaidenMaxHitOverlay maidenMaxHitOverlay;
    @Inject
    private MaidenRedsOverlay redsOverlay;
    private static final int BLOOD_SPLAT_GRAPHIC = 1579;
    private static final int BLOOD_SPLAT_PROJECTILE = 1578;
    private boolean maidenActive;
    private NPC maidenNPC;
    private List<NPC> maidenSpawns = new ArrayList();
    private Map<NPC, Pair<Integer, Integer>> maidenReds = new HashMap();
    private List<WorldPoint> maidenBloodSplatters = new ArrayList();
    private ArrayList<MaidenBloodSplatInfo> maidenBloodSplatterProj = new ArrayList();
    public ArrayList<Color> maidenBloodSplattersColors = new ArrayList();
    private List<WorldPoint> maidenBloodSpawnLocations = new ArrayList();
    private List<WorldPoint> maidenBloodSpawnTrailingLocations = new ArrayList();
    private int newMaidenHp = -1;
    private int newMaidenThresholdHp = -1;
    private short realMaidenHp = -1;
    private short thresholdHp = -1;
    private double maxHit = 36.5;
    private short timesMaidenHealed = 0;
    private short amountMaidenHealed = 0;
    public final DecimalFormat df1 = new DecimalFormat("#0.0");
    private final Consumer<Double> setThreshold = (percent) -> {
        this.thresholdHp = (short)((int)Math.floor((double)this.getMaidenBaseHpIndex() * percent));
    };
    public int ticksUntilAttack = 0;
    public int maidenAttSpd = 10;
    public int lastAnimationID = -1;
    private static final Set<MenuAction> NPC_MENU_ACTIONS;
    public Color c;
    public int nyloSpawnDelay = 2;
    public int maidenPhase = 70;
    public ArrayList<MaidenCrabInfo> maidenCrabInfoList = new ArrayList();
    public Map<NPC, Integer> frozenBloodSpawns = new HashMap();
    public int crabTicksSinceSpawn = 0;

    @Inject
    protected Maiden(SpoonTobPlugin plugin, SpoonTobConfig config) {
        super(plugin, config);
    }

    public void load() {
        this.overlayManager.add(this.maidenOverlay);
        this.overlayManager.add(this.thresholdOverlay);
        this.overlayManager.add(this.maidenMaxHitOverlay);
        this.overlayManager.add(this.redsOverlay);
    }

    public void unload() {
        this.overlayManager.remove(this.maidenOverlay);
        this.overlayManager.remove(this.thresholdOverlay);
        this.overlayManager.remove(this.maidenMaxHitOverlay);
        this.overlayManager.remove(this.redsOverlay);
        this.maidenActive = false;
        this.maidenBloodSplatters.clear();
        this.maidenBloodSplattersColors.clear();
        this.maidenSpawns.clear();
        this.maidenBloodSpawnLocations.clear();
        this.maidenBloodSpawnTrailingLocations.clear();
        this.newMaidenHp = -1;
        this.newMaidenThresholdHp = -1;
        this.timesMaidenHealed = 0;
        this.amountMaidenHealed = 0;
        this.realMaidenHp = -1;
        this.thresholdHp = -1;
        this.maxHit = 36.5;
    }

    void updateMaidenMaxHit() {
        this.maxHit += 3.5;
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned) {
        NPC npc = npcSpawned.getNpc();
        String name = npc.getName();
        switch (npc.getId()) {
            case 8360:
            case 8361:
            case 8362:
            case 8363:
            case 8364:
            case 8365:
            case 10814:
            case 10815:
            case 10816:
            case 10817:
            case 10818:
            case 10819:
            case 10822:
            case 10823:
            case 10824:
            case 10825:
            case 10826:
            case 10827:
                this.maidenActive = true;
                this.maidenNPC = npc;
                if ((this.maidenNPC.getHealthRatio() != -1 || this.maidenNPC.getHealthScale() != -1 || this.maidenNPC.getId() == 10822 || this.maidenNPC.getId() == 8360 || this.maidenNPC.getId() == 10814) && this.maidenNPC.getHealthRatio() == this.maidenNPC.getHealthScale()) {
                    this.ticksUntilAttack = 10;
                } else {
                    this.ticksUntilAttack = -1;
                }

                this.maidenAttSpd = 10;
                this.maidenCrabInfoList.clear();
                if (this.realMaidenHp < 0) {
                    this.realMaidenHp = this.getMaidenBaseHpIndex();
                }

                this.setThreshold.accept(0.7);
                break;
            case 8367:
            case 10821:
            case 10829:
                this.maidenSpawns.add(npc);
        }

        if (name != null && name.equalsIgnoreCase("Nylocas Matomenos") && this.maidenActive && this.maidenNPC != null) {
            this.crabTicksSinceSpawn = 8;
            this.nyloSpawnDelay = 2;
            String position = "??";
            boolean scuffed = false;
            int x = npc.getWorldLocation().getRegionX();
            int y = npc.getWorldLocation().getRegionY();
            if (x == 21 && y == 40) {
                position = "N1";
            } else if (x == 22 && y == 41) {
                position = "N1";
                scuffed = true;
            } else if (x == 25 && y == 40) {
                position = "N2";
            } else if (x == 26 && y == 41) {
                position = "N2";
                scuffed = true;
            } else if (x == 29 && y == 40) {
                position = "N3";
            } else if (x == 30 && y == 41) {
                position = "N3";
                scuffed = true;
            } else if (x == 33 && y == 40) {
                position = "N4";
            } else if (x == 34 && y == 41) {
                position = "N4";
                scuffed = true;
            } else if (x == 33 && y == 38) {
                position = "N4";
            } else if (x == 34 && y == 39) {
                position = "N4";
                scuffed = true;
            } else if (x == 21 && y == 20) {
                position = "S1";
            } else if (x == 22 && y == 19) {
                position = "S1";
                scuffed = true;
            } else if (x == 25 && y == 20) {
                position = "S2";
            } else if (x == 26 && y == 19) {
                position = "S2";
                scuffed = true;
            } else if (x == 29 && y == 20) {
                position = "S3";
            } else if (x == 30 && y == 19) {
                position = "S3";
                scuffed = true;
            } else if (x == 33 && y == 20) {
                position = "S4";
            } else if (x == 34 && y == 19) {
                position = "S4";
                scuffed = true;
            } else if (x == 33 && y == 22) {
                position = "S4";
            } else if (x == 34 && y == 20) {
                position = "S4";
                scuffed = true;
            }

            Iterator var8 = this.client.getNpcs().iterator();

            while(var8.hasNext()) {
                NPC n = (NPC)var8.next();
                if (n.getId() != 8361 && n.getId() != 10814 && n.getId() != 10823) {
                    if (n.getId() != 8362 && n.getId() != 10815 && n.getId() != 10824) {
                        if (n.getId() != 8363 && n.getId() != 10816 && n.getId() != 10825) {
                            continue;
                        }

                        this.maidenPhase = 30;
                        break;
                    }

                    this.maidenPhase = 50;
                    break;
                }

                this.maidenPhase = 70;
                break;
            }

            this.maidenCrabInfoList.add(new MaidenCrabInfo(npc, this.maidenPhase, position, -1, -1, -1, scuffed));
        }

    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned) {
        NPC npc = npcDespawned.getNpc();
        switch (npc.getId()) {
            case 8360:
            case 8361:
            case 8362:
            case 8363:
            case 8364:
            case 8365:
            case 10814:
            case 10815:
            case 10816:
            case 10817:
            case 10818:
            case 10819:
            case 10822:
            case 10823:
            case 10824:
            case 10825:
            case 10826:
            case 10827:
                this.ticksUntilAttack = 0;
                this.maidenAttSpd = 10;
                this.maidenActive = false;
                this.maidenSpawns.clear();
                this.maidenNPC = null;
                this.maidenPhase = 70;
                this.newMaidenHp = -1;
                this.newMaidenThresholdHp = -1;
                this.timesMaidenHealed = 0;
                this.amountMaidenHealed = 0;
                this.realMaidenHp = -1;
                this.thresholdHp = -1;
                this.maxHit = 36.5;
                break;
            case 8367:
            case 10821:
            case 10829:
                this.maidenSpawns.remove(npc);
        }

    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if (this.maidenActive && this.maidenNPC != null) {
            NPC npc = event.getNpc();
            int id = npc.getId();
            switch (id) {
                case 8360:
                case 8361:
                case 8362:
                case 8363:
                case 8364:
                case 8365:
                case 10814:
                case 10815:
                case 10816:
                case 10817:
                case 10818:
                case 10819:
                case 10822:
                case 10823:
                case 10824:
                case 10825:
                case 10826:
                case 10827:
                    if (id != 8361 && id != 10815 && id != 10823) {
                        if (id != 8362 && id != 10816 && id != 10824) {
                            if (id == 8363 || id == 10817 || id == 10825) {
                                this.maidenPhase = 30;
                            }
                        } else {
                            this.maidenPhase = 50;
                        }
                    } else {
                        this.maidenPhase = 70;
                    }
            }

            if (npc.getId() == 8361) {
                this.setThreshold.accept(0.5);
            }

            if (npc.getId() == 8362) {
                this.setThreshold.accept(0.3);
            }
        }

    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (this.maidenActive && this.maidenNPC != null) {
            MenuEntry mbEntry = event.getMenuEntry();
            int type = event.getType();
            if (type >= 2000) {
                type -= 2000;
            }

            MenuAction menuAction = MenuAction.of(type);
            NPC npc;
            if (NPC_MENU_ACTIONS.contains(menuAction) && event.getTarget().contains("Matomenos") && this.maidenCrabInfoList.size() > 0) {
                npc = this.client.getCachedNPCs()[event.getIdentifier()];
                Iterator var6 = this.maidenCrabInfoList.iterator();

                while(var6.hasNext()) {
                    MaidenCrabInfo mci = (MaidenCrabInfo)var6.next();
                    if (mci.crab == npc) {
                        double crabHealthPcent = (double)mci.hpRatio / (double)mci.hpScale * 100.0;
                        if (this.config.maidenRecolourNylos()) {
                            MenuEntry[] menuEntries = this.client.getMenuEntries();
                            MenuEntry menuEntry = menuEntries[menuEntries.length - 1];
                            Color color;
                            if (this.config.oldHpThreshold()) {
                                color = this.plugin.oldHitpointsColor(crabHealthPcent);
                            } else {
                                color = this.plugin.calculateHitpointsColor(crabHealthPcent);
                            }

                            String crabHp = Double.toString(crabHealthPcent);
                            if (crabHp.contains(".")) {
                                crabHp = crabHp.substring(0, crabHp.indexOf(".") + 2);
                            }

                            String var10000 = event.getTarget();
                            String target = ColorUtil.prependColorTag(Text.removeTags(var10000 + " - " + crabHp + "%"), color);
                            menuEntry.setTarget(target);
                            this.client.setMenuEntries(menuEntries);
                        }
                        break;
                    }
                }
            }

            if (event.getTarget().contains("Blood spawn") && event.getType() == MenuAction.NPC_SECOND_OPTION.getId() && (this.config.removeMaidenBloods() == maidenBloodsMode.ATTACK || this.config.removeMaidenBloods() == maidenBloodsMode.BOTH)) {
                npc = this.client.getCachedNPCs()[event.getIdentifier()];
                if (npc != null) {
                    mbEntry.setDeprioritized(true);
                }
            } else if (event.getTarget().contains("Blood spawn") && event.getTarget().contains("Ice B") && event.getType() == MenuAction.WIDGET_TARGET_ON_NPC.getId() && (this.config.removeMaidenBloods() == maidenBloodsMode.CAST || this.config.removeMaidenBloods() == maidenBloodsMode.BOTH)) {
                npc = this.client.getCachedNPCs()[event.getIdentifier()];
                if (npc != null) {
                    mbEntry.setDeprioritized(true);
                }
            }
        }

    }

    @Subscribe
    public void onGameTick(GameTick event) {
        Random rand = new Random();
        float r = rand.nextFloat() / 2.0F;
        float g = rand.nextFloat();
        float b = rand.nextFloat();
        this.c = new Color(r, g, b);
        if (this.maidenActive) {
            if (this.maidenNPC != null) {
                --this.ticksUntilAttack;
            }

            if (this.crabTicksSinceSpawn > 0) {
                --this.crabTicksSinceSpawn;
            }

            Iterator<NPC> it = this.frozenBloodSpawns.keySet().iterator();

            while(it.hasNext()) {
                NPC npc = (NPC)it.next();
                this.frozenBloodSpawns.replace(npc, (Integer)this.frozenBloodSpawns.get(npc) - 1);
                if ((Integer)this.frozenBloodSpawns.get(npc) < -5) {
                    it.remove();
                }
            }

            this.maidenBloodSplatters.clear();
            this.maidenBloodSplattersColors.clear();
            Iterator var9 = this.client.getGraphicsObjects().iterator();

            while(var9.hasNext()) {
                GraphicsObject obj = (GraphicsObject)var9.next();
                if (obj.getId() == 1579) {
                    this.maidenBloodSplatters.add(WorldPoint.fromLocal(this.client, obj.getLocation()));
                    this.maidenBloodSplattersColors.add(Color.getHSBColor((new Random()).nextFloat(), 1.0F, 1.0F));
                }
            }

            this.maidenBloodSpawnTrailingLocations.clear();
            this.maidenBloodSpawnTrailingLocations.addAll(this.maidenBloodSpawnLocations);
            this.maidenBloodSpawnLocations.clear();
            this.maidenSpawns.forEach((s) -> {
                this.maidenBloodSpawnLocations.add(s.getWorldLocation());
            });
            if (this.maidenCrabInfoList.size() > 0) {
                if (this.nyloSpawnDelay == 0) {
                    var9 = this.maidenCrabInfoList.iterator();

                    while(var9.hasNext()) {
                        MaidenCrabInfo mci = (MaidenCrabInfo)var9.next();
                        if (mci.frozenTicks != -1) {
                            --mci.frozenTicks;
                        }
                    }
                } else {
                    --this.nyloSpawnDelay;
                }
            }

            if (this.maidenBloodSplatterProj.size() > 0) {
                for(int i = this.maidenBloodSplatterProj.size() - 1; i >= 0; --i) {
                    if (((MaidenBloodSplatInfo)this.maidenBloodSplatterProj.get(i)).projectile.getRemainingCycles() / 30 <= 0) {
                        this.maidenBloodSplatterProj.remove(i);
                    }
                }
            }
        }

    }

    @Subscribe
    public void onClientTick(ClientTick event) {
        if (this.maidenNPC != null && this.maidenActive) {
            int highestHP = 0;
            int npcIndex = 0;
            MenuEntry highestHpEntry = null;
            this.maidenCrabInfoList.forEach((mcix) -> {
                if (this.maidenCrabInfoList.size() > 0 && this.nyloSpawnDelay == 0 && mcix.crab.getHealthRatio() >= 0) {
                    mcix.hpRatio = mcix.crab.getHealthRatio();
                    mcix.hpScale = mcix.crab.getHealthScale();
                }

            });
            MenuEntry[] npcEntries = (MenuEntry[])Arrays.stream(this.client.getMenuEntries()).filter((menuEntryx) -> {
                return menuEntryx.getTarget().contains("Nylocas Matomenos") && (menuEntryx.getOption().contains("Attack") || menuEntryx.getOption().contains("Cast"));
            }).toArray((x$0) -> {
                return new MenuEntry[x$0];
            });
            if (npcEntries.length > 1 && this.config.maidenCrabHpPriority()) {
                List<MaidenCrabInfo> clickableCrabs = (List)this.maidenCrabInfoList.stream().filter((mcix) -> {
                    return Arrays.stream(npcEntries).anyMatch((menuEntry) -> {
                        return menuEntry.getIdentifier() == mcix.crab.getIndex();
                    });
                }).collect(Collectors.toList());
                Iterator var7 = clickableCrabs.iterator();

                while(true) {
                    MaidenCrabInfo mci;
                    do {
                        if (!var7.hasNext()) {
                            MenuEntry[] newEntries = npcEntries;
                            int index = npcEntries.length;

                            int i;
                            for(i = 0; i < index; ++i) {
                                MenuEntry menuEntry = newEntries[i];
                                if (menuEntry.getIdentifier() == npcIndex) {
                                    highestHpEntry = menuEntry;
                                }
                            }

                            if (highestHpEntry != null) {
                                newEntries = this.client.getMenuEntries();
                                index = Arrays.asList(this.client.getMenuEntries()).indexOf(highestHpEntry);

                                for(i = 0; i < newEntries.length; ++i) {
                                    if (i != index) {
                                        newEntries[i] = newEntries[i].setDeprioritized(true);
                                    }
                                }

                                this.client.setMenuEntries(newEntries);
                            }

                            return;
                        }

                        mci = (MaidenCrabInfo)var7.next();
                    } while(mci.hpRatio <= highestHP && mci.hpRatio != -1);

                    highestHP = mci.hpRatio;
                    npcIndex = mci.crab.getIndex();
                }
            }
        }

    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        Actor actor = event.getActor();
        if (actor.getName() != null && this.maidenNPC != null && this.maidenActive) {
            if (actor.getName().equals("Nylocas Matomenos") && actor.getAnimation() == 8097) {
                NPC npc = (NPC)actor;

                for(int i = this.maidenCrabInfoList.size() - 1; i >= 0; --i) {
                    MaidenCrabInfo mci = (MaidenCrabInfo)this.maidenCrabInfoList.get(i);
                    if (npc == mci.crab) {
                        NPCComposition nComp = this.maidenNPC.getComposition();
                        int distance = npc.getWorldLocation().getX() - (this.maidenNPC.getWorldLocation().getX() + nComp.getSize());
                        if ((distance == -1 || distance == 0) && (npc.getHealthRatio() > 0 || npc.getHealthRatio() == -1)) {
                            double crabHealthPcent = (double)mci.hpRatio / (double)mci.hpScale * 100.0;
                            String crabHp = String.valueOf(crabHealthPcent);
                            String var10000 = crabHp.substring(0, crabHp.indexOf(".") + 2);
                            crabHp = var10000 + "%";
                            if (this.config.leakedMessage()) {
                                String msg = "[<col=ff0000>" + mci.phase + "s</col>] The <col=ff0000>" + mci.position + "</col> crab leaked with <col=ff0000>" + crabHp;
                                this.client.addChatMessage(ChatMessageType.FRIENDSCHATNOTIFICATION, "", msg, "");
                            }

                            if (distance == 0) {
                                this.updateMaidenMaxHit();
                            }
                        }

                        this.maidenCrabInfoList.remove(i);
                        break;
                    }
                }
            } else if (actor.getName().equals("The Maiden of Sugadinti") && (actor.getAnimation() == 8091 || actor.getAnimation() == 8092)) {
                if (this.ticksUntilAttack > 1 && this.maidenNPC.getId() >= 10822) {
                    this.maidenAttSpd -= this.ticksUntilAttack - 1;
                    if (this.maidenAttSpd < 3) {
                        this.maidenAttSpd = 3;
                    }
                }

                this.ticksUntilAttack = this.maidenAttSpd + 1;
            }
        }

    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged event) {
        if (this.maidenActive && this.maidenNPC != null && event.getActor() instanceof NPC) {
            NPC npc = (NPC)event.getActor();
            int ticks = 0;
            if (npc.getGraphic() == 369) {
                ticks = 33;
            } else if (npc.getGraphic() == 367) {
                ticks = 25;
            } else if (npc.getGraphic() == 363) {
                ticks = 16;
            } else if (npc.getGraphic() == 361) {
                ticks = 8;
            } else if (npc.getGraphic() == 179) {
                ticks = 24;
            } else if (npc.getGraphic() == 180) {
                ticks = 16;
            } else if (npc.getGraphic() == 181) {
                ticks = 8;
            }

            if (npc.getName() != null && ticks > 0) {
                if (this.config.bloodSpawnFreezeTimer() && npc.getName().equalsIgnoreCase("blood spawn")) {
                    if (!this.frozenBloodSpawns.containsKey(npc)) {
                        this.frozenBloodSpawns.put(npc, Integer.valueOf(ticks));
                    }
                } else if (npc.getName().equalsIgnoreCase("nylocas matomenos")) {
                    Iterator var4 = this.maidenCrabInfoList.iterator();

                    while(var4.hasNext()) {
                        MaidenCrabInfo mci = (MaidenCrabInfo)var4.next();
                        if (mci.crab == npc && mci.frozenTicks == -1) {
                            mci.frozenTicks = ticks;
                            break;
                        }
                    }
                }
            }
        }

    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (event.getActor() instanceof NPC && this.frozenBloodSpawns.containsKey((NPC)event.getActor())) {
            this.frozenBloodSpawns.remove((NPC)event.getActor());
        }

    }

    @Subscribe
    public void onProjectileMoved(ProjectileMoved event) {
        if (event.getProjectile().getId() == 1578) {
            this.maidenBloodSplatterProj.add(new MaidenBloodSplatInfo(event.getProjectile(), event.getPosition()));
        }

    }

    Color maidenSpecialWarningColor() {
        Color col = Color.GREEN;
        if (this.maidenNPC != null && this.maidenNPC.getInteracting() != null && this.maidenNPC.getInteracting().getName() != null && this.client.getLocalPlayer() != null) {
            return this.maidenNPC.getInteracting().getName().equals(this.client.getLocalPlayer().getName()) ? Color.ORANGE : col;
        } else {
            return col;
        }
    }

    private short getMaidenBaseHpIndex() {
        switch (SpoonTobPlugin.partySize) {
            case 4:
                return 3062;
            case 5:
                return 3500;
            default:
                return 2625;
        }
    }

    public boolean isMaidenActive() {
        return this.maidenActive;
    }

    public NPC getMaidenNPC() {
        return this.maidenNPC;
    }

    public List<NPC> getMaidenSpawns() {
        return this.maidenSpawns;
    }

    public Map<NPC, Pair<Integer, Integer>> getMaidenReds() {
        return this.maidenReds;
    }

    public List<WorldPoint> getMaidenBloodSplatters() {
        return this.maidenBloodSplatters;
    }

    public ArrayList<MaidenBloodSplatInfo> getMaidenBloodSplatterProj() {
        return this.maidenBloodSplatterProj;
    }

    public ArrayList<Color> getMaidenBloodSplattersColors() {
        return this.maidenBloodSplattersColors;
    }

    public List<WorldPoint> getMaidenBloodSpawnLocations() {
        return this.maidenBloodSpawnLocations;
    }

    public List<WorldPoint> getMaidenBloodSpawnTrailingLocations() {
        return this.maidenBloodSpawnTrailingLocations;
    }

    public int getNewMaidenHp() {
        return this.newMaidenHp;
    }

    public int getNewMaidenThresholdHp() {
        return this.newMaidenThresholdHp;
    }

    public short getRealMaidenHp() {
        return this.realMaidenHp;
    }

    public short getThresholdHp() {
        return this.thresholdHp;
    }

    public double getMaxHit() {
        return this.maxHit;
    }

    static {
        NPC_MENU_ACTIONS = ImmutableSet.of(MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION, MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION, MenuAction.NPC_FIFTH_OPTION, MenuAction.WIDGET_TARGET_ON_NPC, new MenuAction[]{MenuAction.ITEM_USE_ON_NPC});
    }
}
