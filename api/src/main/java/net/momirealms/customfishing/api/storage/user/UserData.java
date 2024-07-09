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

package net.momirealms.customfishing.api.storage.user;

import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.statistic.FishingStatistics;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.data.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface UserData {

    /**
     * Get the username
     *
     * @return user name
     */
    @NotNull
    String name();

    /**
     * Get the user's uuid
     *
     * @return uuid
     */
    @NotNull
    UUID uuid();

    /**
     * Get the player instance if that player is online
     *
     * @return player
     */
    @Nullable
    Player player();

    /**
     * Get the fishing bag holder
     *
     * @return fishing bag holder
     */
    @NotNull
    FishingBagHolder holder();

    /**
     * Get the player's earning data
     *
     * @return earning data
     */
    @NotNull
    EarningData earningData();

    /**
     * Get the player's statistics
     *
     * @return statistics
     */
    @NotNull
    FishingStatistics statistics();

    /**
     * If the user is online on current server
     *
     * @return online or not
     */
    boolean isOnline();

    /**
     * If the data is locked
     *
     * @return locked or not
     */
    boolean isLocked();

    /**
     * Get the data in another minimized format that can be saved
     *
     * @return player data
     */
    @NotNull
    PlayerData toPlayerData();

    static Builder builder() {
        return new UserDataImpl.BuilderImpl();
    }

    interface Builder {

        /**
         * Set the username for the UserData being built.
         *
         * @param name the username to set.
         * @return the current Builder instance.
         */
        Builder name(String name);

        /**
         * Set the UUID for the UserData being built.
         *
         * @param uuid the UUID to set.
         * @return the current Builder instance.
         */
        Builder uuid(UUID uuid);

        /**
         * Set the FishingBagHolder for the UserData being built.
         *
         * @param holder the FishingBagHolder to set.
         * @return the current Builder instance.
         */
        Builder holder(FishingBagHolder holder);

        /**
         * Set the EarningData for the UserData being built.
         *
         * @param earningData the EarningData to set.
         * @return the current Builder instance.
         */
        Builder earningData(EarningData earningData);

        /**
         * Set the FishingStatistics for the UserData being built.
         *
         * @param statistics the FishingStatistics to set.
         * @return the current Builder instance.
         */
        Builder statistics(FishingStatistics statistics);

        Builder locked(boolean isLocked);

        Builder data(PlayerData playerData);

        /**
         * Build and return the UserData instance based on the current state of the Builder.
         *
         * @return the constructed UserData instance.
         */
        UserData build();
    }
}
