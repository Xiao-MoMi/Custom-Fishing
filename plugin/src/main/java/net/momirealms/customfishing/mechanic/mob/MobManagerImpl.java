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

package net.momirealms.customfishing.mechanic.mob;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.MobManager;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.mob.MobConfig;
import net.momirealms.customfishing.api.mechanic.mob.MobLibrary;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.mob.VanillaMobImpl;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class MobManagerImpl implements MobManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, MobLibrary> mobLibraryMap;
    private final HashMap<String, MobConfig> mobConfigMap;

    public MobManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.mobLibraryMap = new HashMap<>();
        this.mobConfigMap = new HashMap<>();
        this.registerMobLibrary(new VanillaMobImpl());
    }

    public void load() {
        this.loadConfig();
    }

    public void unload() {
        HashMap<String, MobConfig> tempMap = new HashMap<>(this.mobConfigMap);
        this.mobConfigMap.clear();
        for (Map.Entry<String, MobConfig> entry : tempMap.entrySet()) {
            if (entry.getValue().isPersist()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean registerMobLibrary(MobLibrary mobLibrary) {
        if (mobLibraryMap.containsKey(mobLibrary.identification())) return false;
        else mobLibraryMap.put(mobLibrary.identification(), mobLibrary);
        return true;
    }

    @Override
    public boolean unregisterMobLibrary(String lib) {
        return mobLibraryMap.remove(lib) != null;
    }

    @Override
    public boolean unregisterMobLibrary(MobLibrary mobLibrary) {
        return unregisterMobLibrary(mobLibrary.identification());
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("mobs")) {
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
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                String mobID = section.getString("mob");
                if (mobID == null) {
                    LogUtils.warn("Mob can't be null. File:" + file.getAbsolutePath() + "; Section:" + section.getCurrentPath());
                    continue;
                }
                HashMap<String, Object> propertyMap = new HashMap<>();
                ConfigurationSection property = section.getConfigurationSection("properties");
                if (property != null) {
                    propertyMap.putAll(property.getValues(false));
                }
                MobConfig mobConfig = new MobConfig.Builder()
                        .mobID(mobID)
                        .persist(false)
                        .horizontalVector(section.getDouble("vector.horizontal", 1.1))
                        .verticalVector(section.getDouble("vector.vertical", 1.2))
                        .propertyMap(propertyMap)
                        .build();
                mobConfigMap.put(entry.getKey(), mobConfig);
            }
        }
    }

    public void disable() {
        unload();
        this.mobConfigMap.clear();
        this.mobLibraryMap.clear();
    }

    @Override
    public void summonMob(Location hookLocation, Location playerLocation, Loot loot) {
        MobConfig config = mobConfigMap.get(loot.getID());
        if (config == null) {
            LogUtils.warn("Mob: " + loot.getID() + " doesn't exist.");
            return;
        }
        String mobID = config.getMobID();
        Entity entity;
        if (mobID.contains(":")) {
            String[] split = mobID.split(":", 2);
            String identification = split[0];
            String id = split[1];
            MobLibrary library = mobLibraryMap.get(identification);
            entity = library.spawn(hookLocation, id, config.getPropertyMap());
        } else {
            entity = mobLibraryMap.get("vanilla").spawn(hookLocation, mobID, config.getPropertyMap());
        }
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply((config.getHorizontalVector()) - 1);
        vector = vector.setY((vector.getY() + 0.2) * config.getVerticalVector());
        entity.setVelocity(vector);
    }
}
