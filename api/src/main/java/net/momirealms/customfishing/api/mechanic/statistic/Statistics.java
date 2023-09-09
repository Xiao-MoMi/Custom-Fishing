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

import com.google.gson.annotations.SerializedName;
import net.momirealms.customfishing.api.data.StatisticData;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.loot.Loot;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Statistics {

    @SerializedName("statistic_map")
    private final ConcurrentHashMap<String, Integer> statisticMap;
    private int total;

    public Statistics(StatisticData statisticData) {
        this.statisticMap = new ConcurrentHashMap<>(statisticData.statisticMap);
        this.total = statisticMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    public synchronized void addLootAmount(Loot loot, Condition condition, int amount) {
        if (amount == 1) {
            addSingleLootAmount(loot, condition);
            return;
        }
        Integer previous = statisticMap.get(loot.getID());
        if (previous == null) previous = 0;
        int after = previous + amount;
        statisticMap.put(loot.getID(), after);
        total += amount;
        doSuccessTimesAction(previous, after, condition, loot);
    }

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

    public int getLootAmount(String key) {
        Integer amount = statisticMap.get(key);
        return amount == null ? 0 : amount;
    }

    public void reset() {
        statisticMap.clear();
        total = 0;
    }

    public Map<String, Integer> getStatisticMap() {
        return statisticMap;
    }

    public void setData(String key, int value) {
        if (value <= 0) {
            statisticMap.remove(key);
            return;
        }
        statisticMap.put(key, value);
    }

    public int getTotalCatchAmount() {
        return total;
    }
}
