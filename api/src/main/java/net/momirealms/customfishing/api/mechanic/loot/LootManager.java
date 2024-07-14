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

package net.momirealms.customfishing.api.mechanic.loot;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface for managing loot
 */
public interface LootManager extends Reloadable {

    /**
     * Registers a new loot item.
     *
     * @param loot the {@link Loot} to be registered
     * @return true if the loot was successfully registered, false otherwise
     */
    boolean registerLoot(@NotNull Loot loot);

    /**
     * Retrieves the members of a loot group identified by the given key.
     *
     * @param key the key identifying the loot group
     * @return a list of member identifiers as strings
     */
    @NotNull
    List<String> getGroupMembers(String key);

    /**
     * Retrieves a loot item by its key.
     *
     * @param key the key identifying the loot item
     * @return an {@link Optional} containing the {@link Loot} if found, or an empty {@link Optional} if not
     */
    @NotNull
    Optional<Loot> getLoot(String key);

    /**
     * Retrieves a map of weighted loots based on the given effect and context.
     *
     * @param effect  the {@link Effect} influencing the loot selection
     * @param context the {@link Context} in which the loot selection occurs
     * @return a map of loot keys to their respective weights
     */
    Map<String, Double> getWeightedLoots(Effect effect, Context<Player> context);

    /**
     * Retrieves the next loot item based on the given effect and context.
     *
     * @param effect  the {@link Effect} influencing the loot selection
     * @param context the {@link Context} in which the loot selection occurs
     * @return the next {@link Loot} item, or null if no suitable loot is found
     */
    @Nullable
    Loot getNextLoot(Effect effect, Context<Player> context);
}
