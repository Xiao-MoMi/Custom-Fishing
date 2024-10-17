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

package net.momirealms.customfishing.bukkit.loot;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootManager;
import net.momirealms.customfishing.api.mechanic.requirement.ConditionalElement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.customfishing.common.util.WeightUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("DuplicatedCode")
public class BukkitLootManager implements LootManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, Loot> lootMap = new HashMap<>();
    private final HashMap<String, List<String>> groupMembersMap = new HashMap<>();
    private final LinkedHashMap<String, ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player>> lootConditions = new LinkedHashMap<>();

    public BukkitLootManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        this.lootMap.clear();
        this.groupMembersMap.clear();
        this.lootConditions.clear();
    }

    @Override
    public void load() {
        plugin.debug("Loaded " + lootMap.size() + " loots");
        for (Map.Entry<String, List<String>> entry : groupMembersMap.entrySet()) {
            plugin.debug("Group: {" + entry.getKey() + "} Members: " + entry.getValue());
        }
        File file = new File(plugin.getDataFolder(), "loot-conditions.yml");
        if (!file.exists()) {
            plugin.getBootstrap().saveResource("loot-conditions.yml", false);
        }
        YamlDocument lootConditionsConfig = plugin.getConfigManager().loadData(file);
        for (Map.Entry<String, Object> entry : lootConditionsConfig.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section section) {
                lootConditions.put(entry.getKey(), parseLootConditions(section));
            }
        }
    }

    private ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> parseLootConditions(Section section) {
        Section subSection = section.getSection("sub-groups");
        if (subSection == null) {
            return new ConditionalElement<>(
                    plugin.getConfigManager().parseWeightOperation(section.getStringList("list")),
                    Map.of(),
                    plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), false)
            );
        } else {
            HashMap<String, ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player>> subElements = new HashMap<>();
            for (Map.Entry<String, Object> entry : subSection.getStringRouteMappedValues(false).entrySet()) {
                if (entry.getValue() instanceof Section innerSection) {
                    subElements.put(entry.getKey(), parseLootConditions(innerSection));
                }
            }
            return new ConditionalElement<>(
                    plugin.getConfigManager().parseWeightOperation(section.getStringList("list")),
                    subElements,
                    plugin.getRequirementManager().parseRequirements(section.getSection("conditions"), false)
            );
        }
    }

    @Override
    public boolean registerLoot(@NotNull Loot loot) {
        if (lootMap.containsKey(loot.id())) return false;
        this.lootMap.put(loot.id(), loot);
        for (String group : loot.lootGroup()) {
            addGroupMember(group, loot.id());
        }
        return true;
    }

    @Override
    public Collection<Loot> getRegisteredLoots() {
        return lootMap.values();
    }

    private void addGroupMember(String group, String member) {
        List<String> members = groupMembersMap.get(group);
        if (members == null) {
            members = new ArrayList<>(List.of(member));
            groupMembersMap.put(group, members);
        } else {
            members.add(member);
        }
    }

    @NotNull
    @Override
    public List<String> getGroupMembers(String key) {
        return Optional.ofNullable(groupMembersMap.get(key)).orElse(List.of());
    }

    @NotNull
    @Override
    public Optional<Loot> getLoot(String key) {
        return Optional.ofNullable(lootMap.get(key));
    }

    @Override
    public HashMap<String, Double> getWeightedLoots(Effect effect, Context<Player> context) {
        HashMap<String, Double> lootWeightMap = new HashMap<>();
        for (ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> conditionalElement : lootConditions.values()) {
            modifyWeightMap(lootWeightMap, context, conditionalElement);
        }
        for (Pair<String, BiFunction<Context<Player>, Double, Double>> pair : effect.weightOperations()) {
            Double previous = lootWeightMap.get(pair.left());
            if (previous != null) {
                lootWeightMap.put(pair.left(), pair.right().apply(context, previous));
            }
        }
        for (Pair<String, BiFunction<Context<Player>, Double, Double>> pair : effect.weightOperationsIgnored()) {
            double previous = lootWeightMap.getOrDefault(pair.left(), 0d);
            lootWeightMap.put(pair.left(), pair.right().apply(context, previous));
        }
        return lootWeightMap;
    }

    @Nullable
    @Override
    public Loot getNextLoot(Effect effect, Context<Player> context) {
        HashMap<String, Double> lootWeightMap = new HashMap<>();
        for (ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> conditionalElement : lootConditions.values()) {
            modifyWeightMap(lootWeightMap, context, conditionalElement);
        }
        for (Pair<String, BiFunction<Context<Player>, Double, Double>> pair : effect.weightOperations()) {
            Double previous = lootWeightMap.get(pair.left());
            if (previous != null) {
                lootWeightMap.put(pair.left(), pair.right().apply(context, previous));
            }
        }
        for (Pair<String, BiFunction<Context<Player>, Double, Double>> pair : effect.weightOperationsIgnored()) {
            double previous = lootWeightMap.getOrDefault(pair.left(), 0d);
            lootWeightMap.put(pair.left(), pair.right().apply(context, previous));
        }

        plugin.debug(lootWeightMap);
        String lootID = WeightUtils.getRandom(lootWeightMap);
        return Optional.ofNullable(lootID)
                .map(id -> getLoot(lootID).orElseThrow(() -> new NullPointerException("Could not find loot " + lootID)))
                .orElse(null);
    }

    private void modifyWeightMap(Map<String, Double> weightMap, Context<Player> context, ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> conditionalElement) {
        if (conditionalElement == null) return;
        if (RequirementManager.isSatisfied(context, conditionalElement.getRequirements())) {
            for (Pair<String, BiFunction<Context<Player>, Double, Double>> modifierPair : conditionalElement.getElement()) {
                double previous = weightMap.getOrDefault(modifierPair.left(), 0d);
                weightMap.put(modifierPair.left(), modifierPair.right().apply(context, previous));
            }
            for (ConditionalElement<List<Pair<String, BiFunction<Context<Player>, Double, Double>>>, Player> sub : conditionalElement.getSubElements().values()) {
                modifyWeightMap(weightMap, context, sub);
            }
        }
    }
}
