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

package net.momirealms.customfishing.bukkit.entity;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.integration.EntityProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.entity.EntityConfig;
import net.momirealms.customfishing.api.mechanic.entity.EntityManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class BukkitEntityManager implements EntityManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, EntityProvider> entityProviders = new HashMap<>();
    private final HashMap<String, EntityConfig> entities = new HashMap<>();

    public BukkitEntityManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.registerEntityProvider(new EntityProvider() {
            @Override
            public String identifier() {
                return "vanilla";
            }
            @NotNull
            @Override
            public Entity spawn(@NotNull Location location, @NotNull String id, @NotNull Map<String, Object> propertyMap) {
                return location.getWorld().spawnEntity(location, EntityType.valueOf(id.toUpperCase(Locale.ENGLISH)));
            }
        });
    }

    @Override
    public void unload() {
        this.entities.clear();
    }

    @Override
    public void disable() {
        unload();
        this.entityProviders.clear();
    }

    @Override
    public Optional<EntityConfig> getEntity(String id) {
        return Optional.ofNullable(this.entities.get(id));
    }

    @Override
    public boolean registerEntity(EntityConfig entity) {
        if (entities.containsKey(entity.id())) return false;
        this.entities.put(entity.id(), entity);
        return true;
    }

    public boolean registerEntityProvider(EntityProvider entityProvider) {
        if (entityProviders.containsKey(entityProvider.identifier())) return false;
        else entityProviders.put(entityProvider.identifier(), entityProvider);
        return true;
    }

    public boolean unregisterEntityProvider(String id) {
        return entityProviders.remove(id) != null;
    }

    @Nullable
    @Override
    public Entity summonEntityLoot(Context<Player> context) {
        String id = context.arg(ContextKeys.ID);
        EntityConfig config = requireNonNull(entities.get(id), "Entity " + id + " not found");
        Location hookLocation = requireNonNull(context.arg(ContextKeys.HOOK_LOCATION));
        Location playerLocation = requireNonNull(context.getHolder().getLocation());
        String entityID = config.entityID();
        Entity entity;
        if (entityID.contains(":")) {
            String[] split = entityID.split(":", 2);
            EntityProvider provider = requireNonNull(entityProviders.get(split[0]), "EntityProvider " + split[0] + " doesn't exist");
            entity = requireNonNull(provider.spawn(hookLocation, split[1], config.propertyMap()), "Entity " + entityID + " doesn't exist");
        } else {
            entity = entityProviders.get("vanilla").spawn(hookLocation, entityID, config.propertyMap());
        }
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply(config.horizontalVector().evaluate(context) - 1);
        vector = vector.setY((vector.getY() + 0.2) * config.verticalVector().evaluate(context));
        entity.setVelocity(vector);
        return entity;
    }
}
