package net.momirealms.customfishing.integration.mob;

import com.mineinabyss.geary.papermc.tracking.entities.helpers.HelpersKt;
import com.mineinabyss.geary.prefabs.PrefabKey;
import com.mineinabyss.mobzy.MobzyModuleKt;
import com.mineinabyss.mobzy.MobzyPlugin;
import net.momirealms.customfishing.fishing.loot.Mob;
import net.momirealms.customfishing.integration.MobInterface;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public class GearyMobImpl implements MobInterface {

    @Override
    public void summon(Location playerLoc, Location summonLoc, Mob mob) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(mob.getMobID());
        if (prefabKey == null) return;

        Entity entity = (Entity) HelpersKt.spawnFromPrefab(summonLoc, prefabKey);
        entity.setVelocity(playerLoc.subtract(summonLoc).toVector().multiply(mob.getMobVector().horizontal() - 1).setY((playerLoc.subtract(summonLoc).toVector().getY() + 0.2) * mob.getMobVector().vertical()));
    }
}
