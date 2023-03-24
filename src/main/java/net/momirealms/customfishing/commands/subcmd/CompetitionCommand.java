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
import net.momirealms.customfishing.fishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CompetitionCommand extends AbstractSubCommand {

    public static final CompetitionCommand INSTANCE = new CompetitionCommand();

    public CompetitionCommand() {
        super("competition");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (!ConfigManager.enableCompetition || super.lackArgs(sender, 1, args.size())) return true;
        switch (args.get(0)) {
            case "start" -> {
                if (args.size() < 2) {
                    AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                    return true;
                }
                if (CompetitionSchedule.startCompetition(args.get(1))) AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.forceSuccess);
                else AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.forceFailure);
            }
            case "end" -> {
                CompetitionSchedule.endCompetition();
                AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.forceEnd);
            }
            case "cancel" -> {
                CompetitionSchedule.cancelCompetition();
                AdventureUtils.sendMessage(sender, MessageManager.prefix + MessageManager.forceCancel);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        List<String> completions = new ArrayList<>();
        if (args.size() == 1) {
            for (String cmd : List.of("start", "end", "cancel")) {
                if (cmd.startsWith(args.get(0))) {
                    completions.add(cmd);
                }
            }
        } else if (args.size() == 2 && args.get(0).equals("start")) {
            for (String cmd : competitions()) {
                if (cmd.startsWith(args.get(1))) {
                    completions.add(cmd);
                }
            }
        }
        return completions.isEmpty() ? null : completions;
    }

    private List<String> competitions() {
        return new ArrayList<>(CustomFishing.getInstance().getCompetitionManager().getCompetitionsC().keySet());
    }
}
