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

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MarketSellEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlerList = new HandlerList();
    private final List<ItemStack> items;
    private final List<Double> prices;
    private boolean cancelled;

    public MarketSellEvent(Player player, @NotNull List<ItemStack> items, @NotNull List<Double> prices) {
        super(player);
        this.items = List.copyOf(items);
        this.prices = List.copyOf(prices);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public List<ItemStack> getItems() {
        return this.items;
    }

    public List<Double> getPrices() {
        return this.prices;
    }

    public int itemSize() {
        return this.items.size();
    }

    public double getItemPrice(int index) {
        return this.prices.get(index);
    }

    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }
}
