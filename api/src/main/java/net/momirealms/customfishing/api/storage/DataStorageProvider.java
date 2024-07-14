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

package net.momirealms.customfishing.api.storage;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.user.UserData;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface representing a provider for data storage.
 */
public interface DataStorageProvider {

    /**
     * Initializes the data storage provider with the given configuration.
     *
     * @param config the {@link YamlDocument} configuration for the storage provider
     */
    void initialize(YamlDocument config);

    /**
     * Disables the data storage provider, performing any necessary cleanup.
     */
    void disable();

    /**
     * Retrieves the type of storage used by this provider.
     *
     * @return the {@link StorageType} of this provider
     */
    StorageType getStorageType();

    /**
     * Retrieves the player data for the specified UUID.
     *
     * @param uuid the UUID of the player
     * @param lock whether to lock the player data for exclusive access
     * @return a {@link CompletableFuture} containing an {@link Optional} with the player data, or empty if not found
     */
    CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock);

    /**
     * Updates the player data for the specified UUID.
     *
     * @param uuid       the UUID of the player
     * @param playerData the {@link PlayerData} to be updated
     * @param unlock     whether to unlock the player data after updating
     * @return a {@link CompletableFuture} containing a boolean indicating success or failure
     */
    CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean unlock);

    /**
     * Updates or inserts the player data for the specified UUID.
     *
     * @param uuid       the UUID of the player
     * @param playerData the {@link PlayerData} to be updated or inserted
     * @param unlock     whether to unlock the player data after updating or inserting
     * @return a {@link CompletableFuture} containing a boolean indicating success or failure
     */
    CompletableFuture<Boolean> updateOrInsertPlayerData(UUID uuid, PlayerData playerData, boolean unlock);

    /**
     * Updates the data for multiple players.
     *
     * @param users  a collection of {@link UserData} to be updated
     * @param unlock whether to unlock the player data after updating
     */
    void updateManyPlayersData(Collection<? extends UserData> users, boolean unlock);

    /**
     * Locks or unlocks the player data for the specified UUID.
     *
     * @param uuid  the UUID of the player
     * @param lock  whether to lock (true) or unlock (false) the player data
     */
    void lockOrUnlockPlayerData(UUID uuid, boolean lock);

    /**
     * Retrieves the set of unique user UUIDs.
     *
     * @return a set of unique user UUIDs
     */
    Set<UUID> getUniqueUsers();
}
