/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class HelpCommand extends AbstractSubCommand {

    public static final HelpCommand INSTANCE = new HelpCommand();

    public HelpCommand() {
        super("help");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        AdventureUtils.sendMessage(sender, "<#4169E1>Command usage:");
        AdventureUtils.sendMessage(sender, "  <gray>├─<#FFFACD><Required Augument> ");
        AdventureUtils.sendMessage(sender, "  <gray>└─<#FFFACD><#E1FFFF>[Optional Augument]");
        AdventureUtils.sendMessage(sender, "<#4169E1>/customfishing");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>help");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>about");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>reload <#87CEFA>Reload the plugin");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>forceopenbag <#FFFACD><player> <#87CEFA>Force a player to open his fishing bag");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>sellshop <#FFFACD><player> <#87CEFA>Force a player to open sell shop");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>competition");
        AdventureUtils.sendMessage(sender, "  <gray>│  ├─<white>start <#FFFACD><id> <#87CEFA>Start a competition");
        AdventureUtils.sendMessage(sender, "  <gray>│  ├─<white>end <#87CEFA>End the ongoing competition");
        AdventureUtils.sendMessage(sender, "  <gray>│  └─<white>cancel <#87CEFA>Cancel the ongoing competition");
        AdventureUtils.sendMessage(sender, "  <gray>├─<white>items");
        AdventureUtils.sendMessage(sender, "  <gray>│  ├─<white>loot");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  ├─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  └─<white>import <#FFFACD><key> <#87CEFA>Import the item in hand");
        AdventureUtils.sendMessage(sender, "  <gray>│  ├─<white>rod");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  └─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│  ├─<white>bait");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│  │  └─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│  └─<white>util");
        AdventureUtils.sendMessage(sender, "  <gray>│     ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>│     └─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtils.sendMessage(sender, "  <gray>└─<white>statistics");
        AdventureUtils.sendMessage(sender, "     <gray>├─<white>reset <#FFFACD><player> <#87CEFA>Reset a player's statistics");
        AdventureUtils.sendMessage(sender, "     <gray>└─<white>set <#FFFACD><player> <id> <value> <#87CEFA>Set certain statistics' value");
        AdventureUtils.sendMessage(sender, "<#4169E1>/fishingbag");
        AdventureUtils.sendMessage(sender, "  <gray>└─<white>open <#E1FFFF>[player] <#87CEFA>Open the fishing bag");
        AdventureUtils.sendMessage(sender, "<#4169E1>/sellshop <#87CEFA>Open the sell shop");
        return true;
    }
}
