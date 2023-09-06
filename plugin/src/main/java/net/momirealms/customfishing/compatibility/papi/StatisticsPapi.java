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
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StatisticsPapi extends PlaceholderExpansion {

    private final CustomFishingPlugin plugin;

    public StatisticsPapi(CustomFishingPlugin plugin) {
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
        return "fishingstats";
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
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        OnlineUser onlineUser = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
        if (onlineUser == null) return "";
        Statistics statistics = onlineUser.getStatistics();
        if (params.equals("total")) {
            return String.valueOf(statistics.getTotalCatchAmount());
        }

        String[] split = params.split("_");
        switch (split[0]) {
            case "hascaught" -> {
                return String.valueOf(statistics.getLootAmount(split[1]) != 0);
            }
            case "category" -> {
                List<String> category = plugin.getStatisticsManager().getCategory(split[2]);
                if (category == null) return "0";
                if (split[1].equals("total")) {
                    int total = 0;
                    for (String loot : category) {
                        total += statistics.getLootAmount(loot);
                    }
                    return String.valueOf(total);
                } else if (split[1].equals("progress")) {
                    int size = category.size();
                    int unlocked = 0;
                    for (String loot : category) {
                        if (statistics.getLootAmount(loot) != 0) unlocked++;
                    }
                    double percent = (double) unlocked / size;
                    String progress = String.format("%.1f", percent);
                    return progress.equals("100.0") ? "100" : progress;
                }
            }
        }

        return "null";
    }
}
