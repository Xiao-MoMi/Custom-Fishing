package net.momirealms.customfishing.integration;

import net.momirealms.customfishing.object.loot.Mob;
import org.bukkit.Location;

public interface MobInterface {
    void summon(Location playerLoc, Location summonLoc, Mob mob);
}
