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
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ImportCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new ImportCommand();

    public ImportCommand() {
        super("import", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1){
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
            return true;
        } else if (sender instanceof Player player) {
            if (ItemStackUtil.saveToFile(player.getInventory().getItemInMainHand(), args.get(0))) {
                AdventureUtil.playerMessage(player, MessageManager.prefix + "Done! File is saved to /CustomFishing/loots/" + args.get(0) + ".yml");
            }
        } else {
            AdventureUtil.consoleMessage(MessageManager.prefix + MessageManager.noConsole);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return Collections.singletonList("<file_name>");
        }
        return super.onTabComplete(sender, args);
    }
}