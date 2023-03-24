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
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.command.CommandSender;

import java.util.List;

public class AboutCommand extends AbstractSubCommand {

    public static final AboutCommand INSTANCE = new AboutCommand();

    public AboutCommand() {
        super("about");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        AdventureUtils.sendMessage(sender, "<#00BFFF>\uD83C\uDFA3 CustomFishing <gray>- <#87CEEB>" + CustomFishing.getInstance().getVersionHelper().getPluginVersion());
        AdventureUtils.sendMessage(sender, "<#B0C4DE>A fishing plugin that provides innovative mechanics and powerful loot system");
        AdventureUtils.sendMessage(sender, "<#DA70D6>\uD83E\uDDEA Author: <#FFC0CB>XiaoMoMi");
        AdventureUtils.sendMessage(sender, "<#FF7F50>\uD83D\uDD25 Contributors: <#FFA07A>0ft3n<white>, <#FFA07A>Peng_Lx<white>, <#FFA07A>Masaki");
        AdventureUtils.sendMessage(sender, "<#FFD700>⭐ <click:open_url:https://mo-mi.gitbook.io/xiaomomi-plugins/plugin-wiki/customfishing>Document</click> <#A9A9A9>| <#FAFAD2>⛏ <click:open_url:https://github.com/Xiao-MoMi/Custom-Fishing>Github</click> <#A9A9A9>| <#48D1CC>\uD83D\uDD14 <click:open_url:https://polymart.org/resource/customfishing.2723>Polymart</click>");
        return true;
    }
}
