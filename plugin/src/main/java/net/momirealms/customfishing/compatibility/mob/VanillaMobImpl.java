package net.momirealms.customfishing.compatibility.mob;

import net.momirealms.customfishing.api.mechanic.mob.MobLibrary;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Map;

public class VanillaMobImpl implements MobLibrary {

    @Override
    public String identification() {
        return "vanilla";
    }

    @Override
    public Entity spawn(Location location, String id, Map<String, Object> mobPropertyMap) {
        return location.getWorld().spawnEntity(location, EntityType.valueOf(id.toUpperCase(Locale.ENGLISH)));
    }
}
