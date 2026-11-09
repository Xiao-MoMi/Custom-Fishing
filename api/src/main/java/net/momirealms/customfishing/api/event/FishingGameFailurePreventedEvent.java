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

/**
 * Fired on the hook's region thread once for each prevented failure (including a timeout).
 * The failed attempt has been replaced with a fresh game and a new deadline.
 * This notification cannot be cancelled.
 */
public class FishingGameFailurePreventedEvent extends PlayerEvent {
    private static final HandlerList handlerList = new HandlerList();
    private final GamingPlayer gamingPlayer;
    private final CustomFishingHook hook;
    private final int preventedFailures;

    public FishingGameFailurePreventedEvent(@NotNull CustomFishingHook hook, GamingPlayer gamingPlayer, int preventedFailures) {
        super(hook.getContext().holder());
        this.gamingPlayer = gamingPlayer;
        this.hook = hook;
        this.preventedFailures = preventedFailures;
    }

    public CustomFishingHook hook() {
        return hook;
    }

    /** @return the failed attempt; the hook holds the new game instance */
    public GamingPlayer gamingPlayer() {
        return gamingPlayer;
    }

    /** @return the number of failures prevented so far, including this one */
    public int preventedFailures() {
        return preventedFailures;
    }

    /** @return the number of additional failures that can still be prevented */
    public int remainingFailures() {
        return gamingPlayer.settings().maxFailures() - 1 - preventedFailures;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
