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

import net.momirealms.customfishing.api.mechanic.statistic.FishingStatistics;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.data.PlayerData;

import java.util.UUID;

public interface UserData {

    /**
     * Get the username
     *
     * @return user name
     */
    String getName();

    /**
     * Get the user's uuid
     *
     * @return uuid
     */
    UUID getUUID();

    /**
     * Get the fishing bag holder
     *
     * @return fishing bag holder
     */
    FishingBagHolder getHolder();

    /**
     * Get the player's earning data
     *
     * @return earning data
     */
    EarningData getEarningData();

    /**
     * Get the player's statistics
     *
     * @return statistics
     */
    FishingStatistics getStatistics();

    /**
     * If the user is online on current server
     *
     * @return online or not
     */
    boolean isOnline();

    /**
     * Get the data in another minimized format that can be saved
     *
     * @return player data
     */
    PlayerData getPlayerData();

    /**
     * Get the user
     *
     * @return user
     */
    O getUser();
}
