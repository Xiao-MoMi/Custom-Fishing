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

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.entity.EntityManager;
import net.momirealms.customfishing.api.mechanic.entity.EntityConfigImpl;
import net.momirealms.customfishing.api.integration.EntityProvider;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.bukkit.compatibility.entity.VanillaEntityImpl;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

public class EntityManagerImpl implements EntityManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, EntityProvider> entityLibraryMap;
    private final HashMap<String, EntityConfigImpl> entityConfigMap;

    public EntityManagerImpl(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.entityLibraryMap = new HashMap<>();
        this.entityConfigMap = new HashMap<>();
        this.registerEntityProvider(new VanillaEntityImpl());
    }

    public void load() {
        this.loadConfig();
    }

    public void unload() {
        HashMap<String, EntityConfigImpl> tempMap = new HashMap<>(this.entityConfigMap);
        this.entityConfigMap.clear();
        for (Map.Entry<String, EntityConfigImpl> entry : tempMap.entrySet()) {
            if (entry.getValue().isPersist()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Registers an entity library for use in the plugin.
     *
     * @param entityProvider The entity library to register.
     * @return {@code true} if the entity library was successfully registered, {@code false} if it already exists.
     */
    @Override
    public boolean registerEntityProvider(EntityProvider entityProvider) {
        if (entityLibraryMap.containsKey(entityProvider.identifier())) return false;
        else entityLibraryMap.put(entityProvider.identifier(), entityProvider);
        return true;
    }

    /**
     * Unregisters an entity library by its identification key.
     *
     * @param identification The identification key of the entity library to unregister.
     * @return {@code true} if the entity library was successfully unregistered, {@code false} if it does not exist.
     */
    @Override
    public boolean unregisterEntityProvider(String identification) {
        return entityLibraryMap.remove(identification) != null;
    }

    /**
     * Load configuration files for entity properties.
     */
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

    /**
     * Load a single entity configuration file.
     *
     * @param file The YAML file to load.
     */
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
                EntityConfigImpl entityConfigImpl = new EntityConfigImpl.BuilderImpl()
                        .entityID(entityID)
                        .persist(false)
                        .horizontalVector(section.getDouble("velocity.horizontal", 1.1))
                        .verticalVector(section.getDouble("velocity.vertical", 1.2))
                        .propertyMap(propertyMap)
                        .build();
                entityConfigMap.put(entry.getKey(), entityConfigImpl);
            }
        }
    }

    public void disable() {
        unload();
        this.entityConfigMap.clear();
        this.entityLibraryMap.clear();
    }

    /**
     * Summons an entity based on the given loot configuration to a specified location.
     *
     * @param hookLocation   The location where the entity will be summoned, typically where the fishing hook is.
     * @param playerLocation The location of the player who triggered the entity summoning.
     * @param loot           The loot configuration that defines the entity to be summoned.
     */
    @Override
    public void summonEntity(Location hookLocation, Location playerLocation, Loot loot) {
        EntityConfigImpl config = entityConfigMap.get(loot.getID());
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
            EntityProvider library = entityLibraryMap.get(identification);
            entity = library.spawn(hookLocation, id, config.getPropertyMap());
        } else {
            entity = entityLibraryMap.get("vanilla").spawn(hookLocation, entityID, config.getPropertyMap());
        }
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply((config.getHorizontalVector()) - 1);
        vector = vector.setY((vector.getY() + 0.2) * config.getVerticalVector());
        entity.setVelocity(vector);
    }
}
