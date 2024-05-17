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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The PlayerContextImpl class implements the Context interface specifically
 * for the Player type. It allows for storing and retrieving arguments related
 * to a player in the custom fishing mechanics.
 */
public final class PlayerContextImpl implements Context<Player> {

    private final Player player;
    private final HashMap<ContextKeys<?>, Object> args = new HashMap<>();

    /**
     * Constructs a new PlayerContextImpl with the specified player.
     *
     * @param player the player to be associated with this context.
     */
    public PlayerContextImpl(@NotNull Player player) {
        this.player = player;
        final Location location = player.getLocation();
        arg(ContextKeys.LOCATION, location)
        .arg(ContextKeys.X, location.getBlockX())
        .arg(ContextKeys.Y, location.getBlockY())
        .arg(ContextKeys.Z, location.getBlockZ())
        .arg(ContextKeys.WORLD, location.getWorld().getName());
    }

    /**
     * Retrieves the map of arguments associated with this context.
     *
     * @return a map where the keys are argument names and the values are argument values.
     */
    @Override
    public Map<ContextKeys<?>, Object> args() {
        return args;
    }

    /**
     * Adds an argument to the context and returns the context itself
     * to allow for method chaining.
     *
     * @param key the name of the argument to add.
     * @param value the value of the argument to add.
     * @return the PlayerContextImpl instance to allow for method chaining.
     */
    @Override
    public <C> PlayerContextImpl arg(ContextKeys<C> key, C value) {
        args.put(key, value);
        return this;
    }

    /**
     * Retrieves the value of a specific argument from the context.
     *
     * @param key the name of the argument to retrieve.
     * @return the value of the argument, or null if no argument with the given key exists.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C> C arg(ContextKeys<C> key) {
        return (C) args.get(key);
    }

    /**
     * Gets the player associated with this context.
     *
     * @return the player object associated with this context.
     */
    @Override
    public Player getHolder() {
        return player;
    }
}
