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

package net.momirealms.customfishing.commands.subcmd.item;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ItemStackUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LootCommand extends AbstractSubCommand {

    public static final LootCommand INSTANCE = new LootCommand();

    public LootCommand() {
        super("loot");
        regSubCommand(GiveCommand.INSTANCE);
        regSubCommand(GetCommand.INSTANCE);
        regSubCommand(ImportCommand.INSTANCE);
    }

    public static class GiveCommand extends AbstractSubCommand {

        public static final GiveCommand INSTANCE = new GiveCommand();

        public GiveCommand() {
            super("give");
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (super.lackArgs(sender, 2, args.size())
                    || playerNotOnline(sender, args.get(0))
                    || itemNotExist(sender, "loot", args.get(1))
            ) return true;
            if (args.size() == 2){
                int amount = ItemStackUtils.givePlayerLoot(Bukkit.getPlayer(args.get(0)), args.get(1), 0);
                if (amount > 0)
                    super.giveItemMsg(sender, args.get(0), args.get(1), amount);
            }
            else if (args.size() == 3) {
                if (Integer.parseInt(args.get(2)) < 1) {
                    AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.wrongAmount);
                    return true;
                }
                ItemStackUtils.givePlayerLoot(Bukkit.getPlayer(args.get(0)), args.get(1), Integer.parseInt(args.get(2)));
                super.giveItemMsg(sender, args.get(0), args.get(1), Integer.parseInt(args.get(2)));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return filterStartingWith(online_players(), args.get(0));
            } else if (args.size() == 2) {
                return filterStartingWith(new ArrayList<>(CustomFishing.getInstance().getLootManager().getAllKeys()), args.get(1));
            } else if (args.size() == 3) {
                return filterStartingWith(List.of("1", "2", "4", "8", "16", "32", "64"), args.get(2));
            }
            return null;
        }
    }

    public static class GetCommand extends AbstractSubCommand {

        public static final GetCommand INSTANCE = new GetCommand();

        public GetCommand() {
            super("get");
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (super.lackArgs(sender, 1, args.size())
                    || super.noConsoleExecute(sender)
                    || super.itemNotExist(sender, "loot", args.get(0))
            ) return true;
            if (args.size() == 1){
                int amount = ItemStackUtils.givePlayerLoot((Player) sender, args.get(0), 0);
                if (amount > 0)
                    super.getItemMsg(sender, args.get(0), amount);
            } else {
                if (Integer.parseInt(args.get(1)) < 1){
                    AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.wrongAmount);
                    return true;
                }
                ItemStackUtils.givePlayerLoot((Player) sender, args.get(0), Integer.parseInt(args.get(1)));
                super.getItemMsg(sender, args.get(0), Integer.parseInt(args.get(1)));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return filterStartingWith(CustomFishing.getInstance().getLootManager().getAllKeys(), args.get(0));
            } else if (args.size() == 2) {
                return filterStartingWith(List.of("1", "2", "4", "8", "16", "32", "64"), args.get(1));
            }
            return null;
        }
    }

    public static class ImportCommand extends AbstractSubCommand {

        public static final ImportCommand INSTANCE = new ImportCommand();

        public ImportCommand() {
            super("import");
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (super.noConsoleExecute(sender)
                    || lackArgs(sender, 1, args.size())
            ) return true;
            Player player = (Player) sender;
            if (ItemStackUtils.saveToFile(player.getInventory().getItemInMainHand(), args.get(0))) AdventureUtils.playerMessage(player, MessageManager.prefix + "Done! File is saved to /CustomFishing/loots/imported.yml");
            else AdventureUtils.playerMessage(player, MessageManager.prefix + "<red>Error. The item can't be null or there already exists loot with that key name");
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return Collections.singletonList("<key>");
            }
            return super.onTabComplete(sender, args);
        }
    }
}
