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

package net.momirealms.customfishing.bukkit.integration.papi;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.statistic.FishingStatistics;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.data.StatisticData;
import net.momirealms.customfishing.api.storage.user.UserData;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class StatisticsPapi extends PlaceholderExpansion {

    private final Cache<UUID, Optional<PlayerData>> offlineDataCache;

    private final BukkitCustomFishingPlugin plugin;

    public StatisticsPapi(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.offlineDataCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();
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
        return "2.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        Optional<UserData> onlineUser = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
        String[] split = params.split("_", 2);
        if (onlineUser.isPresent()) {
            return onlineUser.map(
                    data -> {
                        FishingStatistics statistics = data.statistics();
                        switch (split[0]) {
                            case "total" -> {
                                return String.valueOf(statistics.amountOfFishCaught());
                            }
                            case "hascaught" -> {
                                if (split.length == 1) return "Invalid format";
                                return String.valueOf(statistics.getAmount(split[1]) != 0);
                            }
                            case "amount" -> {
                                if (split.length == 1) return "Invalid format";
                                return String.valueOf(statistics.getAmount(split[1]));
                            }
                            case "size-record" -> {
                                float size = statistics.getMaxSize(split[1]);
                                return String.format("%.2f", size < 0 ? 0 : size);
                            }
                            case "category" -> {
                                if (split.length == 1) return "Invalid format";
                                String[] categorySplit = split[1].split("_", 2);
                                if (categorySplit.length == 1) return "Invalid format";
                                List<String> category = plugin.getStatisticsManager().getCategoryMembers(categorySplit[1]);
                                if (categorySplit[0].equals("total")) {
                                    int total = 0;
                                    for (String loot : category) {
                                        total += statistics.getAmount(loot);
                                    }
                                    return String.valueOf(total);
                                } else if (categorySplit[0].equals("progress")) {
                                    int size = category.size();
                                    int unlocked = 0;
                                    for (String loot : category) {
                                        if (statistics.getAmount(loot) != 0) unlocked++;
                                    }
                                    double percent = ((double) unlocked * 100) / size;
                                    String progress = String.format("%.1f", percent);
                                    return progress.equals("100.0") ? "100" : progress;
                                }
                            }
                        }
                        return null;
                    }
            ).orElse("");
        } else {
            Optional<PlayerData> optional = offlineDataCache.get(player.getUniqueId(), (uuid) -> {
                CompletableFuture<Optional<PlayerData>> data = plugin.getStorageManager().getDataSource().getPlayerData(player.getUniqueId(), false, Runnable::run);
                try {
                    return data.get();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            if (optional.isPresent()) {
                PlayerData playerData = optional.get();
                StatisticData statistics = playerData.statistics();
                switch (split[0]) {
                    case "total" -> {
                        int total = 0;
                        for (int i : statistics.amountMap.values()) {
                            total += i;
                        }
                        return String.valueOf(total);
                    }
                    case "hascaught" -> {
                        if (split.length == 1) return "Invalid format";
                        return String.valueOf(statistics.amountMap.getOrDefault(split[1], 0) != 0);
                    }
                    case "amount" -> {
                        if (split.length == 1) return "Invalid format";
                        return String.valueOf(statistics.amountMap.getOrDefault(split[1], 0));
                    }
                    case "size-record" -> {
                        float size = statistics.sizeMap.getOrDefault(split[1], 0f);
                        return String.format("%.2f", size < 0 ? 0 : size);
                    }
                    case "category" -> {
                        if (split.length == 1) return "Invalid format";
                        String[] categorySplit = split[1].split("_", 2);
                        if (categorySplit.length == 1) return "Invalid format";
                        List<String> category = plugin.getStatisticsManager().getCategoryMembers(categorySplit[1]);
                        if (categorySplit[0].equals("total")) {
                            int total = 0;
                            for (String loot : category) {
                                total += statistics.amountMap.getOrDefault(loot, 0);
                            }
                            return String.valueOf(total);
                        } else if (categorySplit[0].equals("progress")) {
                            int size = category.size();
                            int unlocked = 0;
                            for (String loot : category) {
                                if (statistics.amountMap.getOrDefault(loot, 0) != 0) unlocked++;
                            }
                            double percent = ((double) unlocked * 100) / size;
                            String progress = String.format("%.1f", percent);
                            return progress.equals("100.0") ? "100" : progress;
                        }
                    }
                }
                return null;
            } else {
                return "";
            }
        }
    }
}
