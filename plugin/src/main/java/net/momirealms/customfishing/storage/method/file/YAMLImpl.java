package net.momirealms.customfishing.storage.method.file;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.*;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.storage.method.AbstractStorage;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YAMLImpl extends AbstractStorage {

    public YAMLImpl(CustomFishingPlugin plugin) {
        super(plugin);
        File folder = new File(plugin.getDataFolder(), "data");
        if (!folder.exists()) folder.mkdirs();
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.YAML;
    }

    public File getPlayerDataFile(UUID uuid) {
        return new File(plugin.getDataFolder(), "data" + File.separator + uuid + ".yml");
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean ignore) {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) {
            if (Bukkit.getPlayer(uuid) != null) {
                return CompletableFuture.completedFuture(Optional.of(PlayerData.empty()));
            } else {
                return CompletableFuture.completedFuture(Optional.of(PlayerData.NEVER_PLAYED));
            }
        }
        YamlConfiguration data = ConfigUtils.readData(dataFile);
        PlayerData playerData = new PlayerData.Builder()
                .setBagData(new InventoryData(data.getString("bag", ""), data.getInt("size", 9)))
                .setEarningData(new EarningData(data.getDouble("earnings"), data.getInt("date")))
                .setStats(getStatistics(data.getConfigurationSection("stats")))
                .setName(data.getString("name"))
                .build();
        return CompletableFuture.completedFuture(Optional.of(playerData));
    }

    @Override
    public CompletableFuture<Boolean> setPlayData(UUID uuid, PlayerData playerData, boolean ignore) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("name", playerData.getName());
        data.set("bag", playerData.getBagData().serialized);
        data.set("size", playerData.getBagData().size);
        data.set("date", playerData.getEarningData().date);
        data.set("earnings", playerData.getEarningData().earnings);
        ConfigurationSection section = data.createSection("stats");
        for (Map.Entry<String, Integer> entry : playerData.getStatistics().statisticMap.entrySet()) {
            section.set(entry.getKey(), entry.getValue());
        }
        try {
            data.save(getPlayerDataFile(uuid));
        } catch (IOException e) {
            LogUtils.warn("Failed to save player data", e);
        }
        return CompletableFuture.completedFuture(true);
    }

    public StatisticData getStatistics(ConfigurationSection section) {
        if (section == null)
            return StatisticData.empty();
        else {
            HashMap<String, Integer> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                map.put(entry.getKey(), (Integer) entry.getValue());
            }
            return new StatisticData(map);
        }
    }
}
