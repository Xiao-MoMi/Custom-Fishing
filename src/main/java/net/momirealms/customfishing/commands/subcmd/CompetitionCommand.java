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
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.manager.CompetitionManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CompetitionCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new CompetitionCommand();

    public CompetitionCommand() {
        super("competition", null);
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (args.size() < 1){
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
            return true;
        }
        if (args.get(0).equals("start")){
            if (args.size() < 2){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return true;
            }
            if (CompetitionSchedule.startCompetition(args.get(1))){
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceSuccess);
            } else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceFailure);
            }
        } else if (args.get(0).equals("end")) {
            CompetitionSchedule.endCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceEnd);
        } else if (args.get(0).equals("cancel")) {
            CompetitionSchedule.cancelCompetition();
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.forceCancel);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            List<String> arrayList = new ArrayList<>();
            for (String cmd : List.of("start","end","cancel")) {
                if (cmd.startsWith(args.get(0)))
                    arrayList.add(cmd);
            }
            return arrayList;
        }
        if (args.size() == 2 && args.get(0).equals("start")) {
            return competitions();
        }
        return super.onTabComplete(sender, args);
    }

    private List<String> competitions() {
        return new ArrayList<>(CompetitionManager.competitionsC.keySet());
    }
}
