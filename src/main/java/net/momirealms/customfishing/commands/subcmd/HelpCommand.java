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
        AdventureUtil.sendMessage(sender, "<#4169E1>Command usage:");
        AdventureUtil.sendMessage(sender, "  <gray>├─<#FFFACD><Required Augument> ");
        AdventureUtil.sendMessage(sender, "  <gray>└─<#FFFACD><#E1FFFF>[Optional Augument]");
        AdventureUtil.sendMessage(sender, "<#4169E1>/customfishing");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>help");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>about");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>reload <#87CEFA>Reload the plugin");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>forceopenbag <#FFFACD><player> <#87CEFA>Force a player to open his fishing bag");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>sellshop <#FFFACD><player> <#87CEFA>Force a player to open sell shop");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>competition");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>start <#FFFACD><id> <#87CEFA>Start a competition");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>end <#87CEFA>End the ongoing competition");
        AdventureUtil.sendMessage(sender, "  <gray>│  └─<white>cancel <#87CEFA>Cancel the ongoing competition");
        AdventureUtil.sendMessage(sender, "  <gray>├─<white>items");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>loot");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  └─<white>import <#FFFACD><key> <#87CEFA>Import the item in hand");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>rod");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  └─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  ├─<white>bait");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  │  └─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│  └─<white>util");
        AdventureUtil.sendMessage(sender, "  <gray>│     ├─<white>get <#FFFACD><id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>│     └─<white>give <#FFFACD><player> <id> <#E1FFFF>[amount]");
        AdventureUtil.sendMessage(sender, "  <gray>└─<white>statistics");
        AdventureUtil.sendMessage(sender, "     <gray>├─<white>reset <#FFFACD><player> <#87CEFA>Reset a player's statistics");
        AdventureUtil.sendMessage(sender, "     <gray>└─<white>set <#FFFACD><player> <id> <value> <#87CEFA>Set certain statistics' value");
        AdventureUtil.sendMessage(sender, "<#4169E1>/fishingbag");
        AdventureUtil.sendMessage(sender, "  <gray>└─<white>open <#E1FFFF>[player] <#87CEFA>Open the fishing bag");
        AdventureUtil.sendMessage(sender, "<#4169E1>/sellshop <#87CEFA>Open the sell shop");
        return true;
    }
}
