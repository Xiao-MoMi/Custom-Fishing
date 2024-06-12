package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface TotemManager extends Reloadable {

    Collection<String> getActivatedTotems(Location location);

    boolean registerTotem(TotemConfig totem);

    @NotNull
    Optional<TotemConfig> getTotem(String id);
}
