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
        if (onlineUser == null) return "Data not loaded";
        Statistics statistics = onlineUser.getStatistics();
        String[] split = params.split("_", 2);
        switch (split[0]) {
            case "total" -> {
                return String.valueOf(statistics.getTotalCatchAmount());
            }
            case "hascaught" -> {
                if (split.length == 1) return "Invalid format";
                return String.valueOf(statistics.getLootAmount(split[1]) != 0);
            }
            case "amount" -> {
                if (split.length == 1) return "Invalid format";
                return String.valueOf(statistics.getLootAmount(split[1]));
            }
            case "size-record" -> {
                return String.format("%.2f", statistics.getSizeRecord(split[1]));
            }
            case "category" -> {
                if (split.length == 1) return "Invalid format";
                String[] categorySplit = split[1].split("_", 2);
                if (categorySplit.length == 1) return "Invalid format";
                List<String> category = plugin.getStatisticsManager().getCategory(categorySplit[1]);
                if (category == null) return "Category Not Exists";
                if (categorySplit[0].equals("total")) {
                    int total = 0;
                    for (String loot : category) {
                        total += statistics.getLootAmount(loot);
                    }
                    return String.valueOf(total);
                } else if (categorySplit[0].equals("progress")) {
                    int size = category.size();
                    int unlocked = 0;
                    for (String loot : category) {
                        if (statistics.getLootAmount(loot) != 0) unlocked++;
                    }
                    double percent = ((double) unlocked * 100) / size;
                    String progress = String.format("%.1f", percent);
                    return progress.equals("100.0") ? "100" : progress;
                }
            }
        }

        return "null";
    }
}
