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

import net.momirealms.customfishing.api.data.DataStorageInterface;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.user.OfflineUser;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageManager {

    /**
     * Gets the unique server identifier.
     *
     * @return The unique server identifier.
     */
    @NotNull String getUniqueID();

    /**
     * Gets an OnlineUser instance for the specified UUID.
     *
     * @param uuid The UUID of the player.
     * @return An OnlineUser instance if the player is online, or null if not.
     */
    @Nullable OnlineUser getOnlineUser(UUID uuid);

    /**
     * Get all the online users
     *
     * @return online users
     */
    Collection<OnlineUser> getOnlineUsers();

    /**
     * Asynchronously retrieves an OfflineUser instance for the specified UUID.
     * The offline user might be a locked one with no data, use isLockedData() method
     * to check if it's an empty locked data
     *
     * @param uuid The UUID of the player.
     * @param lock Whether to lock the data during retrieval.
     * @return A CompletableFuture that resolves to an Optional containing the OfflineUser instance if found, or empty if not found or locked.
     */
    CompletableFuture<Optional<OfflineUser>> getOfflineUser(UUID uuid, boolean lock);

    /**
     * If the offlineUser is locked with no data in it
     * An user's data would be locked if he is playing on another server that connected
     * to database. Modifying this data would actually do nothing.
     *
     * @param offlineUser offlineUser
     * @return is locked or not
     */
    boolean isLockedData(OfflineUser offlineUser);

    /**
     * Asynchronously saves user data for an OfflineUser.
     *
     * @param offlineUser The OfflineUser whose data needs to be saved.
     * @param unlock Whether to unlock the data after saving.
     * @return A CompletableFuture that resolves to a boolean indicating the success of the data saving operation.
     */
    CompletableFuture<Boolean> saveUserData(OfflineUser offlineUser, boolean unlock);

    /**
     * Gets the data source used for data storage.
     *
     * @return The data source.
     */
    DataStorageInterface getDataSource();

    /**
     * Checks if Redis is enabled.
     *
     * @return True if Redis is enabled; otherwise, false.
     */
    boolean isRedisEnabled();

    /**
     * Converts PlayerData to bytes.
     *
     * @param data The PlayerData to be converted.
     * @return The byte array representation of PlayerData.
     */
    byte @NotNull [] toBytes(@NotNull PlayerData data);

    /**
     * Converts PlayerData to JSON format.
     *
     * @param data The PlayerData to be converted.
     * @return The JSON string representation of PlayerData.
     */
    @NotNull String toJson(@NotNull PlayerData data);

    /**
     * Converts JSON string to PlayerData.
     *
     * @param json The JSON string to be converted.
     * @return The PlayerData object.
     */
    @NotNull PlayerData fromJson(String json);

    /**
     * Converts bytes to PlayerData.
     *
     * @param data The byte array to be converted.
     * @return The PlayerData object.
     */
    @NotNull PlayerData fromBytes(byte[] data);
}
