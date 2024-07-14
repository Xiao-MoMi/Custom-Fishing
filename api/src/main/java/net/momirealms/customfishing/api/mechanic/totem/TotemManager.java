/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * Interface for managing totems.
 */
public interface TotemManager extends Reloadable {

    /**
     * Retrieves a collection of activated totems at the specified location.
     *
     * @param location the {@link Location} to check for activated totems
     * @return a collection of activated totem identifiers as strings
     */
    Collection<String> getActivatedTotems(Location location);

    /**
     * Registers a new totem configuration.
     *
     * @param totem the {@link TotemConfig} to be registered
     * @return true if the totem was successfully registered, false otherwise
     */
    boolean registerTotem(TotemConfig totem);

    /**
     * Retrieves a totem configuration by its unique identifier.
     *
     * @param id the unique identifier of the totem
     * @return an {@link Optional} containing the {@link TotemConfig} if found, or an empty {@link Optional} if not
     */
    @NotNull
    Optional<TotemConfig> getTotem(String id);
}
