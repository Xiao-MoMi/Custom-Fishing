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

package net.momirealms.customfishing.api.data.user;

import net.momirealms.customfishing.api.data.EarningData;
import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.mechanic.bag.FishingBagHolder;
import net.momirealms.customfishing.api.mechanic.statistic.Statistics;

import java.util.UUID;

public interface OfflineUser {
    String getName();

    UUID getUUID();

    FishingBagHolder getHolder();

    EarningData getEarningData();

    Statistics getStatistics();

    boolean isOnline();

    PlayerData getPlayerData();
}
