package net.momirealms.customfishing.bukkit.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.bukkit.util.PlayerUtils;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
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
                .required("id", StringParser.stringComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(BukkitCustomFishingPlugin.getInstance().getItemManager().getItemIDs().stream().map(Suggestion::suggestion).toList());
                    }
                }))
                .optional("amount", IntegerParser.integerParser(1, 6400))
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    final int amount = context.getOrDefault("amount", 1);
                    final String id = context.get("id");
                    final Player player = context.sender();
                    try {
                        ItemStack itemStack = BukkitCustomFishingPlugin.getInstance().getItemManager().buildInternal(Context.player(player).arg(ContextKeys.ID, id), id);
                        if (itemStack == null) {
                            throw new RuntimeException("Unrecognized item id: " + id);
                        }
                        int amountToGive = amount;
                        int maxStack = itemStack.getType().getMaxStackSize();
                        while (amountToGive > 0) {
                            int perStackSize = Math.min(maxStack, amountToGive);
                            amountToGive -= perStackSize;
                            ItemStack more = itemStack.clone();
                            more.setAmount(perStackSize);
                            PlayerUtils.dropItem(player, more, false, true, false);
                        }
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_GET_SUCCESS, Component.text(amount), Component.text(id));
                    } catch (NullPointerException e) {
                        handleFeedback(context, MessageConstants.COMMAND_ITEM_FAILURE_NOT_EXIST, Component.text(id));
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "get_item";
    }
}
