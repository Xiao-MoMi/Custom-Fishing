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
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This class represents an event that is triggered when a fishing result is determined.
 */
public class FishingResultEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private boolean isCancelled;
    private final Result result;
    private final Loot loot;
    private final FishHook fishHook;
    private final Context<Player> context;

    /**
     * Constructs a new FishingResultEvent.
     *
     * @param context The context in which the fishing result occurs
     * @param result The result of the fishing action
     * @param fishHook The fish hook involved
     * @param loot The loot involved
     */
    public FishingResultEvent(@NotNull Context<Player> context, Result result, FishHook fishHook, Loot loot) {
        super(context.holder());
        this.context = context;
        this.result = result;
        this.loot = loot;
        this.fishHook = fishHook;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    /**
     * Gets the {@link Result} of the fishing action.
     *
     * @return The result of the fishing action
     */
    public Result getResult() {
        return result;
    }

    /**
     * Gets the {@link FishHook} involved.
     *
     * @return The fish hook
     */
    public FishHook getFishHook() {
        return fishHook;
    }

    /**
     * Gets the {@link Loot} obtained from the fishing.
     *
     * @return The loot
     */
    public Loot getLoot() {
        return loot;
    }

    /**
     * Sets the custom score for the fishing action.
     *
     * @param score The custom score to set
     */
    public void setScore(double score) {
        context.arg(ContextKeys.CUSTOM_SCORE, score);
    }

    /**
     * Gets the {@link Context<Player>}
     *
     * @return The context
     */
    public Context<Player> getContext() {
        return context;
    }

    /**
     * Gets the amount of loot obtained from the fishing action.
     * If the result is a failure, the amount is 0.
     *
     * @return The amount of loot obtained
     */
    public int getAmount() {
        if (result == Result.FAILURE) return 0;
        return Optional.ofNullable(context.arg(ContextKeys.AMOUNT)).orElse(1);
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum Result {
        SUCCESS,
        FAILURE
    }
}
