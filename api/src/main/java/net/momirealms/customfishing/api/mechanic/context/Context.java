/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

package net.momirealms.customfishing.api.mechanic.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * The Context interface represents a generic context for custom fishing mechanics.
 * It allows for storing and retrieving arguments, as well as getting the holder
 * of the context. This can be used to maintain state or pass parameters within
 * the custom fishing mechanics.
 *
 * @param <T> the type of the holder object for this context
 */
public interface Context<T> {

    /**
     * Retrieves the map of arguments associated with this context.
     *
     * @return a map where the keys are argument names and the values are argument values.
     */
    Map<ContextKeys<?>, Object> args();

    <C> Context<T> arg(ContextKeys<C> key, C value);

    <C> C arg(ContextKeys<C> key);

    /**
     * Gets the holder of this context.
     *
     * @return the holder object of type T.
     */
    T getHolder();

    /**
     * Creates a player-specific context.
     *
     * @param player the player to be used as the holder of the context.
     * @return a new Context instance with the specified player as the holder.
     */
    static Context<Player> player(@NotNull Player player) {
        return new PlayerContextImpl(player);
    }
}
