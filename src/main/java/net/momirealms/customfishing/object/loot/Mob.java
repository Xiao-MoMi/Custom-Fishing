package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.MobVector;

public class Mob extends Loot{

    final String mmID;
    int mmLevel;
    MobVector mobVector;

    public Mob(String key, String mmID) {
        super(key);
        this.mmID = mmID;
    }

    public String getMmID() {
        return mmID;
    }

    public int getMmLevel() {
        return mmLevel;
    }

    public void setMmLevel(int mmLevel) {
        this.mmLevel = mmLevel;
    }

    public MobVector getMobVector() {
        return mobVector;
    }

    public void setMobVector(MobVector mobVector) {
        this.mobVector = mobVector;
    }
}
