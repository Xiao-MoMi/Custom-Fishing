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

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.util.PlayerUtils;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("DuplicatedCode")
public class GetItemCommand extends BukkitCommandFeature<CommandSender> {

    public GetItemCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("id", StringParser.stringComponent(StringParser.StringMode.QUOTED).suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(BukkitCustomFishingPlugin.getInstance().getItemManager().getItemIDs().stream().map(it -> Suggestion.suggestion("\"" + it + "\"")).toList());
                    }
                }))
                .optional("amount", IntegerParser.integerParser(1, 6400))
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .flag(manager.flagBuilder("to-inventory").withAliases("t").build())
                .handler(context -> {
                    final int amount = context.getOrDefault("amount", 1);
                    boolean toInv = context.flags().hasFlag("to-inventory");
                    final String id = context.get("id");
                    final Player player = context.sender();
                    try {
                        ItemStack itemStack = BukkitCustomFishingPlugin.getInstance().getItemManager().buildInternal(Context.player(player).arg(ContextKeys.ID, id), id);
                        if (itemStack == null) {
                            throw new RuntimeException("Unrecognized item id: " + id);
                        }
                        int amountToGive = amount;
                        int maxStack = itemStack.getMaxStackSize();
                        while (amountToGive > 0) {
                            int perStackSize = Math.min(maxStack, amountToGive);
                            amountToGive -= perStackSize;
                            ItemStack more = itemStack.clone();
                            more.setAmount(perStackSize);
                            if (toInv || player.getGameMode() == GameMode.SPECTATOR) {
                                PlayerUtils.putItemsToInventory(player.getInventory(), more, more.getAmount());
                            } else {
                                PlayerUtils.dropItem(player, more, false, true, false);
                            }
                        }
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_GET_SUCCESS, Component.text(amount), Component.text(id));
                    } catch (Exception e) {
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_FAILURE_NOT_EXIST, Component.text(id));
                        BukkitCustomFishingPlugin.getInstance().getPluginLogger().warn("Failed to get item:" + id, e);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "get_item";
    }
}
