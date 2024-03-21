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

package net.momirealms.customfishing.command.sub;

import de.tr7zw.changeme.nbtapi.NBTItem;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.IStringTooltip;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.momirealms.biomeapi.BiomeAPI;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import net.momirealms.customfishing.api.manager.AdventureManager;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import net.momirealms.customfishing.mechanic.fishing.FishingPreparationImpl;
import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.NBTUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class DebugCommand {

    public static DebugCommand INSTANCE = new DebugCommand();

    public CommandAPICommand getDebugCommand() {
        return new CommandAPICommand("debug")
                .withSubcommands(
                        getLootChanceCommand(),
                        getBiomeCommand(),
                        getSeasonCommand(),
                        getGroupCommand(),
                        getCategoryCommand(),
                        getNBTCommand(),
                        getLocationCommand()
                );
    }

    public CommandAPICommand getBiomeCommand() {
        return new CommandAPICommand("biome")
                .executesPlayer((player, arg) -> {
                    AdventureManagerImpl.getInstance().sendMessage(player, BiomeAPI.getBiomeAt(player.getLocation()));
                });
    }

    public CommandAPICommand getLocationCommand() {
        return new CommandAPICommand("location")
                .executesPlayer((player, arg) -> {
                    AdventureManagerImpl.getInstance().sendMessage(player, player.getLocation().toString());
                });
    }

    public CommandAPICommand getNBTCommand() {
        return new CommandAPICommand("nbt")
                .executesPlayer((player, arg) -> {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType() == Material.AIR)
                        return;
                    ArrayList<String> list = new ArrayList<>();
                    ConfigUtils.mapToReadableStringList(NBTUtils.compoundToMap(new NBTItem(item)), list, 0, false);
                    for (String line : list) {
                        AdventureManagerImpl.getInstance().sendMessage(player, line);
                    }
                });
    }

    public CommandAPICommand getSeasonCommand() {
        return new CommandAPICommand("season")
                .executesPlayer((player, arg) -> {
                    SeasonInterface seasonInterface = CustomFishingPlugin.get().getIntegrationManager().getSeasonInterface();
                    if (seasonInterface == null) {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, "NO SEASON PLUGIN");
                        return;
                    }
                    AdventureManagerImpl.getInstance().sendMessage(player, seasonInterface.getSeason(player.getLocation().getWorld()));
                });
    }

    public CommandAPICommand getGroupCommand() {
        return new CommandAPICommand("group")
                .withArguments(new StringArgument("group"))
                .executes((sender, arg) -> {
                    String group = (String) arg.get("group");
                    StringJoiner stringJoiner = new StringJoiner("<white>, </white>");
                    List<String> groups = CustomFishingPlugin.get().getLootManager().getLootGroup(group);
                    if (groups != null)
                        for (String key : groups) {
                            stringJoiner.add(key);
                        }
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "<white>Group<gold>{" + group + "}<yellow>[" + stringJoiner + "]");
                });
    }

    public CommandAPICommand getCategoryCommand() {
        return new CommandAPICommand("category")
                .withArguments(new StringArgument("category"))
                .executes((sender, arg) -> {
                    String c = (String) arg.get("category");
                    StringJoiner stringJoiner = new StringJoiner("<white>, </white>");
                    List<String> cs = CustomFishingPlugin.get().getStatisticsManager().getCategory(c);
                    if (cs != null)
                        for (String key : cs) {
                            stringJoiner.add(key);
                        }
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, "<white>Category<gold>{" + c + "}<yellow>[" + stringJoiner + "]");
                });
    }

    public CommandAPICommand getLootChanceCommand() {
        return new CommandAPICommand("loot-chance")
                .withArguments(new BooleanArgument("lava fishing").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(info ->
                        new IStringTooltip[] {
                                StringTooltip.ofString("true", "loots in lava"),
                                StringTooltip.ofString("false", "loots in water")
                        })))
                .executesPlayer((player, arg) -> {
                    if (player.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
                        AdventureManagerImpl.getInstance().sendMessageWithPrefix(player, "<red>Please hold a fishing rod before using this command.");
                        return;
                    }
                    FishingEffect initialEffect = CustomFishingPlugin.get().getEffectManager().getInitialEffect();
                    FishingPreparation fishingPreparation = new FishingPreparationImpl(player, CustomFishingPlugin.get());
                    boolean inLava = (boolean) arg.getOrDefault("lava fishing", false);
                    fishingPreparation.insertArg("{lava}", String.valueOf(inLava));
                    fishingPreparation.mergeEffect(initialEffect);
                    EffectCarrier totemEffect = CustomFishingPlugin.get().getTotemManager().getTotemEffect(player.getLocation());
                    if (totemEffect != null)
                        for (EffectModifier modifier : totemEffect.getEffectModifiers()) {
                            modifier.modify(initialEffect, fishingPreparation);
                        }
                    var map = CustomFishingPlugin.get().getLootManager().getPossibleLootKeysWithWeight(initialEffect, fishingPreparation);
                    List<LootWithWeight> loots = new ArrayList<>();
                    double sum = 0;
                    for (Map.Entry<String, Double> entry : map.entrySet()) {
                        double weight = entry.getValue();
                        String loot = entry.getKey();
                        if (weight <= 0) continue;
                        loots.add(new LootWithWeight(loot, weight));
                        sum += weight;
                    }
                    LootWithWeight[] lootArray = loots.toArray(new LootWithWeight[0]);
                    quickSort(lootArray, 0,lootArray.length - 1);
                    AdventureManager adventureManager = AdventureManagerImpl.getInstance();
                    adventureManager.sendMessage(player, "<red>---------- results ---------");
                    for (LootWithWeight loot : lootArray) {
                        adventureManager.sendMessage(player, loot.key() + ": <gold>" + String.format("%.2f", loot.weight()*100/sum) + "% <gray>(" + String.format("%.2f", loot.weight()) + ")");
                    }
                    adventureManager.sendMessage(player, "<red>----------- end -----------");
                });
    }

    public record LootWithWeight(String key, double weight) {
    }

    private static void quickSort(LootWithWeight[] loot, int low, int high) {
        if (low < high) {
            int pi = partition(loot, low, high);
            quickSort(loot, low, pi - 1);
            quickSort(loot, pi + 1, high);
        }
    }

    private static int partition(LootWithWeight[] loot, int low, int high) {
        double pivot = loot[high].weight();
        int i = low - 1;
        for (int j = low; j <= high - 1; j++) {
            if (loot[j].weight() > pivot) {
                i++;
                swap(loot, i, j);
            }
        }
        swap(loot, i + 1, high);
        return i + 1;
    }

    private static void swap(LootWithWeight[] loot, int i, int j) {
        LootWithWeight temp = loot[i];
        loot[i] = loot[j];
        loot[j] = temp;
    }
}
