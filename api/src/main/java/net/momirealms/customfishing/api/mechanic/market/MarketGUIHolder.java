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

package net.momirealms.customfishing.api.mechanic.market;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a holder for the Market GUI inventory.
 * This class is used to associate the Market GUI's inventory with an object.
 */
public class MarketGUIHolder implements InventoryHolder {

    private Inventory inventory;

    /**
     * Sets the inventory associated with this holder.
     *
     * @param inventory The inventory to associate with this holder.
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Retrieves the inventory associated with this holder.
     *
     * @return The associated inventory.
     */
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}