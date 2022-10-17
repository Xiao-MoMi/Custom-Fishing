package net.momirealms.customfishing.object.loot;

import net.momirealms.customfishing.object.fishing.Difficulty;
import org.jetbrains.annotations.NotNull;

public class Mob extends Loot{

    private final String mobID;
    private final int mobLevel;
    private final MobVector mobVector;

    public Mob(String key, Difficulty difficulty, int time, int weight, String mobID, int level, MobVector vector) {
        super(key, difficulty, time, weight);
        this.mobID = mobID;
        this.mobLevel = level;
        this.mobVector = vector;
    }

    public String getMobID() {
        return mobID;
    }

    public int getMobLevel() {
        return mobLevel;
    }

    @NotNull
    public MobVector getMobVector() {
        return mobVector;
    }
}
