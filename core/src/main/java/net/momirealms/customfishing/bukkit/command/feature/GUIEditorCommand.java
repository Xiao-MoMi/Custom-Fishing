package net.momirealms.customfishing.bukkit.command.feature;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.bukkit.gui.page.file.FileSelector;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.io.File;

public class GUIEditorCommand extends BukkitCommandFeature<CommandSender> {

    public GUIEditorCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .handler(context -> {
                    new FileSelector(context.sender(), new File(BukkitCustomFishingPlugin.getInstance().getDataFolder(), "contents"));
                });
    }

    @Override
    public String getFeatureID() {
        return "browser";
    }
}
