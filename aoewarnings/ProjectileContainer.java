package net.runelite.client.plugins.aoewarnings;


import java.time.Instant;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;

class ProjectileContainer {
    private Projectile projectile;
    private Instant startTime;
    private AoeProjectileInfo aoeProjectileInfo;
    private int lifetime;
    private int finalTick;
    private LocalPoint targetPoint;

    ProjectileContainer(Projectile projectile, Instant startTime, int lifetime, int finalTick) {
        this.projectile = projectile;
        this.startTime = startTime;
        this.targetPoint = null;
        this.aoeProjectileInfo = AoeProjectileInfo.getById(projectile.getId());
        this.lifetime = lifetime;
        this.finalTick = finalTick;
    }

    Projectile getProjectile() {
        return this.projectile;
    }

    Instant getStartTime() {
        return this.startTime;
    }

    AoeProjectileInfo getAoeProjectileInfo() {
        return this.aoeProjectileInfo;
    }

    int getLifetime() {
        return this.lifetime;
    }

    int getFinalTick() {
        return this.finalTick;
    }

    LocalPoint getTargetPoint() {
        return this.targetPoint;
    }

    void setTargetPoint(LocalPoint targetPoint) {
        this.targetPoint = targetPoint;
    }
}
