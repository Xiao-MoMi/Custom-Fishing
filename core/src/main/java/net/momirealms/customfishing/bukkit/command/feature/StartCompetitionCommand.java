package net.momirealms.customfishing.bukkit.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.concurrent.CompletableFuture;

public class StartCompetitionCommand extends BukkitCommandFeature<CommandSender> {

    public StartCompetitionCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("id", StringParser.stringComponent().suggestionProvider(new SuggestionProvider<>() {
                    @Override
                    public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<Object> context, @NonNull CommandInput input) {
                        return CompletableFuture.completedFuture(BukkitCustomFishingPlugin.getInstance().getCompetitionManager().getCompetitionIDs().stream().map(Suggestion::suggestion).toList());
                    }
                }))
                .optional("group", StringParser.stringParser())
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    String id = context.get("id");
                    String group = context.getOrDefault("group", null);
                    if (BukkitCustomFishingPlugin.getInstance().getCompetitionManager().startCompetition(id, true, group)) {
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_START_SUCCESS);
                    } else {
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_FAILURE_NOT_EXIST, Component.text(id));
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "start_competition";
    }
}
