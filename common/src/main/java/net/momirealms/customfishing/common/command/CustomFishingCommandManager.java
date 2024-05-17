package net.momirealms.customfishing.common.command;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.util.Index;
import org.apache.logging.log4j.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface CustomFishingCommandManager<C> {

    String commandsFile = "commands.yml";

    void unregisterFeatures();

    void registerFeature(CommandFeature<C> feature, CommandConfig<C> config);

    void registerDefaultFeatures();

    Index<String, CommandFeature<C>> getFeatures();

    void setFeedbackConsumer(@NotNull TriConsumer<C, String, Component> feedbackConsumer);

    TriConsumer<C, String, Component> defaultFeedbackConsumer();

    CommandConfig<C> getCommandConfig(YamlDocument document, String featureID);

    Collection<Command.Builder<C>> buildCommandBuilders(CommandConfig<C> config);

    CommandManager<C> getCommandManager();

    void handleCommandFeedback(C sender, TranslatableComponent.Builder key, Component... args);

    void handleCommandFeedback(C sender, String node, Component component);
}
