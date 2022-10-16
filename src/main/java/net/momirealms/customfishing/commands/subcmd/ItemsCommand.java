package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ItemsCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new ItemsCommand();

    public ItemsCommand() {
        super("items", null);
        regSubCommand(UtilCommand.INSTANCE);
        regSubCommand(RodCommand.INSTANCE);
        regSubCommand(BaitCommand.INSTANCE);
        regSubCommand(LootCommand.INSTANCE);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        return super.onCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        return super.onTabComplete(sender, args);
    }
}
