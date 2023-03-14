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
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.manager.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerStatisticsData {

    private final ConcurrentHashMap<String, Integer> amountMap;
    private int total_catch_amount;

    public PlayerStatisticsData() {
        this.amountMap = new ConcurrentHashMap<>();
        this.total_catch_amount = 0;
    }

    public PlayerStatisticsData(ConfigurationSection section) {
        this.amountMap = new ConcurrentHashMap<>();
        this.total_catch_amount = 0;
        for (String key : section.getKeys(false)) {
            int amount = section.getInt(key);
            total_catch_amount += amount;
            amountMap.put(key, amount);
        }
    }

    public PlayerStatisticsData(String longText) {
        this.total_catch_amount = 0;
        this.amountMap = (ConcurrentHashMap<String, Integer>) Arrays.stream(longText.split(";"))
                .map(element -> element.split(":"))
                .filter(pair -> pair.length == 2)
                .collect(Collectors.toConcurrentMap(pair -> pair[0], pair -> {
                    int amount = Integer.parseInt(pair[1]);
                    total_catch_amount += amount;
                    return amount;
                }));
    }

    public String getLongText() {
        StringJoiner joiner = new StringJoiner(";");
        for (Map.Entry<String, Integer> entry : amountMap.entrySet()) {
            joiner.add(entry.getKey() + ":" + entry.getValue());
        }
        return joiner.toString();
    }

    public void addFishAmount(Loot loot, UUID uuid, int amount) {
        Integer previous = amountMap.get(loot.getKey());
        if (previous == null) previous = 0;
        int after = previous + amount;
        amountMap.put(loot.getKey(), after);
        total_catch_amount += amount;
        Player player = Bukkit.getPlayer(uuid);
        if (player == null) return;
        doSuccessTimesAction(previous, after, player, loot);
    }

    private void doSuccessTimesAction(Integer previous, int after, Player player, Loot vanilla) {
        HashMap<Integer, Action[]> actionMap = vanilla.getSuccessTimesActions();
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
        List<String> categories = CustomFishing.getInstance().getLootManager().getCategories(category);
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
        List<String> categories = CustomFishing.getInstance().getLootManager().getCategories(category);
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

    public int getTotalCatchAmount() {
        return total_catch_amount;
    }
}
