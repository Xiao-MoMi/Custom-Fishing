package net.momirealms.customfishing.bukkit.command.feature;

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;

public class OpenBagCommand extends BukkitCommandFeature<CommandSender> {

    public OpenBagCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("player", PlayerParser.playerParser())
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    final Player player = context.get("player");
                    BukkitCustomFishingPlugin.getInstance().getBagManager().openBag(player, player.getUniqueId());
                    handleFeedback(context, MessageConstants.COMMAND_BAG_OPEN_SUCCESS, Component.text(player.getName()));
                });
    }

    @Override
    public String getFeatureID() {
        return "open_bag";
    }
}
