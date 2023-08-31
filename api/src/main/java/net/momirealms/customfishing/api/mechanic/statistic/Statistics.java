package net.momirealms.customfishing.api.mechanic.statistic;

import com.google.gson.annotations.SerializedName;
import net.momirealms.customfishing.api.data.StatisticData;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class Statistics {

    @SerializedName("statistic_map")
    private final HashMap<String, Integer> statisticMap;
    private int total;

    public Statistics() {
        this.statisticMap = new HashMap<>();
        this.total = 0;
    }

    public Statistics(ConfigurationSection section) {
        this.statisticMap = new HashMap<>();
        this.total = 0;
        for (String key : section.getKeys(false)) {
            int amount = section.getInt(key);
            total += amount;
            statisticMap.put(key, amount);
        }
    }

    public Statistics(StatisticData statisticData) {
        this.statisticMap = new HashMap<>(statisticData.statisticMap);
        this.total = statisticMap.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void addLootAmount(Loot loot, FishingPreparation fishingPreparation, int amount) {
        Integer previous = statisticMap.get(loot.getID());
        if (previous == null) previous = 0;
        int after = previous + amount;
        statisticMap.put(loot.getID(), after);
        total += amount;
        doSuccessTimesAction(previous, after, fishingPreparation, loot);
    }

    private void doSuccessTimesAction(Integer previous, int after, FishingPreparation fishingPreparation, Loot loot) {
        HashMap<Integer, Action[]> actionMap = loot.getSuccessTimesActionMap();
        if (actionMap != null) {
            for (Map.Entry<Integer, Action[]> entry : actionMap.entrySet()) {
                if (entry.getKey() > previous && entry.getKey() <= after) {
                    for (Action action : entry.getValue()) {
                        action.trigger(fishingPreparation);
                    }
                }
            }
        }
    }

    public int getFishAmount(String key) {
        Integer amount = statisticMap.get(key);
        return amount == null ? 0 : amount;
    }

    public boolean hasFished(String key) {
        return statisticMap.containsKey(key);
    }

    public void reset() {
        statisticMap.clear();
    }

    public HashMap<String, Integer> getStatisticMap() {
        return statisticMap;
    }

    public void setData(String key, int value) {
        statisticMap.put(key, value);
    }

    public int getTotalCatchAmount() {
        return total;
    }
}
