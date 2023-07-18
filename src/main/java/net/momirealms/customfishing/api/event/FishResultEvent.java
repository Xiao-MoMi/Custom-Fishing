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

import net.momirealms.customfishing.fishing.FishResult;
import net.momirealms.customfishing.fishing.loot.Loot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishResultEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled;
    private boolean isDouble;
    private final FishResult result;
    private final ItemStack itemStack;
    private final String loot_id;
    private final Loot loot;
    private static final HandlerList handlerList = new HandlerList();

    public FishResultEvent(@NotNull Player who, FishResult result, boolean isDouble, @Nullable ItemStack itemStack, @Nullable String loot_id, @Nullable Loot loot) {
        super(who);
        this.cancelled = false;
        this.result = result;
        this.isDouble = isDouble;
        this.itemStack = itemStack;
        this.loot_id = loot_id;
        this.loot = loot;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public boolean isDouble() {
        return isDouble;
    }

    @NotNull
    public FishResult getResult() {
        return result;
    }

    /**
     * Would be null if failed or caught a mob
     * @return loot id
     */
    @Nullable
    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setDouble(boolean willDouble) {
        isDouble = willDouble;
    }

    @Nullable
    @Deprecated
    public String getLoot_id() {
        return loot_id;
    }

    /**
     * Would be null if failed
     * @return loot id (Vanilla loots would be "vanilla")
     */
    @Nullable
    public String getLootID() {
        return loot_id;
    }

    public Loot getLoot() {
        return loot;
    }
}
