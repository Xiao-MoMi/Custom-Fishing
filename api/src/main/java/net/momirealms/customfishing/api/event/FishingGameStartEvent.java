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

import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class FishingGameStartEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final GamingPlayer gamingPlayer;
    private final CustomFishingHook hook;

    public FishingGameStartEvent(@NotNull CustomFishingHook hook, GamingPlayer gamingPlayer) {
        super(hook.getContext().holder());
        this.gamingPlayer = gamingPlayer;
        this.hook = hook;
    }

    public CustomFishingHook hook() {
        return hook;
    }

    public GamingPlayer gamingPlayer() {
        return gamingPlayer;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
