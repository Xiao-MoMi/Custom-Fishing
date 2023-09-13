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

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageManager {

    /**
     * Get server unique id
     * @return id
     */
    String getUniqueID();

    /**
     * Get online user's data
     * @param uuid uuid
     * @return online user data
     */
    OnlineUser getOnlineUser(UUID uuid);

    /**
     * Get an offline user's data
     * Otherwise it would return Optional.empty() if data is locked
     * It an offline user never played the server, its name would equal "" (empty string)
     * @param uuid uuid
     * @param lock lock
     * @return offline user data
     */
    CompletableFuture<Optional<OfflineUser>> getOfflineUser(UUID uuid, boolean lock);

    CompletableFuture<Boolean> saveUserData(OfflineUser offlineUser, boolean unlock);

    /**
     * Get all the players in servers that connected to the same redis server
     * @return amount
     */
    CompletableFuture<Integer> getRedisPlayerCount();

    /**
     * Get plugin data source
     * @return data source
     */
    DataStorageInterface getDataSource();

    boolean isRedisEnabled();

    byte[] toBytes(@NotNull PlayerData data);

    @NotNull String toJson(@NotNull PlayerData data);

    PlayerData fromJson(String json);

    @NotNull PlayerData fromBytes(byte[] data);
}
