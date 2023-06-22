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


import net.momirealms.customfishing.commands.subcmd.*;
import net.momirealms.customfishing.commands.subcmd.item.ItemsCommand;

public class MainCommand extends AbstractMainCommand {

    public MainCommand() {
        regSubCommand(ReloadCommand.INSTANCE);
        regSubCommand(ItemsCommand.INSTANCE);
        regSubCommand(CompetitionCommand.INSTANCE);
        regSubCommand(SellShopCommand.INSTANCE);
        regSubCommand(OpenBagCommand.INSTANCE);
        regSubCommand(StatisticsCommand.INSTANCE);
        regSubCommand(HelpCommand.INSTANCE);
        regSubCommand(AboutCommand.INSTANCE);
        regSubCommand(DebugCommand.INSTANCE);
        regSubCommand(ConvertCommand.INSTANCE);
    }
}
