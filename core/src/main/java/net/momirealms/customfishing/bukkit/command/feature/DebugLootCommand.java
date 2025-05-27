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

package net.momirealms.customfishing.bukkit.command.feature;

import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.fishing.FishingGears;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class DebugLootCommand extends BukkitCommandFeature<CommandSender> {

    public DebugLootCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("surrounding", StringParser.stringComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(Stream.of("lava", "water", "void").map(Suggestion::suggestion).toList());
                    }
                }))
                .optional("page", IntegerParser.integerParser(1))
                .handler(context -> {
                    String surrounding = context.get("surrounding");
                    if (context.sender().getInventory().getItemInMainHand().getType() != Material.FISHING_ROD) {
                        handleFeedback(context, MessageConstants.COMMAND_DEBUG_LOOT_FAILURE_ROD);
                        return;
                    }
                    final Player player = context.sender();
                    int page = (int) context.optional("page").orElse(1) - 1;

                    Context<Player> playerContext = Context.player(player);
                    FishingGears gears = new FishingGears(playerContext);

                    Effect effect = Effect.newInstance();
                    // The effects impact mechanism at this stage
                    for (EffectModifier modifier : gears.effectModifiers()) {
                        for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                            consumer.accept(effect, playerContext, 0);
                        }
                    }

                    playerContext.arg(ContextKeys.SURROUNDING, surrounding);
                    Effect tempEffect = effect.copy();
                    for (EffectModifier modifier : gears.effectModifiers()) {
                        for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                            consumer.accept(tempEffect, playerContext, 1);
                        }
                    }

                    playerContext.arg(ContextKeys.OTHER_LOCATION, player.getLocation());
                    playerContext.arg(ContextKeys.OTHER_X, player.getLocation().getBlockX());
                    playerContext.arg(ContextKeys.OTHER_Y, player.getLocation().getBlockY());
                    playerContext.arg(ContextKeys.OTHER_Z, player.getLocation().getBlockZ());

                    Map<String, Double> weightMap = BukkitCustomFishingPlugin.getInstance().getLootManager().getWeightedLoots(tempEffect, playerContext);

                    if (weightMap.isEmpty()) {
                        handleFeedback(context, MessageConstants.COMMAND_DEBUG_LOOT_FAILURE_NO_LOOT);
                        return;
                    }

                    List<LootWithWeight> loots = new ArrayList<>();
                    double sum = 0;
                    for (Map.Entry<String, Double> entry : weightMap.entrySet()) {
                        double weight = entry.getValue();
                        String loot = entry.getKey();
                        if (weight <= 0) continue;
                        loots.add(new LootWithWeight(loot, weight));
                        sum += weight;
                    }
                    LootWithWeight[] lootArray = loots.toArray(new LootWithWeight[0]);
                    int maxPages = (int) Math.ceil((double) lootArray.length / 10) - 1;
                    if (page > maxPages) return;

                    quickSort(lootArray, 0,lootArray.length - 1);
                    Component component = Component.empty();
                    List<Component> children = new ArrayList<>();
                    int i = 0;
                    for (LootWithWeight loot : lootArray) {
                        if (i >= page * 10 && i < page * 10 + 10) {
                            children.add(Component.newline()
                                    .append(Component.text(loot.key + ": ").color(NamedTextColor.WHITE))
                                    .append(Component.text(String.format("%.4f", loot.weight * 100 / sum) + "% ").color(NamedTextColor.GOLD))
                                    .append(Component.text("(" + loot.weight + ")").color(NamedTextColor.GRAY)));
                        }
                        i++;
                    }
                    handleFeedback(context, MessageConstants.COMMAND_DEBUG_LOOT_SUCCESS, component.children(children));
                    Component previous = Component.text("( <<< )");
                    if (page > 0) {
                        previous = previous.color(NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand(commandConfig.getUsages().get(0) + " " + surrounding + " " + (page)));
                    } else {
                        previous = previous.color(NamedTextColor.GRAY);
                    }
                    Component next = Component.text("( >>> )");
                    if (page < maxPages) {
                        next = next.color(NamedTextColor.GREEN).clickEvent(ClickEvent.runCommand(commandConfig.getUsages().get(0) + " " + surrounding + " " + (page + 2)));
                    } else {
                        next = next.color(NamedTextColor.GRAY);
                    }
                    BukkitCustomFishingPlugin.getInstance().getSenderFactory().wrap(player)
                            .sendMessage(
                                Component.empty().children(List.of(
                                        previous,
                                        Component.text("   "),
                                        Component.text("[" + (page + 1) + "/" + (maxPages + 1) + "]").color(NamedTextColor.AQUA),
                                        Component.text("   "),
                                        next
                                ))
                            );
                });
    }

    @Override
    public String getFeatureID() {
        return "debug_loot";
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
