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

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that occurs when a player casts a fishing rod.
 */
public class RodCastEvent extends PlayerEvent implements Cancellable {

    private final Effect effect;
    private boolean isCancelled;
    private final PlayerFishEvent event;
    private final FishingPreparation preparation;
    private static final HandlerList handlerList = new HandlerList();

    /**
     * Constructs a new RodCastEvent.
     *
     * @param event              The original PlayerFishEvent that triggered the rod cast.
     * @param fishingPreparation The fishing preparation associated with the rod cast.
     * @param effect             The effect associated with the fishing rod cast.
     */
    public RodCastEvent(PlayerFishEvent event, FishingPreparation fishingPreparation, Effect effect) {
        super(event.getPlayer());
        this.effect = effect;
        this.event = event;
        this.preparation = fishingPreparation;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    /**
     * Cancelling this event would not cancel the bukkit PlayerFishEvent
     *
     * @param cancel true if you wish to cancel this event
     */
    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Gets the fishing preparation associated with the rod cast.
     *
     * @return The FishingPreparation associated with the rod cast.
     */
    public FishingPreparation getPreparation() {
        return preparation;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    /**
     * Gets the effect associated with the fishing rod cast.
     *
     * @return The Effect associated with the rod cast.
     */
    public Effect getEffect() {
        return effect;
    }

    /**
     * Gets the original PlayerFishEvent that triggered the rod cast.
     *
     * @return The original PlayerFishEvent.
     */
    public PlayerFishEvent getBukkitPlayerFishEvent() {
        return event;
    }
}
