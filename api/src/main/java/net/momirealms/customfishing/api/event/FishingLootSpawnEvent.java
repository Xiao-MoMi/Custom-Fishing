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

public class FishingLootSpawnEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();
    private final Location location;
    private final Entity entity;
    private final Loot loot;
    private final Context<Player> context;
    private boolean skipActions;
    private boolean summonEntity;

    public FishingLootSpawnEvent(@NotNull Context<Player> context, Location location, Loot loot, @Nullable Entity entity) {
        super(context.getHolder());
        this.entity = entity;
        this.loot = loot;
        this.location = location;
        this.skipActions = false;
        this.summonEntity = true;
        this.context = context;
    }

    public Context<Player> getContext() {
        return context;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Get the loot entity
     *
     * @return entity
     */
    @Nullable
    public Entity getEntity() {
        return entity;
    }

    @NotNull
    public Loot getLoot() {
        return loot;
    }

    public boolean summonEntity() {
        return summonEntity;
    }

    public void summonEntity(boolean summonEntity) {
        this.summonEntity = summonEntity;
    }

    public boolean skipActions() {
        return skipActions;
    }

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
