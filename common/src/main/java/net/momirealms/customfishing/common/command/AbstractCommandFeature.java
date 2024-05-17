package net.momirealms.customfishing.common.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.momirealms.customfishing.common.sender.SenderFactory;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

public abstract class AbstractCommandFeature<C> implements CommandFeature<C> {

    protected final CustomFishingCommandManager<C> customFishingCommandManager;
    protected CommandConfig<C> commandConfig;

    public AbstractCommandFeature(CustomFishingCommandManager<C> customFishingCommandManager) {
        this.customFishingCommandManager = customFishingCommandManager;
    }

    protected abstract SenderFactory<?, C> getSenderFactory();

    public abstract Command.Builder<? extends C> assembleCommand(CommandManager<C> manager, Command.Builder<C> builder);

    @Override
    @SuppressWarnings("unchecked")
    public Command<C> registerCommand(CommandManager<C> manager, Command.Builder<C> builder) {
        Command<C> command = (Command<C>) assembleCommand(manager, builder).build();
        manager.command(command);
        return command;
    }

    @Override
    public void registerRelatedFunctions() {
        // empty
    }

    @Override
    public void unregisterRelatedFunctions() {
        // empty
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleFeedback(CommandContext<?> context, TranslatableComponent.Builder key, Component... args) {
        if (context.flags().hasFlag("silent")) {
            return;
        }
        customFishingCommandManager.handleCommandFeedback((C) context.sender(), key, args);
    }

    @Override
    public void handleFeedback(C sender, TranslatableComponent.Builder key, Component... args) {
        customFishingCommandManager.handleCommandFeedback(sender, key, args);
    }

    @Override
    public CustomFishingCommandManager<C> getCustomFishingCommandManager() {
        return customFishingCommandManager;
    }

    @Override
    public CommandConfig<C> getCommandConfig() {
        return commandConfig;
    }

    public void setCommandConfig(CommandConfig<C> commandConfig) {
        this.commandConfig = commandConfig;
    }
}
