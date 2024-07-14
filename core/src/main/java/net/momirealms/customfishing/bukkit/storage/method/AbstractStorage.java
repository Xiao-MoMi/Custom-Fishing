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

package net.momirealms.customfishing.bukkit.storage.method;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.storage.DataStorageProvider;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import net.momirealms.customfishing.api.storage.user.UserData;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * An abstract class that implements the DataStorageInterface and provides common functionality for data storage.
 */
public abstract class AbstractStorage implements DataStorageProvider {

    protected BukkitCustomFishingPlugin plugin;

    public AbstractStorage(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize(YamlDocument config) {
        // This method can be overridden in subclasses to perform initialization tasks specific to the storage type.
    }

    @Override
    public void disable() {
        // This method can be overridden in subclasses to perform cleanup or shutdown tasks specific to the storage type.
    }

    /**
     * Get the current time in seconds since the Unix epoch.
     *
     * @return The current time in seconds.
     */
    public int getCurrentSeconds() {
        return (int) Instant.now().getEpochSecond();
    }

    @Override
    public void updateManyPlayersData(Collection<? extends UserData> users, boolean unlock) {
        for (UserData user : users) {
            this.updatePlayerData(user.uuid(), user.toPlayerData(), unlock);
        }
    }

    public void lockOrUnlockPlayerData(UUID uuid, boolean lock) {
        // Note: Only remote database would override this method
    }

    @Override
    public CompletableFuture<Boolean> updateOrInsertPlayerData(UUID uuid, PlayerData playerData, boolean unlock) {
        // By default, delegate to the updatePlayerData method to update or insert player data.
        return updatePlayerData(uuid, playerData, unlock);
    }
}
