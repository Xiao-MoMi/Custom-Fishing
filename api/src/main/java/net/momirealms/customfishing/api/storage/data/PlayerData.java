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

package net.momirealms.customfishing.api.storage.data;

import com.google.gson.annotations.SerializedName;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

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
    transient private UUID uuid;
    transient private boolean locked;
    transient private byte[] jsonBytes;

    public PlayerData(UUID uuid, String name, StatisticData statisticsData, InventoryData bagData, EarningData earningData, boolean isLocked) {
        this.name = name;
        this.statisticsData = statisticsData;
        this.bagData = bagData;
        this.earningData = earningData;
        this.locked = isLocked;
        this.uuid = uuid;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static PlayerData empty() {
        return new Builder()
                .bag(InventoryData.empty())
                .earnings(EarningData.empty())
                .statistics(StatisticData.empty())
                .uuid(new UUID(0, 0))
                .locked(false)
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
        private boolean isLocked = false;
        private UUID uuid;

        @NotNull
        public Builder name(@NotNull String name) {
            this.name = name;
            return this;
        }

        @NotNull
        public Builder uuid(@NotNull UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        @NotNull
        public Builder locked(boolean locked) {
            this.isLocked = locked;
            return this;
        }

        @NotNull
        public Builder statistics(@Nullable StatisticData statisticsData) {
            this.statisticsData = statisticsData;
            return this;
        }

        @NotNull
        public Builder bag(@Nullable InventoryData inventoryData) {
            this.bagData = inventoryData;
            return this;
        }

        @NotNull
        public Builder earnings(@Nullable EarningData earningData) {
            this.earningData = earningData;
            return this;
        }

        @NotNull
        public PlayerData build() {
            return new PlayerData(requireNonNull(uuid), name, statisticsData, bagData, earningData, isLocked);
        }
    }

    public byte[] toBytes() {
        if (jsonBytes == null) {
            jsonBytes = BukkitCustomFishingPlugin.getInstance().getStorageManager().toBytes(this);
        }
        return jsonBytes;
    }

    /**
     * Gets the statistics data for the player.
     *
     * @return the fishing statistics data.
     */
    public StatisticData statistics() {
        return statisticsData;
    }

    /**
     * Gets the bag data for the player.
     *
     * @return the bag data.
     */
    public InventoryData bagData() {
        return bagData;
    }

    /**
     * Gets the earnings data for the player.
     *
     * @return the earnings data.
     */
    public EarningData earningData() {
        return earningData;
    }

    /**
     * Gets the name of the player.
     *
     * @return the player's name.
     */
    public String name() {
        return name;
    }

    /**
     * Gets if the data is locked
     *
     * @return locked or not
     */
    public boolean locked() {
        return locked;
    }

    /**
     * Set if the data is locked
     *
     * @param locked locked or not
     */
    public void locked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Gets the uuid
     *
     * @return uuid
     */
    public UUID uuid() {
        return uuid;
    }

    /**
     * Set the uuid of the data
     *
     * @param uuid uuid
     */
    public void uuid(UUID uuid) {
        this.uuid = uuid;
    }
}
