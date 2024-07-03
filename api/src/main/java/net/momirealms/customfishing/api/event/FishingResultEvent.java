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

public class FishingResultEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private boolean isCancelled;
    private final Result result;
    private final Loot loot;
    private final FishHook fishHook;
    private Context<Player> context;

    public FishingResultEvent(@NotNull Context<Player> context, Result result, FishHook fishHook, Loot loot) {
        super(context.getHolder());
        this.result = result;
        this.loot = loot;
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

    public Result getResult() {
        return result;
    }

    public FishHook getFishHook() {
        return fishHook;
    }

    public Loot getLoot() {
        return loot;
    }

    public void setScore(double score) {
        context.arg(ContextKeys.CUSTOM_SCORE, score);
    }

    public Context<Player> getContext() {
        return context;
    }

    public int getAmount() {
        if (result == Result.FAILURE) return 0;
        return Optional.ofNullable(context.arg(ContextKeys.AMOUNT)).orElse(1);
    }

    public enum Result {
        SUCCESS,
        FAILURE
    }
}
