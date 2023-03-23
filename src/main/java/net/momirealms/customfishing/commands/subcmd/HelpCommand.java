package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand extends AbstractSubCommand {

    public static final HelpCommand INSTANCE = new HelpCommand();

    public HelpCommand() {
        super("help");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        AdventureUtil.sendMessage(sender, "<#4169E1>/customfishing");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>reload <#87CEFA>Reload the plugin");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>forceopenbag <player> <#87CEFA>Force a player to open his fishing bag");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>sellshop <player> <#87CEFA>Force a player to open sell shop");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>competition");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>start <id> <#87CEFA>Start a competition");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>end <#87CEFA>End the ongoing competition");
        AdventureUtil.sendMessage(sender, "  <gray>│  └─<white>cancel <#87CEFA>Cancel the ongoing competition");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>items");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>loot");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>get <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>give <player> <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  └─<white>import <key> <#87CEFA>Import the item in hand");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>rod");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>get <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  └─<white>give <player> <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>bait");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>get <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  └─<white>give <player> <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  └─<white>util");
        AdventureUtil.sendMessage(sender, "  <gray>│     ├─<white>et <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│     └─<white>give <player> <id> [amount]");
        AdventureUtil.sendMessage(sender, "  <gray>└─<white>statistics");
        AdventureUtil.sendMessage(sender, "     <gray>├─<white>reset <player> <#87CEFA>Reset a player's statistics");
        AdventureUtil.sendMessage(sender, "     <gray>└─<white>set <player> <id> <value> <#87CEFA>Set certain statistics' value");
        AdventureUtil.sendMessage(sender, "<#4169E1>/fishingbag");
        AdventureUtil.sendMessage(sender, "  <gray>└─<white>open [player] <#87CEFA>Open the fishing bag");
        AdventureUtil.sendMessage(sender, "<#4169E1>/sellshop <#87CEFA>Open the sell shop");
        return true;
    }
}
