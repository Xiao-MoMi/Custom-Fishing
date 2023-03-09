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

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class UtilCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new UtilCommand();

    public UtilCommand() {
        super("util", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 2) {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
        }
        else if (args.get(0).equalsIgnoreCase("get")) {
            if (sender instanceof Player player){
                if (!CustomFishing.getInstance().getEffectManager().getUtilItems().containsKey(args.get(1))){
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.itemNotExist);
                    return true;
                }
                if (args.size() == 2){
                    ItemStackUtil.givePlayerUtil(player, args.get(1), 1);
                    AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.getItem.replace("{Amount}", "1").replace("{Item}", args.get(1)));
                } else {
                    if (Integer.parseInt(args.get(2)) < 1){
                        AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.wrongAmount);
                        return true;
                    }
                    ItemStackUtil.givePlayerUtil(player, args.get(1), Integer.parseInt(args.get(2)));
                    AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.getItem.replace("{Amount}", args.get(2)).replace("{Item}", args.get(1)));
                }
            } else {
                AdventureUtil.consoleMessage(MessageManager.prefix + MessageManager.noConsole);
            }
        }
        else if (args.get(0).equalsIgnoreCase("give")) {
            if (args.size() < 3){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return true;
            }
            Player player = Bukkit.getPlayer(args.get(1));
            if (player == null){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.notOnline.replace("{Player}", args.get(1)));
                return true;
            }
            if (!CustomFishing.getInstance().getEffectManager().getUtilItems().containsKey(args.get(2))){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.itemNotExist);
                return true;
            }
            if (args.size() == 3){
                ItemStackUtil.givePlayerUtil(player, args.get(2), 1);
                super.giveItemMsg(sender, args.get(1), args.get(2), 1);
            } else {
                if (Integer.parseInt(args.get(3)) < 1){
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.wrongAmount);
                    return true;
                }
                ItemStackUtil.givePlayerUtil(player, args.get(2), Integer.parseInt(args.get(3)));
                super.giveItemMsg(sender, args.get(1), args.get(2), Integer.parseInt(args.get(3)));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return List.of("get", "give");
        } else if (args.size() == 2) {
            if (args.get(0).equals("get")) {
                return filterStartingWith(utils(), args.get(1));
            } else if (args.get(0).equals("give")) {
                return filterStartingWith(online_players(), args.get(1));
            }
        } else if (args.size() == 3) {
            if (args.get(0).equals("get")) {
                return List.of("1","2","4","8","16","32","64");
            } else if (args.get(0).equals("give")) {
                return filterStartingWith(utils(), args.get(2));
            }
        } else if (args.size() == 4 && args.get(0).equals("give")) {
            return List.of("1","2","4","8","16","32","64");
        }
        return null;
    }

    private List<String> utils() {
        return new ArrayList<>(CustomFishing.getInstance().getEffectManager().getUtilItems().keySet());
    }
}
