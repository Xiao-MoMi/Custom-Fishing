package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.mob.MobLibrary;
import org.bukkit.Location;

public interface MobManager {
    boolean registerMobLibrary(MobLibrary mobLibrary);

    boolean unregisterMobLibrary(String lib);

    boolean unregisterMobLibrary(MobLibrary mobLibrary);

    void summonMob(Location hookLocation, Location playerLocation, Loot loot);
}
