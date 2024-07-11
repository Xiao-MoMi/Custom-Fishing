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

import net.momirealms.customfishing.api.mechanic.totem.TotemConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that occurs when a player activates a totem.
 */
public class TotemActivateEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Location coreLocation;
    private boolean isCancelled;
    private final TotemConfig config;

    /**
     * Constructs a new TotemActivateEvent.
     *
     * @param who       The player who activated the totem.
     * @param location  The location of the totem's core.
     * @param config    The configuration of the totem being activated.
     */
    public TotemActivateEvent(@NotNull Player who, Location location, TotemConfig config) {
        super(who);
        this.coreLocation = location;
        this.config = config;
    }

    /**
     * Gets the location of the totem's core.
     *
     * @return The location of the totem's core.
     */
    public Location getCoreLocation() {
        return coreLocation;
    }

    /**
     * Gets the {@link TotemConfig} of the totem being activated.
     *
     * @return The TotemConfig of the totem being activated.
     */
    public TotemConfig getConfig() {
        return config;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
