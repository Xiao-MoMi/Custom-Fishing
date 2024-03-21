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
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This class represents an event that occurs when a fishing hook lands in either lava or water.
 */
public class FishHookLandEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();
    private final Target target;
    private final FishHook fishHook;
    private final Effect effect;
    private boolean isFirst;

    /**
     * Constructs a new FishHookLandEvent.
     *
     * @param who           The player who triggered the event.
     * @param target        The target where the fishing hook has landed (LAVA or WATER).
     * @param hook          The fishing hook entity.
     * @param initialEffect The initial effect
     */
    public FishHookLandEvent(@NotNull Player who, Target target, FishHook hook, boolean isFirst, Effect initialEffect) {
        super(who);
        this.target = target;
        this.fishHook = hook;
        this.effect = initialEffect;
        this.isFirst = isFirst;
    }

    /**
     * Gets the target where the fishing hook has landed.
     *
     * @return The target, which can be either LAVA or WATER.
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Gets the fish hook bukkit entity
     *
     * @return fish hook
     */
    public FishHook getFishHook() {
        return fishHook;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    /**
     * Is the first try of one fishing catch
     *
     * @return is first try
     */
    public boolean isFirst() {
        return isFirst;
    }

    /**
     * Get the fishing effect
     * It's not advised to modify this value without checking "isFirst()" since this event can be trigger multiple times in one fishing catch
     *
     * @return fishing effect
     */
    public Effect getEffect() {
        return effect;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum Target {
        LAVA,
        WATER
    }
}
