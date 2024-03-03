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
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.UUID;

public interface BagManager {

    /**
     * Is bag enabled
     *
     * @return enabled or not
     */
    boolean isEnabled();

    /**
     * Retrieves the online bag inventory associated with a player's UUID.
     *
     * @param uuid The UUID of the player for whom the bag inventory is retrieved.
     * @return The online bag inventory if the player is online, or null if not found.
     */
    Inventory getOnlineBagInventory(UUID uuid);

    /**
     * Get the rows of a player's fishing bag
     *
     * @param player player who owns the bag
     * @return rows
     */
    int getBagInventoryRows(Player player);

    /**
     * Initiates the process of editing the bag inventory of an offline player by an admin.
     *
     * @param admin    The admin player performing the edit.
     * @param userData The OfflineUser data of the player whose bag is being edited.
     */
    void editOfflinePlayerBag(Player admin, OfflineUser userData);

    /**
     * Get the actions to perform when loot is automatically collected
     *
     * @return actions
     */
    Action[] getCollectLootActions();

    /**
     * Get the actions to perform when bag is full
     *
     * @return actions
     */
    Action[] getBagFullActions();

    /**
     * If bag can store fishing loots
     *
     * @return can store or not
     */
    boolean doesBagStoreLoots();

    /**
     * Get the fishing bag's title
     *
     * @return title
     */
    String getBagTitle();

    /**
     * Get a list of allowed items in bag
     *
     * @return whitelisted items
     */
    List<Material> getBagWhiteListItems();

    /**
     * Get the requirements for automatically collecting loots
     *
     * @return requirements
     */
    Requirement[] getCollectRequirements();
}
