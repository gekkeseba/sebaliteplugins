package net.runelite.client.plugins.partydefencetracker;


import net.runelite.client.party.messages.PartyMemberMessage;

public final class DefenceTrackerUpdate extends PartyMemberMessage {
    private final String boss;

    private final int index;

    private final boolean alive;

    private final int world;

    private final String weapon;

    public String toString() {
        return "DefenceTrackerUpdate(boss=" + getBoss() + ", index=" + getIndex() + ", alive=" + isAlive() + ", world=" + getWorld() + ", weapon=" + getWeapon() + ")";
    }

    public DefenceTrackerUpdate(String boss, int index, boolean alive, int world, String weapon) {
        this.boss = boss;
        this.index = index;
        this.alive = alive;
        this.world = world;
        this.weapon = weapon;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DefenceTrackerUpdate))
            return false;
        DefenceTrackerUpdate other = (DefenceTrackerUpdate)o;
        if (!other.canEqual(this))
            return false;
        if (!super.equals(o))
            return false;
        if (getIndex() != other.getIndex())
            return false;
        if (isAlive() != other.isAlive())
            return false;
        if (getWorld() != other.getWorld())
            return false;
        Object this$boss = getBoss(), other$boss = other.getBoss();
        if ((this$boss == null) ? (other$boss != null) : !this$boss.equals(other$boss))
            return false;
        Object this$weapon = getWeapon(), other$weapon = other.getWeapon();
        return !((this$weapon == null) ? (other$weapon != null) : !this$weapon.equals(other$weapon));
    }

    protected boolean canEqual(Object other) {
        return other instanceof DefenceTrackerUpdate;
    }
    public String getBoss() {
        return this.boss;
    }

    public int getIndex() {
        return this.index;
    }

    public boolean isAlive() {
        return this.alive;
    }

    public int getWorld() {
        return this.world;
    }

    public String getWeapon() {
        return this.weapon;
    }
}
