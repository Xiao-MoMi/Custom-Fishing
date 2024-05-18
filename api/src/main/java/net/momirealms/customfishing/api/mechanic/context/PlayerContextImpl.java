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
import org.jetbrains.annotations.Nullable;

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
    public PlayerContextImpl(@Nullable Player player) {
        this.player = player;
        if (player == null) return;
        final Location location = player.getLocation();
        arg(ContextKeys.LOCATION, location)
        .arg(ContextKeys.X, location.getBlockX())
        .arg(ContextKeys.Y, location.getBlockY())
        .arg(ContextKeys.Z, location.getBlockZ())
        .arg(ContextKeys.WORLD, location.getWorld().getName());
    }

    @Override
    public Map<ContextKeys<?>, Object> args() {
        return args;
    }

    @Override
    public Map<String, String> toPlaceholderMap() {
        HashMap<String, String> placeholders = new HashMap<>();
        for (Map.Entry<ContextKeys<?>, Object> entry : args.entrySet()) {
            placeholders.put(entry.getKey().toString(), entry.getValue().toString());
        }
        return placeholders;
    }

    @Override
    public <C> PlayerContextImpl arg(ContextKeys<C> key, C value) {
        args.put(key, value);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C arg(ContextKeys<C> key) {
        return (C) args.get(key);
    }

    @Override
    public Player getHolder() {
        return player;
    }
}
