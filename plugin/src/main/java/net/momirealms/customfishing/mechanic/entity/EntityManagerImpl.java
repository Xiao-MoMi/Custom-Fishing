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

package net.momirealms.customfishing.mechanic.entity;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.EntityManager;
import net.momirealms.customfishing.api.mechanic.entity.EntityConfig;
import net.momirealms.customfishing.api.mechanic.entity.EntityLibrary;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.entity.VanillaEntityImpl;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class EntityManagerImpl implements EntityManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, EntityLibrary> entityLibraryMap;
    private final HashMap<String, EntityConfig> entityConfigMap;

    public EntityManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.entityLibraryMap = new HashMap<>();
        this.entityConfigMap = new HashMap<>();
        this.registerEntityLibrary(new VanillaEntityImpl());
    }

    public void load() {
        this.loadConfig();
    }

    public void unload() {
        HashMap<String, EntityConfig> tempMap = new HashMap<>(this.entityConfigMap);
        this.entityConfigMap.clear();
        for (Map.Entry<String, EntityConfig> entry : tempMap.entrySet()) {
            if (entry.getValue().isPersist()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public boolean registerEntityLibrary(EntityLibrary entityLibrary) {
        if (entityLibraryMap.containsKey(entityLibrary.identification())) return false;
        else entityLibraryMap.put(entityLibrary.identification(), entityLibrary);
        return true;
    }

    @Override
    public boolean unregisterEntityLibrary(String lib) {
        return entityLibraryMap.remove(lib) != null;
    }

    @Override
    public boolean unregisterEntityLibrary(EntityLibrary entityLibrary) {
        return unregisterEntityLibrary(entityLibrary.identification());
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("entity")) {
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
                String entityID = section.getString("entity");
                if (entityID == null) {
                    LogUtils.warn("Entity can't be null. File:" + file.getAbsolutePath() + "; Section:" + section.getCurrentPath());
                    continue;
                }
                HashMap<String, Object> propertyMap = new HashMap<>();
                ConfigurationSection property = section.getConfigurationSection("properties");
                if (property != null) {
                    propertyMap.putAll(property.getValues(false));
                }
                EntityConfig entityConfig = new EntityConfig.Builder()
                        .entityID(entityID)
                        .persist(false)
                        .horizontalVector(section.getDouble("velocity.horizontal", 1.1))
                        .verticalVector(section.getDouble("velocity.vertical", 1.2))
                        .propertyMap(propertyMap)
                        .build();
                entityConfigMap.put(entry.getKey(), entityConfig);
            }
        }
    }

    public void disable() {
        unload();
        this.entityConfigMap.clear();
        this.entityLibraryMap.clear();
    }

    @Override
    public void summonEntity(Location hookLocation, Location playerLocation, Loot loot) {
        EntityConfig config = entityConfigMap.get(loot.getID());
        if (config == null) {
            LogUtils.warn("Entity: " + loot.getID() + " doesn't exist.");
            return;
        }
        String entityID = config.getEntityID();
        Entity entity;
        if (entityID.contains(":")) {
            String[] split = entityID.split(":", 2);
            String identification = split[0];
            String id = split[1];
            EntityLibrary library = entityLibraryMap.get(identification);
            entity = library.spawn(hookLocation, id, config.getPropertyMap());
        } else {
            entity = entityLibraryMap.get("vanilla").spawn(hookLocation, entityID, config.getPropertyMap());
        }
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply((config.getHorizontalVector()) - 1);
        vector = vector.setY((vector.getY() + 0.2) * config.getVerticalVector());
        entity.setVelocity(vector);
    }
}
