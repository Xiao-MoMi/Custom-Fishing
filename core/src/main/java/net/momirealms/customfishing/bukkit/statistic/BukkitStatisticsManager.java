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

package net.momirealms.customfishing.bukkit.statistic;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class BukkitStatisticsManager implements StatisticsManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, List<String>> categoryMap;

    public BukkitStatisticsManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.categoryMap = new HashMap<>();
    }

    public void load() {
        this.loadCategoriesFromPluginFolder();
    }

    public void unload() {
        this.categoryMap.clear();
    }

    public void disable() {
        unload();
    }

    /**
     * Get the statistics for a player with the given UUID.
     *
     * @param uuid The UUID of the player for whom statistics are retrieved.
     * @return The player's statistics or null if the player is not found.
     */
    @Override
    @Nullable
    public Statistics getStatistics(UUID uuid) {
        OnlineUserData onlineUser = plugin.getStorageManager().getOnlineUser(uuid);
        if (onlineUser == null) return null;
        return onlineUser.getStatistics();
    }

    @SuppressWarnings("DuplicatedCode")
    public void loadCategoriesFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("category")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                        this.loadSingleFile(subFile);
                    }
                }
            }
        }
    }

    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(false)) {
            categoryMap.put(key, config.getStringList(key));
        }
    }

    /**
     * Get a list of strings associated with a specific key in a category map.
     *
     * @param key The key to look up in the category map.
     * @return A list of strings associated with the key or null if the key is not found.
     */
    @Override
    @Nullable
    public List<String> getCategory(String key) {
        return categoryMap.get(key);
    }
}
