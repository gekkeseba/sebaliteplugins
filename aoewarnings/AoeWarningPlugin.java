package net.runelite.client.plugins.aoewarnings;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Projectile;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(name = "AoE Warnings", enabledByDefault = false, description = "Shows the final destination for AoE Attack projectiles", tags = {"bosses", "combat", "pve", "overlay"})
public class AoeWarningPlugin extends Plugin {
    private final Set<CrystalBomb> bombs = new HashSet<>();

    Set<CrystalBomb> getBombs() {
        return this.bombs;
    }

    private final Set<ProjectileContainer> projectiles = new HashSet<>();

    @Inject
    public AoeWarningConfig config;

    @Inject
    private Notifier notifier;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AoeWarningOverlay coreOverlay;

    @Inject
    private BombOverlay bombOverlay;

    @Inject
    private Client client;

    Set<ProjectileContainer> getProjectiles() {
        return this.projectiles;
    }

    private List<WorldPoint> lightningTrail = new ArrayList<>();

    List<WorldPoint> getLightningTrail() {
        return this.lightningTrail;
    }

    private List<GameObject> acidTrail = new ArrayList<>();

    List<GameObject> getAcidTrail() {
        return this.acidTrail;
    }

    private List<GameObject> crystalSpike = new ArrayList<>();

    List<GameObject> getCrystalSpike() {
        return this.crystalSpike;
    }

    private List<GameObject> wintertodtSnowFall = new ArrayList<>();

    private static final int VERZIK_REGION = 12611;

    private static final int GROTESQUE_GUARDIANS_REGION = 6727;

    public static final int OLM_LIGHTNING = 1356;

    List<GameObject> getWintertodtSnowFall() {
        return this.wintertodtSnowFall;
    }

    private Projectile lastProjectile = null;

    @Provides
    AoeWarningConfig getConfig(ConfigManager configManager) {
        return (AoeWarningConfig)configManager.getConfig(AoeWarningConfig.class);
    }

    protected void startUp() {
        this.overlayManager.add(this.coreOverlay);
        this.overlayManager.add(this.bombOverlay);
        reset();
    }

    protected void shutDown() {
        this.overlayManager.remove(this.coreOverlay);
        this.overlayManager.remove(this.bombOverlay);
        reset();
    }

    private void projectileSpawned(ProjectileMoved event) {
        Projectile projectile = event.getProjectile();
        if (AoeProjectileInfo.getById(projectile.getId()) == null)
            return;
        int id = projectile.getId();
        int lifetime = this.config.delay() + projectile.getRemainingCycles() * 20;
        int ticksRemaining = projectile.getRemainingCycles() / 30;
        if (!isTickTimersEnabledForProjectileID(id))
            ticksRemaining = 0;
        int tickCycle = this.client.getTickCount() + ticksRemaining;
        if (isConfigEnabledForProjectileId(id, false)) {
            this.projectiles.add(new ProjectileContainer(projectile, Instant.now(), lifetime, tickCycle));
            if (this.config.aoeNotifyAll() || isConfigEnabledForProjectileId(id, true))
                this.notifier.notify("AoE attack detected!");
        }
    }

    @Subscribe
    private void onProjectileMoved(ProjectileMoved event) {
        if (!this.projectiles.contains(event.getProjectile()))
            projectileSpawned(event);
        if (this.projectiles.isEmpty())
            return;
        Projectile projectile = event.getProjectile();
        this.projectiles.forEach(proj -> {
            if (proj.getProjectile() == projectile)
                proj.setTargetPoint(event.getPosition());
        });
    }

    @Subscribe
    private void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject gameObject = event.getGameObject();
        switch (gameObject.getId()) {
            case 29766:
                this.bombs.add(new CrystalBomb(gameObject, this.client.getTickCount()));
                if (this.config.aoeNotifyAll() || this.config.bombDisplayNotifyEnabled())
                    this.notifier.notify("Bomb!");
                break;
            case 30032:
                this.acidTrail.add(gameObject);
                break;
            case 30033:
                this.crystalSpike.add(gameObject);
                break;
            case 26690:
                if (this.config.isWintertodtEnabled()) {
                    this.wintertodtSnowFall.add(gameObject);
                    if (this.config.aoeNotifyAll() || this.config.isWintertodtNotifyEnabled())
                        this.notifier.notify("Snow Fall!");
                }
                break;
        }
    }

    @Subscribe
    private void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject gameObject = event.getGameObject();
        switch (gameObject.getId()) {
            case 29766:
                this.bombs.removeIf(o -> (o.getGameObject() == gameObject));
                break;
            case 30032:
                this.acidTrail.remove(gameObject);
                break;
            case 30033:
                this.crystalSpike.remove(gameObject);
                break;
            case 26690:
                this.wintertodtSnowFall.remove(gameObject);
                break;
        }
    }

    @Subscribe
    private void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOGGED_IN)
            return;
        reset();
    }

    @Subscribe
    private void onGameTick(GameTick event) {
        this.lightningTrail.clear();
        if (this.config.LightningTrail())
            this.client.getGraphicsObjects().forEach(o -> {
                if (o.getId() == 1356) {
                    this.lightningTrail.add(WorldPoint.fromLocal(this.client, o.getLocation()));
                    if (this.config.aoeNotifyAll() || this.config.LightningTrailNotifyEnabled())
                        this.notifier.notify("Lightning!");
                }
            });
        this.bombs.forEach(CrystalBomb::bombClockUpdate);
    }

    private boolean isTickTimersEnabledForProjectileID(int projectileId) {
        AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);
        if (projectileInfo == null)
            return false;
        switch (projectileInfo) {
            case VASA_RANGED_AOE:
            case VORKATH_POISON_POOL:
            case VORKATH_SPAWN:
            case VORKATH_TICK_FIRE:
            case OLM_BURNING:
            case OLM_FALLING_CRYSTAL_TRAIL:
            case OLM_ACID_TRAIL:
            case OLM_FIRE_LINE:
                return false;
        }
        return true;
    }

    private boolean isConfigEnabledForProjectileId(int projectileId, boolean notify) {
        AoeProjectileInfo projectileInfo = AoeProjectileInfo.getById(projectileId);
        if (projectileInfo == null)
            return false;
        if (notify && this.config.aoeNotifyAll())
            return true;
        switch (projectileInfo) {
            case LIZARDMAN_SHAMAN_AOE:
                return notify ? this.config.isShamansNotifyEnabled() : this.config.isShamansEnabled();
            case CRAZY_ARCHAEOLOGIST_AOE:
                return notify ? this.config.isArchaeologistNotifyEnabled() : this.config.isArchaeologistEnabled();
            case ICE_DEMON_RANGED_AOE:
            case ICE_DEMON_ICE_BARRAGE_AOE:
                return notify ? this.config.isIceDemonNotifyEnabled() : this.config.isIceDemonEnabled();
            case VASA_RANGED_AOE:
            case VASA_AWAKEN_AOE:
                return notify ? this.config.isVasaNotifyEnabled() : this.config.isVasaEnabled();
            case TEKTON_METEOR_AOE:
                return notify ? this.config.isTektonNotifyEnabled() : this.config.isTektonEnabled();
            case VORKATH_POISON_POOL:
            case VORKATH_SPAWN:
            case VORKATH_TICK_FIRE:
            case VORKATH_BOMB:
                return notify ? this.config.isVorkathNotifyEnabled() : this.config.vorkathModes().contains(AoeWarningConfig.VorkathMode.of(projectileInfo));
            case VETION_LIGHTNING:
                return notify ? this.config.isVetionNotifyEnabled() : this.config.isVetionEnabled();
            case CHAOS_FANATIC:
                return notify ? this.config.isChaosFanaticNotifyEnabled() : this.config.isChaosFanaticEnabled();
            case GALVEK_BOMB:
            case GALVEK_MINE:
                return notify ? this.config.isGalvekNotifyEnabled() : this.config.isGalvekEnabled();
            case DAWN_FREEZE:
            case DUSK_CEILING:
                if (regionCheck(6727))
                    return notify ? this.config.isGargBossNotifyEnabled() : this.config.isGargBossEnabled();
            case VERZIK_P1_ROCKS:
                if (regionCheck(12611))
                    return notify ? this.config.isVerzikNotifyEnabled() : this.config.isVerzikEnabled();
            case OLM_BURNING:
            case OLM_FALLING_CRYSTAL_TRAIL:
            case OLM_ACID_TRAIL:
            case OLM_FIRE_LINE:
            case OLM_FALLING_CRYSTAL:
                return notify ? this.config.isOlmNotifyEnabled() : this.config.isOlmEnabled();
            case CORPOREAL_BEAST:
            case CORPOREAL_BEAST_DARK_CORE:
                return notify ? this.config.isCorpNotifyEnabled() : this.config.isCorpEnabled();
            case XARPUS_POISON_AOE:
                return notify ? this.config.isXarpusNotifyEnabled() : this.config.isXarpusEnabled();
            case ADDY_DRAG_POISON:
                return notify ? this.config.addyDragsNotifyEnabled() : this.config.addyDrags();
            case DRAKE_BREATH:
                return notify ? this.config.isDrakeNotifyEnabled() : this.config.isDrakeEnabled();
            case CERB_FIRE:
                return notify ? this.config.isCerbFireNotifyEnabled() : this.config.isCerbFireEnabled();
            case DEMONIC_GORILLA_BOULDER:
                return notify ? this.config.isDemonicGorillaNotifyEnabled() : this.config.isDemonicGorillaEnabled();
            case VERZIK_PURPLE_SPAWN:
                return notify ? this.config.isVerzikNotifyEnabled() : this.config.isVerzikEnabled();
        }
        return false;
    }

    private void reset() {
        this.lightningTrail.clear();
        this.acidTrail.clear();
        this.crystalSpike.clear();
        this.wintertodtSnowFall.clear();
        this.bombs.clear();
        this.projectiles.clear();
    }

    private boolean regionCheck(int region) {
        return ArrayUtils.contains(this.client.getMapRegions(), region);
    }
}
