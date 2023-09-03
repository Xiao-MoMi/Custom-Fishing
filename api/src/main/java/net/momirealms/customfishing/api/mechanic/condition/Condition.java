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

package net.momirealms.customfishing.api.mechanic.condition;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Condition {

    @Nullable
    protected final Location location;
    @Nullable
    protected final Player player;
    @NotNull
    protected final Map<String, String> args;

    public Condition() {
        this(null, null, new HashMap<>());
    }

    public Condition(HashMap<String, String> args) {
        this(null, null, args);
    }

    public Condition(Player player) {
        this(player.getLocation(), player, new HashMap<>());
    }

    public Condition(Player player, Map<String, String> args) {
        this(player.getLocation(), player, args);
    }

    public Condition(@Nullable Location location, @Nullable Player player, @NotNull Map<String, String> args) {
        this.location = location;
        this.player = player;
        this.args = args;
        if (player != null)
            this.args.put("{player}", player.getName());
        if (location != null) {
            this.args.put("{x}", String.valueOf(location.getX()));
            this.args.put("{y}", String.valueOf(location.getY()));
            this.args.put("{z}", String.valueOf(location.getZ()));
            this.args.put("{world}", location.getWorld().getName());
        }
    }

    @Nullable
    public Location getLocation() {
        return location;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Map<String, String> getArgs() {
        return args;
    }

    @Nullable
    public String getArg(String key) {
        return args.get(key);
    }

    public void insertArg(String key, String value) {
        args.put(key, value);
    }
}
