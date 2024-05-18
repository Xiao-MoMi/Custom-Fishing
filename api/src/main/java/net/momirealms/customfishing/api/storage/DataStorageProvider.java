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

package net.momirealms.customfishing.api.storage;

import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.user.UserData;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataStorageProvider {

    /**
     * Initialize the data resource
     */
    void initialize();

    /**
     * Close the data resource
     */
    void disable();

    /**
     * Get the storage data source type
     *
     * @return {@link StorageType}
     */
    StorageType getStorageType();

    /**
     * Retrieve a player's data
     *
     * @param uuid The UUID of the player.
     * @param lock Whether to lock the player data during retrieval.
     * @return A CompletableFuture containing the optional player data.
     */
    CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock);

    /**
     * Update a player's data
     *
     * @param uuid      The UUID of the player.
     * @param playerData The player data to update.
     * @param unlock    Whether to unlock the player data after updating.
     * @return A CompletableFuture indicating the success of the update.
     */
    CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean unlock);

    /**
     * Update or insert a player's data into the SQL database.
     *
     * @param uuid      The UUID of the player.
     * @param playerData The player data to update or insert.
     * @param unlock    Whether to unlock the player data after updating or inserting.
     * @return A CompletableFuture indicating the success of the operation.
     */
    CompletableFuture<Boolean> updateOrInsertPlayerData(UUID uuid, PlayerData playerData, boolean unlock);

    /**
     * Update data for multiple players
     *
     * @param users  A collection of OfflineUser objects representing players.
     * @param unlock Whether to unlock the player data after updating.
     */
    void updateManyPlayersData(Collection<? extends UserData<OfflinePlayer>> users, boolean unlock);

    /**
     * Lock or unlock a player's data in the SQL database.
     *
     * @param uuid The UUID of the player.
     * @param lock Whether to lock or unlock the player data.
     */
    void lockOrUnlockPlayerData(UUID uuid, boolean lock);

    /**
     * Get a set of unique user UUIDs
     *
     * @return A set of unique user UUIDs.
     */
    Set<UUID> getUniqueUsers();
}
