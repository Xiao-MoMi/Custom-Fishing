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

package net.momirealms.customfishing.api.mechanic.statistic;

import net.momirealms.customfishing.common.util.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FishingStatisticsImpl implements FishingStatistics {

    private int amountOfFishCaught;
    private final Map<String, Integer> amountMap;
    private final Map<String, Float> sizeMap;

    public FishingStatisticsImpl(HashMap<String, Integer> amountMap, HashMap<String, Float> sizeMap) {
        this.amountMap = Collections.synchronizedMap(amountMap);
        this.sizeMap = Collections.synchronizedMap(sizeMap);
        this.amountOfFishCaught = amountMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int amountOfFishCaught() {
        return amountOfFishCaught;
    }

    @Override
    public void amountOfFishCaught(int amountOfFishCaught) {
        this.amountOfFishCaught = amountOfFishCaught;
    }

    @Override
    public int getAmount(String id) {
        return amountMap.getOrDefault(id, -1);
    }

    @Override
    public Pair<Integer, Integer> addAmount(String id, int amount) {
        if (amount <= 0) return Pair.of(-1, -1);
        int previous = amountMap.getOrDefault(id, 0);
        amountMap.put(id, previous + amount);
        amountOfFishCaught += amount;
        return Pair.of(previous, previous + amount);
    }

    @Override
    public void setAmount(String id, int amount) {
        if (amount < 0) amount = 0;
        int previous = amountMap.getOrDefault(id, 0);
        int delta = amount - previous;
        this.amountOfFishCaught += delta;
        amountMap.put(id, amount);
    }

    @Override
    public float getMaxSize(String id) {
        return sizeMap.getOrDefault(id, -1f);
    }

    @Override
    public void setMaxSize(String id, float maxSize) {
        if (maxSize < 0) maxSize = 0;
        sizeMap.put(id, maxSize);
    }

    @Override
    public boolean updateSize(String id, float newSize) {
        if (newSize <= 0) return false;
        float previous = sizeMap.getOrDefault(id, 0f);
        if (previous >= newSize) return false;
        sizeMap.put(id, newSize);
        return true;
    }

    @Override
    public void reset() {
        this.sizeMap.clear();
        this.amountMap.clear();
        this.amountOfFishCaught = 0;
    }

    @Override
    public Map<String, Integer> amountMap() {
        return amountMap;
    }

    @Override
    public Map<String, Float> sizeMap() {
        return sizeMap;
    }

    public static class BuilderImpl implements Builder {
        private final HashMap<String, Integer> amountMap = new HashMap<>();
        private final HashMap<String, Float> sizeMap = new HashMap<>();
        @Override
        public Builder amountMap(Map<String, Integer> amountMap) {
            this.amountMap.putAll(amountMap);
            return this;
        }
        @Override
        public Builder sizeMap(Map<String, Float> sizeMap) {
            this.sizeMap.putAll(sizeMap);
            return this;
        }
        @Override
        public FishingStatistics build() {
            return new FishingStatisticsImpl(amountMap, sizeMap);
        }
    }
}
