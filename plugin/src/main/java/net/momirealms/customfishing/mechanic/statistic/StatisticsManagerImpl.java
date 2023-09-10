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

package net.momirealms.customfishing.mechanic.statistic;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import net.momirealms.customfishing.api.manager.StatisticsManager;
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class StatisticsManagerImpl implements StatisticsManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, List<String>> categoryMap;

    public StatisticsManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.categoryMap = new HashMap<>();
    }

    public void load() {
        this.loadCategoriesFromPluginFolder();
        LogUtils.info("Loaded " + categoryMap.size() + " categories.");
    }

    public void unload() {
        this.categoryMap.clear();
    }

    public void disable() {
        unload();
    }

    @Override
    public Statistics getStatistics(UUID uuid) {
        OnlineUser onlineUser = plugin.getStorageManager().getOnlineUser(uuid);
        if (onlineUser == null) return null;
        return onlineUser.getStatistics();
    }

    @SuppressWarnings("DuplicatedCode")
    public void loadCategoriesFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("categories")) {
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

    @Override
    @Nullable
    public List<String> getCategory(String key) {
        return categoryMap.get(key);
    }
}
