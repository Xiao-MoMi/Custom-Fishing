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

package net.momirealms.customfishing.bukkit.storage.method.file;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.storage.StorageType;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.data.InventoryData;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.data.StatisticData;
import net.momirealms.customfishing.bukkit.storage.method.AbstractStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class YAMLProvider extends AbstractStorage {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public YAMLProvider(BukkitCustomFishingPlugin plugin) {
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
    public CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock, Executor executor) {
        File dataFile = getPlayerDataFile(uuid);
        if (!dataFile.exists()) {
            if (Bukkit.getPlayer(uuid) != null) {
                var data = PlayerData.empty();
                data.uuid(uuid);
                return CompletableFuture.completedFuture(Optional.of(data));
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        }
        YamlDocument data = plugin.getConfigManager().loadData(dataFile);
        PlayerData playerData = PlayerData.builder()
                .uuid(uuid)
                .bag(new InventoryData(data.getString("bag", ""), data.getInt("size", 9)))
                .earnings(new EarningData(data.getDouble("earnings"), data.getInt("date")))
                .statistics(getStatistics(data.getSection("stats")))
                .name(data.getString("name", ""))
                .build();
        return CompletableFuture.completedFuture(Optional.of(playerData));
    }

    @Override
    public CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean ignore) {
        YamlConfiguration data = new YamlConfiguration();
        data.set("name", playerData.name());
        data.set("bag", playerData.bagData().serialized);
        data.set("size", playerData.bagData().size);
        data.set("date", playerData.earningData().date);
        data.set("earnings", playerData.earningData().earnings);
        ConfigurationSection section = data.createSection("stats");
        ConfigurationSection amountSection = section.createSection("amount");
        ConfigurationSection sizeSection = section.createSection("size");
        for (Map.Entry<String, Integer> entry : playerData.statistics().amountMap.entrySet()) {
            amountSection.set(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Float> entry : playerData.statistics().sizeMap.entrySet()) {
            sizeSection.set(entry.getKey(), entry.getValue());
        }
        try {
            data.save(getPlayerDataFile(uuid));
        } catch (IOException e) {
            plugin.getPluginLogger().warn("Failed to save player data", e);
        }
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Set<UUID> getUniqueUsers() {
        File folder = new File(plugin.getDataFolder(), "data");
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

    private StatisticData getStatistics(Section section) {
        HashMap<String, Integer> amountMap = new HashMap<>();
        HashMap<String, Float> sizeMap = new HashMap<>();
        if (section == null) {
            return new StatisticData(amountMap, sizeMap);
        }
        Section amountSection = section.getSection("amount");
        if (amountSection != null) {
            for (Map.Entry<String, Object> entry : amountSection.getStringRouteMappedValues(false).entrySet()) {
                amountMap.put(entry.getKey(), (Integer) entry.getValue());
            }
        }
        Section sizeSection = section.getSection("size");
        if (sizeSection != null) {
            for (Map.Entry<String, Object> entry : sizeSection.getStringRouteMappedValues(false).entrySet()) {
                sizeMap.put(entry.getKey(), ((Double) entry.getValue()).floatValue());
            }
        }
        return new StatisticData(amountMap, sizeMap);
    }
}
