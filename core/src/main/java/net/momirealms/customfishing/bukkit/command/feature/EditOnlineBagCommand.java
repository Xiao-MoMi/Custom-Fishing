package net.momirealms.customfishing.bukkit.command.feature;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;

public class EditOnlineBagCommand extends BukkitCommandFeature<CommandSender> {

    public EditOnlineBagCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("player", PlayerParser.playerParser())
                .handler(context -> {
                    Player admin = context.sender();
                    Player online = context.get("player");
                    BukkitCustomFishingPlugin.getInstance().getBagManager().openBag(admin, online.getUniqueId());
                });
    }

    @Override
    public String getFeatureID() {
        return "edit_online_bag";
    }
}
