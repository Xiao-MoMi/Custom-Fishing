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

import net.momirealms.customfishing.api.data.StatisticData;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.loot.Loot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a statistics system for tracking loot and catch amounts.
 */
public class Statistics {

    private final ConcurrentHashMap<String, Integer> statisticMap;
    private final ConcurrentHashMap<String, Float> sizeMap;
    private int total;

    /**
     * Creates a new instance of Statistics based on provided statistic data.
     *
     * @param statisticData The initial statistic data.
     */
    public Statistics(StatisticData statisticData) {
        this.statisticMap = new ConcurrentHashMap<>(statisticData.amountMap);
        this.sizeMap = new ConcurrentHashMap<>(statisticData.sizeMap);
        this.total = statisticMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Adds an amount of loot to the statistics.
     *
     * @param loot      The loot item.
     * @param condition The condition associated with the loot.
     * @param amount    The amount of loot to add.
     */
    public synchronized void addLootAmount(Loot loot, Condition condition, int amount) {
        if (amount < 1) {
            return;
        }
        if (amount == 1) {
            addSingleLootAmount(loot, condition);
            return;
        }
        Integer previous = statisticMap.get(loot.getStatisticKey().getAmountKey());
        if (previous == null) previous = 0;
        int after = previous + amount;
        statisticMap.put(loot.getStatisticKey().getAmountKey(), after);
        total += amount;
        doSuccessTimesAction(previous, after, condition, loot);
    }

    /**
     * Performs actions associated with the success times of acquiring loot.
     *
     * @param previous  The previous success times.
     * @param after     The updated success times.
     * @param condition The condition associated with the loot.
     * @param loot      The loot item.
     */
    private void doSuccessTimesAction(Integer previous, int after, Condition condition, Loot loot) {
        HashMap<Integer, Action[]> actionMap = loot.getSuccessTimesActionMap();
        if (actionMap != null) {
            for (Map.Entry<Integer, Action[]> entry : actionMap.entrySet()) {
                if (entry.getKey() > previous && entry.getKey() <= after) {
                    for (Action action : entry.getValue()) {
                        action.trigger(condition);
                    }
                }
            }
        }
    }

    public boolean setSizeIfHigher(String loot, float size) {
        float previous = sizeMap.getOrDefault(loot, 0f);
        if (previous >= size) return false;
        sizeMap.put(loot, size);
        return true;
    }

    /**
     * Adds a single loot amount to the statistics.
     *
     * @param loot      The loot item.
     * @param condition The condition associated with the loot.
     */
    private void addSingleLootAmount(Loot loot, Condition condition) {
        Integer previous = statisticMap.get(loot.getID());
        if (previous == null) previous = 0;
        int after = previous + 1;
        statisticMap.put(loot.getID(), after);
        total += 1;
        Action[] actions = loot.getSuccessTimesActionMap().get(after);
        if (actions != null)
            for (Action action : actions) {
                action.trigger(condition);
            }
    }

    /**
     * Gets the amount of a specific loot item in the statistics.
     *
     * @param key The key of the loot item.
     * @return The amount of the specified loot item.
     */
    public int getLootAmount(String key) {
        return statisticMap.getOrDefault(key, 0);
    }

    public float getSizeRecord(String key) {
        return sizeMap.getOrDefault(key, 0f);
    }

    /**
     * Resets the statistics data.
     */
    public void reset() {
        statisticMap.clear();
        total = 0;
    }

    /**
     * Gets the statistic map containing loot item keys and their respective amounts.
     *
     * @return The statistic map.
     */
    public Map<String, Integer> getStatisticMap() {
        return statisticMap;
    }

    public ConcurrentHashMap<String, Float> getSizeMap() {
        return sizeMap;
    }

    /**
     * Sets data for a specific key in the statistics.
     *
     * @param key   The key to set data for.
     * @param value The value to set.
     */
    public void setData(String key, int value) {
        if (value <= 0) {
            statisticMap.remove(key);
            return;
        }
        statisticMap.put(key, value);
    }

    /**
     * Gets the total catch amount across all loot items.
     *
     * @return The total catch amount.
     */
    public int getTotalCatchAmount() {
        return total;
    }
}
