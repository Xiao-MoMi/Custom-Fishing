package net.momirealms.customfishing.common.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

public interface CommandFeature<C> {

    Command<C> registerCommand(CommandManager<C> cloudCommandManager, Command.Builder<C> builder);

    String getFeatureID();

    void registerRelatedFunctions();

    void unregisterRelatedFunctions();

    void handleFeedback(CommandContext<?> context, TranslatableComponent.Builder key, Component... args);

    void handleFeedback(C sender, TranslatableComponent.Builder key, Component... args);

    CustomFishingCommandManager<C> getCustomFishingCommandManager();

    CommandConfig<C> getCommandConfig();
}
