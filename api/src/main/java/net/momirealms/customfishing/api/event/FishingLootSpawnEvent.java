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

package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents an event that is triggered when fishing loot is spawned.
 */
public class FishingLootSpawnEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();
    private final Location location;
    private final Entity entity;
    private final Loot loot;
    private final Context<Player> context;
    private boolean skipActions;
    private boolean summonEntity;

    /**
     * Constructs a new FishingLootSpawnEvent.
     *
     * @param context The context in which the loot is spawned
     * @param location The location where the loot is spawned
     * @param loot The loot that is being spawned
     * @param entity The entity associated with the loot, if any
     */
    public FishingLootSpawnEvent(@NotNull Context<Player> context, Location location, Loot loot, @Nullable Entity entity) {
        super(context.holder());
        this.entity = entity;
        this.loot = loot;
        this.location = location;
        this.skipActions = false;
        this.summonEntity = true;
        this.context = context;
    }

    /**
     * Gets the {@link Context<Player>} in which the loot is spawned.
     *
     * @return The context
     */
    public Context<Player> getContext() {
        return context;
    }

    /**
     * Gets the {@link Location} where the loot is spawned.
     *
     * @return The location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the {@link Entity} associated with the loot, if any.
     *
     * @return The entity, or null if the item is `AIR`
     */
    @Nullable
    public Entity getEntity() {
        return entity;
    }

    /**
     * Gets the {@link Loot} that is being spawned.
     *
     * @return The loot
     */
    @NotNull
    public Loot getLoot() {
        return loot;
    }

    /**
     * Checks if the entity should be summoned.
     *
     * @return True if the entity should be summoned, otherwise false
     */
    public boolean summonEntity() {
        return summonEntity;
    }

    /**
     * Sets whether the entity should be summoned.
     *
     * @param summonEntity True to summon the entity, otherwise false
     */
    public void summonEntity(boolean summonEntity) {
        this.summonEntity = summonEntity;
    }

    /**
     * Checks if actions related to the loot should be skipped.
     *
     * @return True if actions should be skipped, otherwise false
     */
    public boolean skipActions() {
        return skipActions;
    }

    /**
     * Sets whether actions related to the loot should be skipped.
     *
     * @param skipActions True to skip actions, otherwise false
     */
    public void skipActions(boolean skipActions) {
        this.skipActions = skipActions;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
