package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.Location;

import java.util.Collection;
import java.util.List;

public interface TotemManager extends Reloadable {

    Collection<String> getActivatedTotems(Location location);
}
