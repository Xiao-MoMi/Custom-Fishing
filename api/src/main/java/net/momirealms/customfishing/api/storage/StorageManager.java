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
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageManager extends Reloadable {

    @NotNull
    String getServerID();

    @NotNull
    Optional<UserData> getOnlineUser(UUID uuid);

    @NotNull
    Collection<UserData> getOnlineUsers();

    CompletableFuture<Optional<UserData>> getOfflineUserData(UUID uuid, boolean lock);

    CompletableFuture<Boolean> saveUserData(UserData userData, boolean unlock);

    @NotNull
    DataStorageProvider getDataSource();

    boolean isRedisEnabled();

    /**
     * Converts PlayerData to bytes.
     *
     * @param data The PlayerData to be converted.
     * @return The byte array representation of PlayerData.
     */
    byte[] toBytes(@NotNull PlayerData data);

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
