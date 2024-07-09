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

package net.momirealms.customfishing.api.mechanic.entity;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * EntityManager interface for managing custom entities in the fishing plugin.
 */
public interface EntityManager extends Reloadable {

    /**
     * Retrieves the configuration for a custom entity by its identifier.
     *
     * @param id The unique identifier of the entity configuration.
     * @return An Optional containing the EntityConfig if found, or an empty Optional if not found.
     */
    Optional<EntityConfig> getEntity(String id);

    boolean registerEntity(EntityConfig entity);

    @NotNull
    Entity summonEntityLoot(Context<Player> context);
}