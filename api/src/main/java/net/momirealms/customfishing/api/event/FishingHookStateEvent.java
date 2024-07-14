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

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that occurs when the state of a fishing hook changes.
 * It is triggered by various states of the fishing hook such as when a fish bites, escapes, is lured, or is landed.
 */
public class FishingHookStateEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();
    private final FishHook fishHook;
    private final State state;

    /**
     * Constructs a new FishingHookStateEvent.
     *
     * @param who The player associated with this event
     * @param hook The fishing hook involved in this event
     * @param state The state of the fishing hook
     */
    public FishingHookStateEvent(@NotNull Player who, FishHook hook, State state) {
        super(who);
        this.fishHook = hook;
        this.state = state;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Gets the {@link FishHook} involved in this event.
     *
     * @return The FishHook involved in this event
     */
    public FishHook getFishHook() {
        return fishHook;
    }

    /**
     * Gets the {@link State} of the fishing hook.
     *
     * @return The state of the fishing hook
     */
    public State getState() {
        return state;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum State {
        BITE,
        ESCAPE,
        LURE,
        LAND
    }
}
