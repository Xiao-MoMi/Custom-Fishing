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

package net.momirealms.customfishing.api.storage.data;

import com.google.gson.annotations.SerializedName;

/**
 * The InventoryData class holds data related to a player's fishing bag.
 * It includes a serialized representation of the inventory and the size of the inventory.
 */
public class InventoryData {

    @SerializedName("inventory")
    public String serialized;
    @SerializedName("size")
    public int size;

    /**
     * Creates an instance of InventoryData with default values (empty inventory and size of 9).
     *
     * @return a new instance of InventoryData with default values.
     */
    public static InventoryData empty() {
        return new InventoryData("", 9);
    }

    /**
     * Constructs a new InventoryData instance with specified serialized inventory and size.
     *
     * @param serialized the serialized representation of the inventory.
     * @param size the size of the inventory.
     */
    public InventoryData(String serialized, int size) {
        this.serialized = serialized;
        this.size = size;
    }
}
