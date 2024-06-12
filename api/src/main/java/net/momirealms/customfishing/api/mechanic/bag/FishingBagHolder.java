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

package net.momirealms.customfishing.api.mechanic.bag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FishingBagHolder implements InventoryHolder {

    private final UUID owner;
    private Inventory inventory;

    public FishingBagHolder(UUID owner) {
        this.owner = owner;
    }

    @Override
    public @NotNull Inventory getInventory() {
        Player player = Bukkit.getPlayer(owner);
        if (player != null) {
            int rows = BagManager.getBagInventoryRows(player);
            if (rows * 9 != inventory.getSize()) {
                Inventory newBag = Bukkit.createInventory(this, rows * 9);
                ItemStack[] newContents = new ItemStack[rows * 9];
                ItemStack[] oldContents = inventory.getContents();
                for (int i = 0; i < rows * 9 && i < oldContents.length; i++) {
                    newContents[i] = oldContents[i];
                }
                newBag.setContents(newContents);
                this.setInventory(newBag);
            }
        }
        return inventory;
    }

    public void setItems(ItemStack[] itemStacks) {
        this.inventory.setContents(itemStacks);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public static FishingBagHolder create(UUID owner, ItemStack[] itemStacks, int size) {
        FishingBagHolder holder = new FishingBagHolder(owner);
        Inventory inventory = Bukkit.createInventory(holder, size);
        holder.setInventory(inventory);
        holder.setItems(itemStacks);
        return holder;
    }
}