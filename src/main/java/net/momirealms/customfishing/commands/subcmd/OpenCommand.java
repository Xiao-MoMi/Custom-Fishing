package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class OpenCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new OpenCommand();

    private OpenCommand() {
        super("open", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1) {

        }
        return true;
    }
}
