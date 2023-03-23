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
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SellShopCommand extends AbstractSubCommand {

    public static final SellShopCommand INSTANCE = new SellShopCommand();

    public SellShopCommand() {
        super("sellshop");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (super.lackArgs(sender, 1, args.size())
                || playerNotOnline(sender, args.get(0))
        ) return true;
        Player player = Bukkit.getPlayer(args.get(0));
        assert player != null;
        player.closeInventory();
        CustomFishing.getInstance().getSellManager().openGuiForPlayer(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return filterStartingWith(online_players(), args.get(0));
        }
        return null;
    }
}
