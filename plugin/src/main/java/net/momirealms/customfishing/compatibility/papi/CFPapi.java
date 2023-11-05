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

package net.momirealms.customfishing.compatibility.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CFPapi extends PlaceholderExpansion {

    private final CustomFishingPlugin plugin;

    public CFPapi(CustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        super.register();
    }

    public void unload() {
        super.unregister();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "customfishing";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        String[] split = params.split("_");

        Player player = offlinePlayer.getPlayer();
        if (player == null)
            return "";

        switch (split[0]) {
            case "market" -> {
                if (split.length < 2)
                    return null;
                switch (split[1]) {
                    case "limit" -> {
                        if (split.length < 3) {
                            return String.format("%.2f", plugin.getMarketManager().getEarningLimit(player));
                        } else {
                            Player another = Bukkit.getPlayer(split[2]);
                            if (another == null) {
                                return "";
                            }
                            return String.format("%.2f", plugin.getMarketManager().getEarningLimit(another));
                        }
                    }
                    case "earnings" -> {
                        OnlineUser user;
                        if (split.length < 3) {
                            user = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
                        } else {
                            Player another = Bukkit.getPlayer(split[2]);
                            if (another == null) {
                                return "";
                            }
                            user = plugin.getStorageManager().getOnlineUser(another.getUniqueId());
                        }
                        if (user == null)
                            return "";
                        return String.format("%.2f", user.getEarningData().earnings);
                    }
                    case "canearn" -> {
                        if (split.length < 3) {
                            OnlineUser user = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
                            if (user == null)
                                return "";
                            return String.format("%.2f", plugin.getMarketManager().getEarningLimit(player) - user.getEarningData().earnings);
                        } else {
                            Player another = Bukkit.getPlayer(split[2]);
                            if (another == null) {
                                return "";
                            }

                            OnlineUser user = plugin.getStorageManager().getOnlineUser(another.getUniqueId());
                            if (user == null)
                                return "";
                            return String.format("%.2f", plugin.getMarketManager().getEarningLimit(another) - user.getEarningData().earnings);
                        }
                    }
                }
            }
        }
        return null;
    }
}
