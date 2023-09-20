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
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A data storage implementation that uses YAML files to store player data, with support for legacy data.
 */
public class YAMLImpl extends AbstractStorage implements LegacyDataStorageInterface {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public YAMLImpl(CustomFishingPlugin plugin) {
        super(plugin);
        File folder = new File(plugin.getDataFolder(), "data");
        if (!folder.exists()) folder.mkdirs();
    }

    @Override
    public StorageType getStorageType() {
        return StorageType.YAML;
    }

    /**
     * Get the file associated with a player's UUID for storing YAML data.
     *
     * @param uuid The UUID of the player.
     * @return The file for the player's data.
     */
    public File getPlayerDataFile(UUID uuid) {
        return new File(plugin.getDataFolder(), "data" + File.separator + uuid + ".yml");
    }

    @Override
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock) {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) {
            if (Bukkit.getPlayer(uuid) != null) {
                return CompletableFuture.completedFuture(Optional.of(PlayerData.LOCKED));
            } else {
                return CompletableFuture.completedFuture(Optional.of(PlayerData.empty()));
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
    public CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean ignore) {
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

    @Override
    public Set<UUID> getUniqueUsers(boolean legacy) {
        File folder;
        if (legacy) {
            folder = new File(plugin.getDataFolder(), "data/fishingbag");
        } else {
            folder = new File(plugin.getDataFolder(), "data");
        }
        Set<UUID> uuids = new HashSet<>();
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    uuids.add(UUID.fromString(file.getName().substring(0, file.getName().length() - 4)));
                }
            }
        }
        return uuids;
    }

    /**
     * Parse statistics data from a YAML ConfigurationSection.
     *
     * @param section The ConfigurationSection containing statistics data.
     * @return The parsed StatisticData object.
     */
    private StatisticData getStatistics(ConfigurationSection section) {
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

    @Override
    public CompletableFuture<Optional<PlayerData>> getLegacyPlayerData(UUID uuid) {
        // Retrieve legacy player data (YAML format) for a given UUID.
        var builder = new PlayerData.Builder().setName("");
        File bagFile = new File(plugin.getDataFolder(), "data/fishingbag/" + uuid + ".yml");
        if (bagFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(bagFile);
            String contents = yaml.getString("contents", "");
            int size = yaml.getInt("size", 9);
            builder.setBagData(new InventoryData(contents, size));
        } else {
            builder.setBagData(InventoryData.empty());
        }

        File statFile = new File(plugin.getDataFolder(), "data/statistics/" + uuid + ".yml");
        if (statFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(statFile);
            HashMap<String, Integer> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : yaml.getValues(false).entrySet()) {
                if (entry.getValue() instanceof Integer integer) {
                    map.put(entry.getKey(), integer);
                }
            }
            builder.setStats(new StatisticData(map));
        } else {
            builder.setStats(StatisticData.empty());
        }

        File sellFile = new File(plugin.getDataFolder(), "data/sell/" + uuid + ".yml");
        if (sellFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(sellFile);
            builder.setEarningData(new EarningData(yaml.getDouble("earnings"), yaml.getInt("date")));
        } else {
            builder.setEarningData(EarningData.empty());
        }

        return CompletableFuture.completedFuture(Optional.of(builder.build()));
    }
}
