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

import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * This class represents an event that occurs when a player gets a result from fishing.
 */
public class FishingResultEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private boolean isCancelled;
    private final Result result;
    private final Loot loot;
    private final FishHook fishHook;
    private final Map<String, String> args;

    /**
     * Constructs a new FishingResultEvent.
     *
     * @param who    The player who triggered the event.
     * @param result The result of the fishing action (SUCCESS or FAILURE).
     * @param loot   The loot received from fishing.
     * @param args   A map of placeholders and their corresponding values.
     */
    public FishingResultEvent(@NotNull Player who, Result result, FishHook fishHook, Loot loot, Map<String, String> args) {
        super(who);
        this.result = result;
        this.loot = loot;
        this.args = args;
        this.fishHook = fishHook;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
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
     * Gets the value associated with a specific argument key.
     * Usage example event.getArg("{x}")
     *
     * @param key The argument key enclosed in curly braces, e.g., "{amount}".
     * @return The value associated with the argument key, or null if not found.
     */
    public String getArg(String key) {
        return args.get(key);
    }

    /**
     * Set the value associated with a specific argument key.
     * @param key key
     * @param value value
     * @return previous value
     */
    @Nullable
    public String setArg(String key, String value) {
        return args.put(key, value);
    }

    /**
     * Gets the result of the fishing action.
     *
     * @return The fishing result, which can be either SUCCESS or FAILURE.
     */
    public Result getResult() {
        return result;
    }

    /**
     * Get the fish hook entity.
     *
     * @return fish hook
     */
    public FishHook getFishHook() {
        return fishHook;
    }

    /**
     * Gets the loot received from fishing.
     *
     * @return The loot obtained from the fishing action.
     */
    public Loot getLoot() {
        return loot;
    }

    /**
     * Gets the amount of loot received.
     * This value is determined by the "multiple-loot" effect.
     * If you want to get the amount of item spawned, listen to FishingLootSpawnEvent
     *
     * @return The amount of loot received, or 1 if the loot is block or entity
     */
    public int getAmount() {
        return Integer.parseInt(Optional.ofNullable(getArg("{amount}")).orElse("1"));
    }

    /**
     * Set the loot amount (Only works for items)
     *
     * @param amount amount
     */
    public void setAmount(int amount) {
        setArg("{amount}", String.valueOf(amount));
    }

    /**
     * Set the score to get in competition
     *
     * @param score score
     */
    public void setScore(double score) {
        setArg("{SCORE}", String.valueOf(score));
    }

    /**
     * An enumeration representing possible fishing results (SUCCESS or FAILURE).
     */
    public enum Result {
        SUCCESS,
        FAILURE
    }
}
