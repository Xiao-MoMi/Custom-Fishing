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

package net.momirealms.customfishing.api.storage.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The PlayerData class holds data related to a player.
 * It includes the player's name, their fishing statistics, inventory data, and earnings data.
 */
public class PlayerData {

    public static final String DEFAULT_NAME = "";
    public static final StatisticData DEFAULT_STATISTICS = StatisticData.empty();
    public static final InventoryData DEFAULT_BAG = InventoryData.empty();
    public static final EarningData DEFAULT_EARNING = EarningData.empty();

    @SerializedName("name")
    protected String name;
    @SerializedName("stats")
    protected StatisticData statisticsData;
    @SerializedName("bag")
    protected InventoryData bagData;
    @SerializedName("trade")
    protected EarningData earningData;

    /**
     * Constructs a new PlayerData instance with specified values.
     *
     * @param name the name of the player.
     * @param statisticsData the fishing statistics data.
     * @param bagData the inventory data.
     * @param earningData the earnings data.
     */
    public PlayerData(String name, StatisticData statisticsData, InventoryData bagData, EarningData earningData) {
        this.name = name;
        this.statisticsData = statisticsData;
        this.bagData = bagData;
        this.earningData = earningData;
    }

    // A static instance representing a locked state of PlayerData.
    public static PlayerData LOCKED = empty();

    /**
     * Creates a new Builder instance for constructing PlayerData objects.
     *
     * @return a new Builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an instance of PlayerData with empty fields.
     *
     * @return a new instance of PlayerData with empty fields.
     */
    public static PlayerData empty() {
        return new Builder()
                .bag(InventoryData.empty())
                .earnings(EarningData.empty())
                .stats(StatisticData.empty())
                .build();
    }

    /**
     * The Builder class provides a fluent API for constructing PlayerData instances.
     */
    public static class Builder {

        private String name = DEFAULT_NAME;
        private StatisticData statisticsData = DEFAULT_STATISTICS;
        private InventoryData bagData = DEFAULT_BAG;
        private EarningData earningData = DEFAULT_EARNING;

        /**
         * Sets the name for the PlayerData instance.
         *
         * @param name the name of the player.
         * @return the Builder instance.
         */
        @NotNull
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the statistics data for the PlayerData instance.
         *
         * @param statisticsData the fishing statistics data.
         * @return the Builder instance.
         */
        @NotNull
        public Builder stats(@Nullable StatisticData statisticsData) {
            this.statisticsData = statisticsData;
            return this;
        }

        /**
         * Sets the inventory data for the PlayerData instance.
         *
         * @param inventoryData the inventory data.
         * @return the Builder instance.
         */
        @NotNull
        public Builder bag(@Nullable InventoryData inventoryData) {
            this.bagData = inventoryData;
            return this;
        }

        /**
         * Sets the earnings data for the PlayerData instance.
         *
         * @param earningData the earnings data.
         * @return the Builder instance.
         */
        @NotNull
        public Builder earnings(@Nullable EarningData earningData) {
            this.earningData = earningData;
            return this;
        }

        /**
         * Builds and returns the PlayerData instance.
         *
         * @return the constructed PlayerData instance.
         */
        @NotNull
        public PlayerData build() {
            return new PlayerData(name, statisticsData, bagData, earningData);
        }
    }

    /**
     * Gets the statistics data for the player.
     *
     * @return the fishing statistics data.
     */
    public StatisticData getStatistics() {
        return statisticsData;
    }

    /**
     * Gets the bag data for the player.
     *
     * @return the bag data.
     */
    public InventoryData getBagData() {
        return bagData;
    }

    /**
     * Gets the earnings data for the player.
     *
     * @return the earnings data.
     */
    public EarningData getEarningData() {
        return earningData;
    }

    /**
     * Gets the name of the player.
     *
     * @return the player's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the PlayerData instance is in a locked state.
     *
     * @return true if the PlayerData instance is locked, false otherwise.
     */
    public boolean isLocked() {
        return this == LOCKED;
    }
}
