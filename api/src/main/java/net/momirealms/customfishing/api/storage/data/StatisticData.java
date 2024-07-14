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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The StatisticData class stores fishing statistics including amounts and sizes
 * of fish caught, represented as maps.
 */
public class StatisticData {

    @SerializedName(value="amount", alternate={"map"})
    public Map<String, Integer> amountMap;

    @SerializedName("size")
    public Map<String, Float> sizeMap;

    /**
     * Default constructor that initializes the sizeMap and amountMap as empty HashMaps.
     */
    private StatisticData() {
        this.sizeMap = new HashMap<>();
        this.amountMap = new HashMap<>();
    }

    /**
     * Parameterized constructor that initializes the sizeMap and amountMap with provided values.
     *
     * @param amount a map containing the amount of each type of fish caught.
     * @param size a map containing the size of each type of fish caught.
     */
    public StatisticData(@NotNull Map<String, Integer> amount, @NotNull Map<String, Float> size) {
        this.amountMap = amount;
        this.sizeMap = size;
    }

    /**
     * Creates an instance of StatisticData with empty maps.
     *
     * @return a new instance of StatisticData with empty maps.
     */
    public static StatisticData empty() {
        return new StatisticData();
    }
}
