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

import java.util.HashMap;
import java.util.Map;

public class StatisticData {

    @SerializedName(value="amount", alternate={"map"})
    public Map<String, Integer> amountMap;

    @SerializedName("size")
    public Map<String, Float> sizeMap;

    public StatisticData() {
        this.sizeMap = new HashMap<>();
        this.amountMap = new HashMap<>();
    }

    public StatisticData(@NotNull Map<String, Integer> amount, @NotNull Map<String, Float> size) {
        this.amountMap = amount;
        this.sizeMap = size;
    }

    public static StatisticData empty() {
        return new StatisticData();
    }
}
