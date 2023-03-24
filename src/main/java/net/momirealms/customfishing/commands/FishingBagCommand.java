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

package net.momirealms.customfishing.commands;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class FishingBagCommand extends AbstractMainCommand {

    public FishingBagCommand() {
        regSubCommand(OpenCommand.INSTANCE);
    }

    public static class OpenCommand extends AbstractSubCommand {

        public static final OpenCommand INSTANCE = new OpenCommand();

        private OpenCommand() {
            super("open");
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (super.noConsoleExecute(sender)) return true;
            Player player = (Player) sender;
            if (args.size() == 0) {
                if (!sender.hasPermission("fishingbag.open")) {
                    AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.noPerm);
                    return true;
                }
                player.closeInventory();
                CustomFishing.getInstance().getBagDataManager().openFishingBag(player, player, false);
            }
            if (args.size() >= 1) {
                if (!sender.hasPermission("customfishing.admin")) {
                    AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.noPerm);
                    return true;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayerIfCached(args.get(0));
                if (offlinePlayer == null) {
                    AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.playerNotExist);
                    return true;
                }
                player.closeInventory();
                CustomFishing.getInstance().getBagDataManager().openFishingBag(player, offlinePlayer, args.size() >= 2 && args.get(1).equals("--force"));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (ConfigManager.enableFishingBag && sender.hasPermission("customfishing.admin") && args.size() == 1) {
                return filterStartingWith(online_players(), args.get(0));
            }
            return null;
        }
    }
}
