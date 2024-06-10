package net.momirealms.customfishing.bukkit.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

public class ReloadCommand extends BukkitCommandFeature<CommandSender> {

    public ReloadCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s"))
                .handler(context -> {
                    long time1 = System.currentTimeMillis();
                    BukkitCustomFishingPlugin.getInstance().reload();
                    handleFeedback(context, MessageConstants.COMMAND_RELOAD_SUCCESS, Component.text(System.currentTimeMillis() - time1));
                });
    }

    @Override
    public String getFeatureID() {
        return "reload";
    }
}
