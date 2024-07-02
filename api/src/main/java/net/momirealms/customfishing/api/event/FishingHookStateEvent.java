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

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class FishingHookStateEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();
    private final FishHook fishHook;
    private final State state;

    public FishingHookStateEvent(@NotNull Player who, FishHook hook, State state) {
        super(who);
        this.fishHook = hook;
        this.state = state;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public FishHook getFishHook() {
        return fishHook;
    }

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
