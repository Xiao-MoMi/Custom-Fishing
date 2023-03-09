package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.data.PlayerStatisticsData;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.listener.JoinQuitListener;
import net.momirealms.customfishing.object.DataFunction;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

    public void disable() {
        unload();
        DataManager dataManager = plugin.getDataManager();
        for (Map.Entry<UUID, PlayerStatisticsData> entry : statisticsDataMap.entrySet()) {
            dataManager.getDataStorageInterface().saveStatistics(entry.getKey(), entry.getValue(), true);
        }
        statisticsDataMap.clear();
    }

    @Override
    public void onQuit(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerStatisticsData playerStatisticsData = statisticsDataMap.remove(uuid);
        triedTimes.remove(player.getUniqueId());
        if (playerStatisticsData != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                plugin.getDataManager().getDataStorageInterface().saveStatistics(uuid, playerStatisticsData, true);
            });
        }
    }

    @Override
    public void onJoin(Player player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            joinReadData(player, false);
        }, 10);
    }

    public void joinReadData(Player player, boolean force) {
        if (player == null || !player.isOnline()) return;
        PlayerStatisticsData statisticsData = plugin.getDataManager().getDataStorageInterface().loadStatistics(player.getUniqueId(), force);
        if (statisticsData != null) {
            statisticsDataMap.put(player.getUniqueId(), statisticsData);
        }
        else if (!force) {
            if (!checkTriedTimes(player.getUniqueId())) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> joinReadData(player, false), 20);
            }
            else {
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> joinReadData(player, true), 20);
            }
        }
    }

    public void addFishAmount(UUID uuid, Loot loot, int amount) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            statisticsData.addFishAmount(loot, amount, uuid);
        }
    }

    public int getFishAmount(UUID uuid, String key) {
        PlayerStatisticsData statisticsData = statisticsDataMap.get(uuid);
        if (statisticsData != null) {
            return statisticsData.getFishAmount(key);
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
