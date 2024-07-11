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

import net.momirealms.customfishing.api.mechanic.fishing.FishingGears;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that occurs when a player casts a fishing rod.
 */
public class RodCastEvent extends PlayerEvent implements Cancellable {

    private final FishingGears gears;
    private boolean isCancelled;
    private final PlayerFishEvent event;
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Constructs a new RodCastEvent.
     *
     * @param event The original PlayerFishEvent that triggered this event
     * @param gears The fishing gears used by the player
     */
    public RodCastEvent(PlayerFishEvent event, FishingGears gears) {
        super(event.getPlayer());
        this.gears = gears;
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Cancelling this event would disable CustomFishing mechanics
     * If you want to prevent players from casting, cancel {@link #getBukkitPlayerFishEvent()} too
     *
     * @param cancel true if you want to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Get the {@link FishingGears}
     *
     * @return fishing gears
     */
    public FishingGears getGears() {
        return gears;
    }

    /**
     * Gets the original {@link PlayerFishEvent} that triggered the {@link RodCastEvent}.
     *
     * @return The original PlayerFishEvent.
     */
    public PlayerFishEvent getBukkitPlayerFishEvent() {
        return event;
    }
}
