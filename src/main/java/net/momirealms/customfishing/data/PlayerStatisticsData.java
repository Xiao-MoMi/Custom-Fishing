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

package net.momirealms.customfishing.data;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.manager.LootManager;
import net.momirealms.customfishing.object.action.Action;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerStatisticsData {

    private final ConcurrentHashMap<String, Integer> amountMap;
    private final LootManager lootManager;

    public PlayerStatisticsData() {
        this.amountMap = new ConcurrentHashMap<>();
        this.lootManager = CustomFishing.getInstance().getLootManager();
    }

    public PlayerStatisticsData(ConfigurationSection section) {
        this.lootManager = CustomFishing.getInstance().getLootManager();
        this.amountMap = new ConcurrentHashMap<>();
        for (String key : section.getKeys(false)) {
            amountMap.put(key, section.getInt(key));
        }
    }

    public PlayerStatisticsData(String longText) {
        this.lootManager = CustomFishing.getInstance().getLootManager();
        this.amountMap = (ConcurrentHashMap<String, Integer>) Arrays.stream(longText.split(";"))
                .map(element -> element.split(":"))
                .filter(pair -> pair.length == 2)
                .collect(Collectors.toConcurrentMap(pair -> pair[0], pair -> Integer.parseInt(pair[1])));
    }

    public String getLongText() {
        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<String, Integer> entry : amountMap.entrySet()) {
            joiner.add(entry.getKey() + ":" + entry.getValue());
        }
        return joiner.toString();
    }

    public void addFishAmount(Loot loot, int amount, UUID uuid) {
        Integer previous = amountMap.get(loot.getKey());
        if (previous == null) previous = 0;
        int after = previous + amount;
        amountMap.put(loot.getKey(), after);
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        HashMap<Integer, Action[]> actionMap = loot.getSuccessTimesActions();
        if (actionMap != null) {
            for (Map.Entry<Integer, Action[]> entry : actionMap.entrySet()) {
                if (entry.getKey() > previous && entry.getKey() <= after) {
                    for (Action action : entry.getValue()) {
                        action.doOn(player, null);
                    }
                }
            }
        }
    }

    public int getFishAmount(String key) {
        Integer amount = amountMap.get(key);
        return amount == null ? 0 : amount;
    }

    public boolean hasFished(String key) {
        return amountMap.containsKey(key);
    }

    /**
     * Get a category's unlock progress
     * @param category category name
     * @return percent
     */
    public double getCategoryUnlockProgress(String category) {
        List<String> categories = lootManager.getCategories(category);
        if (categories == null) return -1d;
        double total = categories.size();
        double unlocked = 0;
        for (String value : categories) {
            if (hasFished(value)) {
                unlocked++;
            }
        }
        return (unlocked / total) * 100d;
    }

    public int getCategoryTotalFishAmount(String category) {
        List<String> categories = lootManager.getCategories(category);
        if (categories == null) return -1;
        int total = 0;
        for (String value : categories) {
            total += getFishAmount(value);
        }
        return total;
    }

    public void reset() {
        amountMap.clear();
    }

    public ConcurrentHashMap<String, Integer> getAmountMap() {
        return amountMap;
    }

    public void setData(String key, int value) {
        amountMap.put(key, value);
    }
}
