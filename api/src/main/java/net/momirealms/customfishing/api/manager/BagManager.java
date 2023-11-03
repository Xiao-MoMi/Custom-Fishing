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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.data.user.OfflineUser;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface BagManager {

    boolean isEnabled();

    /**
     * Retrieves the online bag inventory associated with a player's UUID.
     *
     * @param uuid The UUID of the player for whom the bag inventory is retrieved.
     * @return The online bag inventory if the player is online, or null if not found.
     */
    Inventory getOnlineBagInventory(UUID uuid);

    int getBagInventoryRows(Player player);

    /**
     * Initiates the process of editing the bag inventory of an offline player by an admin.
     *
     * @param admin    The admin player performing the edit.
     * @param userData The OfflineUser data of the player whose bag is being edited.
     */
    void editOfflinePlayerBag(Player admin, OfflineUser userData);
}
