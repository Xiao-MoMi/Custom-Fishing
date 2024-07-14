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

package net.momirealms.customfishing.api.mechanic.bag;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The BagManager interface defines methods for managing fishing bags.
 */
public interface BagManager extends Reloadable {

    /**
     * Retrieves the number of inventory rows for a player's fishing bag based on their permissions.
     * The size is determined by the highest permission the player has from "fishingbag.rows.1" to "fishingbag.rows.6".
     * If no specific permission is found, the default size is 1 row.
     *
     * @param player the player whose fishing bag size is being retrieved
     * @return the number of inventory rows for the player's fishing bag, based on their permissions.
     */
    static int getBagInventoryRows(Player player) {
        int size = 1;
        for (int i = 6; i > 1; i--) {
            if (player.hasPermission("fishingbag.rows." + i)) {
                size = i;
                break;
            }
        }
        return size;
    }

    /**
     * Opens the fishing bag of a specified owner for a viewer player asynchronously.
     * The method returns a {@link CompletableFuture} that completes with a boolean indicating
     * whether the bag was successfully opened.
     *
     * @param viewer the player who will view the fishing bag
     * @param owner  the UUID of the player who owns the fishing bag
     * @return a {@link CompletableFuture} that completes with {@code true} if the bag was successfully opened,
     *         or {@code false} otherwise.
     */
    CompletableFuture<Boolean> openBag(@NotNull Player viewer, @NotNull UUID owner);
}
