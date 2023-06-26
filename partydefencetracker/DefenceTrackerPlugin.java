package net.runelite.client.plugins.partydefencetracker;

import com.google.inject.Provides;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.IndexDataBase;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.api.SpritePixels;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.messages.PartyMessage;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.party.PartyPlugin;
import net.runelite.client.plugins.specialcounter.SpecialCounterUpdate;
import net.runelite.client.plugins.specialcounter.SpecialWeapon;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name = "Party Defence Tracker", description = "Calculates the defence based off party specs", tags = {"party", "defence", "tracker", "boosting", "special", "counter"})
@PluginDependency(PartyPlugin.class)
public class DefenceTrackerPlugin extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(DefenceTrackerPlugin.class);

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PartyService partyService;

    @Inject
    private WSClient wsClient;

    @Inject
    private DefenceTrackerConfig config;

    @Inject
    private SkillIconManager skillIconManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private CoXLayoutSolver layoutSolver;

    private String boss = "";

    private int bossIndex = 0;

    private double bossDef = -1.0D;

    private DefenceInfoBox box = null;

    private VulnerabilityInfoBox vulnBox = null;

    private SpritePixels vuln = null;

    private RedKerisInfoBox redKerisBox = null;

    private int redKerisTicks = 0;

    private int hitsplatTick;

    private Hitsplat lastSpecHitsplat;

    private NPC lastSpecTarget;

    public int getRedKerisTicks() {
        return this.redKerisTicks;
    }

    private double lastSpecPercent = -1.0D;

    private boolean hmXarpus = false;

    private boolean bloatDown = false;

    private boolean inCm;

    private boolean coxModeSet = false;

    private QueuedNpc queuedNpc = null;

    Map<String, ArrayList<Integer>> bossRegions = new HashMap<String, ArrayList<Integer>>() {

    };

    private final List<String> coxBosses = Arrays.asList(new String[] { "Great Olm (Left claw)", "Ice demon", "Skeletal Mystic", "Tekton", "Vasa Nistirio" });

    @Provides
    DefenceTrackerConfig provideConfig(ConfigManager configManager) {
        return (DefenceTrackerConfig)configManager.getConfig(DefenceTrackerConfig.class);
    }

    protected void startUp() throws Exception {
        reset();
        this.wsClient.registerMessage(DefenceTrackerUpdate.class);
    }

    protected void shutDown() throws Exception {
        reset();
        this.wsClient.unregisterMessage(DefenceTrackerUpdate.class);
    }

    protected void reset() {
        this.infoBoxManager.removeInfoBox(this.box);
        this.infoBoxManager.removeInfoBox(this.vulnBox);
        this.infoBoxManager.removeInfoBox(this.redKerisBox);
        this.boss = "";
        this.bossIndex = 0;
        this.bossDef = -1.0D;
        this.box = null;
        this.vulnBox = null;
        this.vuln = null;
        this.redKerisBox = null;
        this.redKerisTicks = 0;
        this.bloatDown = false;
        this.queuedNpc = null;
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged e) {
        int animation = e.getActor().getAnimation();
        if (e.getActor() instanceof net.runelite.api.Player && e.getActor() != null && this.client.getLocalPlayer() != null && e.getActor().getName() != null)
            if (e.getActor().getName().equals(this.client.getLocalPlayer().getName()))
                if (animation == 1816 && this.boss.equalsIgnoreCase("sotetseg") && inBossRegion()) {
                    this.infoBoxManager.removeInfoBox(this.box);
                    this.bossDef = 200.0D;
                }
        if (e.getActor() instanceof NPC && e.getActor().getName() != null)
            if (e.getActor().getName().equalsIgnoreCase("pestilent bloat")) {
                this.bloatDown = (animation == 8082);
            } else if (animation == 9685 && (this.boss.equalsIgnoreCase("Tumeken's Warden") || this.boss.equalsIgnoreCase("Elidinis' Warden"))) {
                this.infoBoxManager.removeInfoBox(this.box);
                this.bossDef = 60.0D;
            }
    }

    @Subscribe
    public void onGameTick(GameTick e) {
        if (this.partyService.isInParty())
            for (NPC n : this.client.getNpcs()) {
                if (n != null && n.getName() != null && (n.getName().equalsIgnoreCase(this.boss) || (n.getName().contains("Tekton") && this.boss.equalsIgnoreCase("Tekton"))) && (n
                        .isDead() || n.getHealthRatio() == 0))
                    this.partyService.send((PartyMessage)new DefenceTrackerUpdate(n.getName(), n.getIndex(), false, this.client.getWorld(), ""));
            }
        this.layoutSolver.onGameTick(e);
        if (!this.coxModeSet && this.client.getVarbitValue(5432) == 1) {
            this.inCm = this.layoutSolver.isCM();
            this.coxModeSet = true;
        }
        if (this.redKerisTicks > 0) {
            this.redKerisTicks--;
            if (this.redKerisTicks == 0)
                this.infoBoxManager.removeInfoBox(this.redKerisBox);
        }
        if (this.lastSpecHitsplat != null && this.lastSpecTarget != null) {
            if (this.lastSpecHitsplat.getAmount() > 0 && this.partyService.isInParty())
                this.partyService.send((PartyMessage)new DefenceTrackerUpdate(this.lastSpecTarget.getName(), this.lastSpecTarget.getIndex(), true, this.client.getWorld(), "keris"));
            this.lastSpecHitsplat = null;
            this.lastSpecTarget = null;
        }
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied e) {
        Actor target = e.getActor();
        Hitsplat hitsplat = e.getHitsplat();
        if (hitsplat.isMine() && target instanceof NPC && this.lastSpecTarget != null && this.hitsplatTick == this.client.getTickCount()) {
            NPC npc = (NPC)target;
            String name = npc.getName();
            if (name != null && (BossInfo.getBoss(name) != null || this.bossIndex == npc.getIndex()))
                this.lastSpecHitsplat = hitsplat;
        }
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned e) {
        NPC npc = e.getNpc();
        if (npc.getName() != null && BossInfo.getBoss(npc.getName()) != null)
            this.hmXarpus = (npc.getId() >= 10770 && npc.getId() <= 10772);
    }

    @Subscribe
    public void onActorDeath(ActorDeath e) {
        if (e.getActor() instanceof NPC && e.getActor().getName() != null && this.client.getLocalPlayer() != null && this.partyService.isInParty())
            if (e.getActor().getName().equalsIgnoreCase(this.boss) || (e.getActor().getName().contains("Tekton") && this.boss.equalsIgnoreCase("Tekton")))
                this.partyService.send((PartyMessage)new DefenceTrackerUpdate(e.getActor().getName(), ((NPC)e.getActor()).getIndex(), false, this.client.getWorld(), ""));
    }

    @Subscribe
    public void onSpecialCounterUpdate(SpecialCounterUpdate e) {
        int hit = e.getHit();
        int world = e.getWorld();
        SpecialWeapon weapon = e.getWeapon();
        int index = e.getNpcIndex();
        NPC npc = this.client.getCachedNPCs()[index];
        this.clientThread.invoke(() -> {
            if ((npc != null && npc.getName() != null && BossInfo.getBoss(npc.getName()) != null) || this.bossIndex == index) {
                if (this.bossIndex != index) {
                    String bossName = npc.getName();
                    if (!this.boss.equalsIgnoreCase(bossName) || (bossName.contains("Tekton") && !this.boss.equalsIgnoreCase("Tekton"))) {
                        baseDefence(bossName, index);
                        calculateQueue(index);
                    }
                }
                if (inBossRegion() && world == this.client.getWorld()) {
                    calculateDefence(weapon, hit);
                    updateDefInfobox();
                }
            } else {
                if (this.queuedNpc == null || this.queuedNpc.index != index)
                    this.queuedNpc = new QueuedNpc(index);
                this.queuedNpc.queuedSpecs.add(new QueuedNpc.QueuedSpec(weapon, hit));
            }
        });
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (event.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION)
            if (Text.removeTags(event.getMessage()).equals("The raid has begun!") && this.client.getVarbitValue(5432) == 1) {
                this.inCm = this.layoutSolver.isCM();
                this.coxModeSet = true;
            }
    }

    @Subscribe
    private void onVarbitChanged(VarbitChanged e) {
        if (!inBossRegion())
            reset();
        if (this.client.getVarbitValue(5432) != 1 && isInCoxLobby()) {
            this.inCm = false;
            this.coxModeSet = false;
        }
        this.layoutSolver.onVarbitChanged(e);
        if (e.getVarpId() == 300) {
            if (this.client.getLocalPlayer() != null && this.client.getLocalPlayer().getPlayerComposition() != null) {
                int weapon = this.client.getLocalPlayer().getPlayerComposition().getEquipmentId(KitType.WEAPON);
                if (weapon == 27287 && (e.getValue() == this.lastSpecPercent - 750.0D || e.getValue() == this.lastSpecPercent - 375.0D)) {
                    int serverTicks = this.client.getTickCount();
                    this.clientThread.invokeLater(() -> {
                        Actor target = this.client.getLocalPlayer().getInteracting();
                        this.lastSpecTarget = (target instanceof NPC) ? (NPC)target : null;
                        this.hitsplatTick = serverTicks + 1;
                    });
                }
            }
            this.lastSpecPercent = e.getValue();
        }
    }

    @Subscribe
    public void onGraphicChanged(GraphicChanged e) {
        if (e.getActor() instanceof NPC && e.getActor().getName() != null && e.getActor().hasSpotAnim(169) && this.partyService.isInParty())
            if (BossInfo.getBoss(e.getActor().getName()) != null)
                this.partyService.send((PartyMessage)new DefenceTrackerUpdate(e.getActor().getName(), ((NPC)e.getActor()).getIndex(), true, this.client.getWorld(), "vuln"));
    }

    @Subscribe
    protected void onGameStateChanged(GameStateChanged e) {
        this.layoutSolver.onGameStateChanged(e);
    }

    private void baseDefence(String bossName, int index) {
        this.boss = bossName;
        this.bossIndex = index;
        this.bossDef = BossInfo.getBaseDefence(this.boss);
        if (this.boss.equalsIgnoreCase("Xarpus") && this.hmXarpus) {
            this.bossDef = 200.0D;
        } else if (this.coxBosses.contains(this.boss)) {
            this.bossDef *= 1.0D + 0.01D * (this.client.getVarbitValue(5424) - 1);
            if (this.inCm)
                this.bossDef *= this.boss.contains("Tekton") ? 1.2D : 1.5D;
        }
    }

    private void calculateDefence(SpecialWeapon weapon, int hit) {
        if (weapon == SpecialWeapon.DRAGON_WARHAMMER) {
            if (hit == 0) {
                if (this.client.getVarbitValue(5432) == 1 && this.boss.equalsIgnoreCase("Tekton"))
                    this.bossDef -= this.bossDef * 0.05D;
            } else {
                this.bossDef -= this.bossDef * 0.3D;
            }
        } else if (weapon == SpecialWeapon.BANDOS_GODSWORD) {
            if (hit == 0) {
                if (this.client.getVarbitValue(5432) == 1 && this.boss.equalsIgnoreCase("Tekton"))
                    this.bossDef -= 10.0D;
            } else if (this.boss.equalsIgnoreCase("Corporeal Beast") || (inBossRegion() && this.boss.equalsIgnoreCase("Pestilent Bloat") && !this.bloatDown)) {
                this.bossDef -= (hit * 2);
            } else {
                this.bossDef -= hit;
            }
        } else if ((weapon == SpecialWeapon.ARCLIGHT || weapon == SpecialWeapon.DARKLIGHT) && hit > 0) {
            if (this.boss.equalsIgnoreCase("K'ril Tsutsaroth") || this.boss.equalsIgnoreCase("Abyssal Sire")) {
                this.bossDef -= BossInfo.getBaseDefence(this.boss) * 0.1D;
            } else {
                this.bossDef -= BossInfo.getBaseDefence(this.boss) * 0.05D;
            }
        } else if (weapon == SpecialWeapon.BARRELCHEST_ANCHOR) {
            this.bossDef -= hit * 0.1D;
        } else if (weapon == SpecialWeapon.BONE_DAGGER || weapon == SpecialWeapon.DORGESHUUN_CROSSBOW) {
            this.bossDef -= hit;
        }
        if (this.boss.equalsIgnoreCase("Sotetseg") && this.bossDef < 100.0D) {
            this.bossDef = 100.0D;
        } else if (this.bossDef < 0.0D) {
            this.bossDef = 0.0D;
        }
    }

    private void calculateQueue(int index) {
        if (this.queuedNpc != null) {
            if (this.queuedNpc.index == index)
                for (QueuedNpc.QueuedSpec spec : this.queuedNpc.queuedSpecs)
                    calculateDefence(spec.weapon, spec.hit);
            this.queuedNpc = null;
        }
    }

    private void updateDefInfobox() {
        this.infoBoxManager.removeInfoBox(this.box);
        this.box = new DefenceInfoBox(this.skillIconManager.getSkillImage(Skill.DEFENCE), this, Math.round(this.bossDef), this.config);
        this.box.setTooltip(ColorUtil.wrapWithColorTag(this.boss, Color.WHITE));
        this.infoBoxManager.addInfoBox(this.box);
    }

    private boolean inBossRegion() {
        if (this.client.getLocalPlayer() != null && this.bossRegions.containsKey(this.boss)) {
            WorldPoint wp = WorldPoint.fromLocalInstance(this.client, this.client.getLocalPlayer().getLocalLocation());
            if (wp != null)
                return ((ArrayList)this.bossRegions.get(this.boss)).contains(Integer.valueOf(wp.getRegionID()));
        }
        return (this.client.getVarbitValue(5432) == 1 || !this.coxBosses.contains(this.boss));
    }

    public boolean isInCoxLobby() {
        return (this.client.getMapRegions() != null && (this.client.getMapRegions()).length > 0 && Arrays.stream(this.client.getMapRegions()).anyMatch(s -> (s == 4919)));
    }
}
