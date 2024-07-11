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

/**
 * Interface representing user data.
 * This interface provides methods for accessing and managing user-related information.
 */
public interface UserData {

    /**
     * Retrieves the username.
     *
     * @return the username as a {@link String}
     */
    @NotNull
    String name();

    /**
     * Retrieves the user's UUID.
     *
     * @return the UUID as a {@link UUID}
     */
    @NotNull
    UUID uuid();

    /**
     * Retrieves the {@link Player} instance if the player is online.
     *
     * @return the {@link Player} instance, or null if the player is offline
     */
    @Nullable
    Player player();

    /**
     * Retrieves the fishing bag holder.
     *
     * @return the {@link FishingBagHolder}
     */
    @NotNull
    FishingBagHolder holder();

    /**
     * Retrieves the player's earning data.
     *
     * @return the {@link EarningData}
     */
    @NotNull
    EarningData earningData();

    /**
     * Retrieves the player's fishing statistics.
     *
     * @return the {@link FishingStatistics}
     */
    @NotNull
    FishingStatistics statistics();

    /**
     * Checks if the user is online on the current server.
     *
     * @return true if the user is online, false otherwise
     */
    boolean isOnline();

    /**
     * Checks if the data is locked.
     *
     * @return true if the data is locked, false otherwise
     */
    boolean isLocked();

    /**
     * Converts the user data to a minimized format that can be saved.
     *
     * @return the {@link PlayerData}
     */
    @NotNull
    PlayerData toPlayerData();

    /**
     * Creates a new {@link Builder} instance to construct {@link UserData}.
     *
     * @return a new {@link Builder} instance
     */
    static Builder builder() {
        return new UserDataImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing instances of {@link UserData}.
     */
    interface Builder {

        /**
         * Sets the username for the {@link UserData} being built.
         *
         * @param name the username to set
         * @return the current {@link Builder} instance for method chaining
         */
        Builder name(String name);

        /**
         * Sets the UUID for the {@link UserData} being built.
         *
         * @param uuid the UUID to set
         * @return the current {@link Builder} instance for method chaining
         */
        Builder uuid(UUID uuid);

        /**
         * Sets the fishing bag holder for the {@link UserData} being built.
         *
         * @param holder the {@link FishingBagHolder} to set
         * @return the current {@link Builder} instance for method chaining
         */
        Builder holder(FishingBagHolder holder);

        /**
         * Sets the earning data for the {@link UserData} being built.
         *
         * @param earningData the {@link EarningData} to set
         * @return the current {@link Builder} instance for method chaining
         */
        Builder earningData(EarningData earningData);

        /**
         * Sets the fishing statistics for the {@link UserData} being built.
         *
         * @param statistics the {@link FishingStatistics} to set
         * @return the current {@link Builder} instance for method chaining
         */
        Builder statistics(FishingStatistics statistics);

        /**
         * Sets whether the data is locked for the {@link UserData} being built.
         *
         * @param isLocked true if the data should be locked, false otherwise
         * @return the current {@link Builder} instance for method chaining
         */
        Builder locked(boolean isLocked);

        /**
         * Sets the player data for the {@link UserData} being built.
         *
         * @param playerData the {@link PlayerData} to set
         * @return the current {@link Builder} instance for method chaining
         */
        Builder data(PlayerData playerData);

        /**
         * Builds and returns the {@link UserData} instance based on the current state of the {@link Builder}.
         *
         * @return the constructed {@link UserData} instance
         */
        UserData build();
    }
}
