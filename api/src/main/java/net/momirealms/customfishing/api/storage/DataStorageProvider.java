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

import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.user.UserData;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface DataStorageProvider {

    void initialize(YamlDocument config);

    void disable();

    StorageType getStorageType();

    CompletableFuture<Optional<PlayerData>> getPlayerData(UUID uuid, boolean lock);

    CompletableFuture<Boolean> updatePlayerData(UUID uuid, PlayerData playerData, boolean unlock);

    CompletableFuture<Boolean> updateOrInsertPlayerData(UUID uuid, PlayerData playerData, boolean unlock);

    void updateManyPlayersData(Collection<? extends UserData> users, boolean unlock);

    void lockOrUnlockPlayerData(UUID uuid, boolean lock);

    Set<UUID> getUniqueUsers();
}
