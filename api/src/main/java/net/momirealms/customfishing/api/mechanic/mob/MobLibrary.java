package net.momirealms.customfishing.api.mechanic.mob;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Map;

public interface MobLibrary {

    String identification();

    Entity spawn(Location location, String id, Map<String, Object> mobPropertyMap);
}
