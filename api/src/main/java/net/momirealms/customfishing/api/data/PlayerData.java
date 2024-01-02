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

package net.momirealms.customfishing.api.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerData {

    @SerializedName("name")
    protected String name;
    @SerializedName("stats")
    protected StatisticData statisticsData;
    @SerializedName("bag")
    protected InventoryData bagData;
    @SerializedName("trade")
    protected EarningData earningData;

    public static PlayerData LOCKED = empty();

    public static Builder builder() {
        return new Builder();
    }

    public static PlayerData empty() {
        return new Builder()
                .setBagData(InventoryData.empty())
                .setEarningData(EarningData.empty())
                .setStats(StatisticData.empty())
                .build();
    }

    public static class Builder {

        private final PlayerData playerData;

        public Builder() {
            this.playerData = new PlayerData();
        }

        @NotNull
        public Builder setName(@Nullable String name) {
            this.playerData.name = name;
            return this;
        }

        @NotNull
        public Builder setStats(@Nullable StatisticData statisticsData) {
            this.playerData.statisticsData = statisticsData;
            return this;
        }

        @NotNull
        public Builder setBagData(@Nullable InventoryData inventoryData) {
            this.playerData.bagData = inventoryData;
            return this;
        }

        @NotNull
        public Builder setEarningData(@Nullable EarningData earningData) {
            this.playerData.earningData = earningData;
            return this;
        }

        @NotNull
        public PlayerData build() {
            return this.playerData;
        }
    }

    public StatisticData getStatistics() {
        return statisticsData;
    }

    public InventoryData getBagData() {
        return bagData;
    }

    public EarningData getEarningData() {
        return earningData;
    }

    public String getName() {
        return name;
    }

    public boolean isLocked() {
        return this == LOCKED;
    }
}
