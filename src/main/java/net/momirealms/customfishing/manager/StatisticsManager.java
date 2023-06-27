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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.data.PlayerStatisticsData;
import net.momirealms.customfishing.fishing.loot.LootImpl;
import net.momirealms.customfishing.listener.JoinQuitListener;
import net.momirealms.customfishing.object.DataFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StatisticsManager extends DataFunction {

    private final ConcurrentHashMap<UUID, PlayerStatisticsData> statisticsDataMap;
    private final JoinQuitListener joinQuitListener;
    private final CustomFishing plugin;

    public StatisticsManager(CustomFishing plugin) {
        super();
        this.statisticsDataMap = new ConcurrentHashMap<>();
        this.joinQuitListener = new JoinQuitListener(this);
        this.plugin = plugin;
    }

    @Override
    public void load() {
        if (!ConfigManager.enableStatistics) return;
        Bukkit.getPluginManager().registerEvents(joinQuitListener, plugin);
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(joinQuitListener);
    }

    public void saveStatisticsDataForOnlinePlayers(boolean unlock) {
        plugin.getDataManager().getDataStorageInterface().saveStatistics(statisticsDataMap.entrySet(), unlock);
    }

    @Override
    public void disable() {
        unload();
        saveStatisticsDataForOnlinePlayers(true);
        statisticsDataMap.clear();
    }

    @Override
    public void onQuit(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerStatisticsData playerStatisticsData = statisticsDataMap.remove(uuid);
        triedTimes.remove(player.getUniqueId());
        if (playerStatisticsData != null) {
            plugin.getScheduler().runTaskAsync(() -> plugin.getDataManager().getDataStorageInterface().saveStatistics(uuid, playerStatisticsData, true));
        }
    }

    @Override
    public void onJoin(Player player) {
        plugin.getScheduler().runTaskAsyncLater(() -> joinReadData(player, false), 500, TimeUnit.MILLISECONDS);
    }

    public void joinReadData(Player player, boolean force) {
        if (player == null || !player.isOnline()) return;
        PlayerStatisticsData statisticsData = plugin.getDataManager().getDataStorageInterface().loadStatistics(player.getUniqueId(), force);
        if (statisticsData != null) {
            statisticsDataMap.put(player.getUniqueId(), statisticsData);
        } else if (!force) {
            if (checkTriedTimes(player.getUniqueId())) {
                plugin.getScheduler().runTaskAsyncLater(() -> joinReadData(player, false), 2500, TimeUnit.MILLISECONDS);
            } else {
                plugin.getScheduler().runTaskAsyncLater(() -> joinReadData(player, true), 2500, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void addFishAmount(UUID uuid, LootImpl loot, int amount) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            statisticsData.addFishAmount(loot, uuid, amount);
        }
    }

    public int getFishAmount(UUID uuid, String key) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            return statisticsData.getFishAmount(key);
        }
        return -1;
    }

    public int getTotalFishAmount(UUID uuid) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            return statisticsData.getTotalCatchAmount();
        }
        return -1;
    }

    public boolean hasFished(UUID uuid, String key) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            return statisticsData.hasFished(key);
        }
        return false;
    }

    public double getCategoryUnlockProgress(UUID uuid, String category) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            return statisticsData.getCategoryUnlockProgress(category);
        }
        return -1d;
    }

    public int getCategoryTotalFishAmount(UUID uuid, String category) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            return statisticsData.getCategoryTotalFishAmount(category);
        }
        return -1;
    }

    public boolean reset(UUID uuid) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            statisticsData.reset();
            return true;
        }
        return false;
    }

    public void setData(UUID uuid, String key, int amount) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            statisticsData.setData(key, amount);
        }
    }
}
