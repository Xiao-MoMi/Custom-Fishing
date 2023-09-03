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
 *
 */

package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.setting.Config;
import net.momirealms.customfishing.setting.Locale;
import net.momirealms.customfishing.storage.method.database.nosql.RedisManager;

import java.util.Set;

public class CompetitionCommand {

    public static CompetitionCommand INSTANCE = new CompetitionCommand();

    public CommandAPICommand getCompetitionCommand() {
        return new CommandAPICommand("competition")
                .withPermission("customfishing.command.competition")
                .withSubcommands(
                        getCompetitionStartCommand(),
                        getCompetitionEndCommand(),
                        getCompetitionStopCommand()
                );
    }

    private CommandAPICommand getCompetitionStartCommand() {
        Set<String> allCompetitions = CustomFishingPlugin.get().getCompetitionManager().getAllCompetitions();
        var command = new CommandAPICommand("start")
                .withArguments(
                    new StringArgument("id")
                        .replaceSuggestions(
                            ArgumentSuggestions.strings(allCompetitions)
                        )
                );
        if (Config.redisRanking) command.withOptionalArguments(new StringArgument("-allservers"));
        command.executes((sender, args) -> {
            String id = (String) args.get(0);
            if (!allCompetitions.contains(id)) {
                AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Competition_Not_Exist.replace("{id}", id));
                return;
            }
            boolean allServer = args.getOrDefault(1, "").equals("-allservers");
            CustomFishingPlugin.get().getCompetitionManager().startCompetition(id, true, allServer);
        });
        return command;
    }

    private CommandAPICommand getCompetitionEndCommand() {
        var command = new CommandAPICommand("end");
        if (Config.redisRanking) command.withOptionalArguments(new StringArgument("-allservers"));
        command.executes((sender, args) -> {
            boolean allServer = args.getOrDefault(1, "").equals("-allservers");
            if (allServer) {
                RedisManager.getInstance().sendRedisMessage("cf_competition", "end");
            } else {
                FishingCompetition competition = CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition();
                if (competition != null) {
                    competition.end();
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_End_Competition);
                } else {
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_No_Competition_Ongoing);
                }
            }
        });
        return command;
    }

    private CommandAPICommand getCompetitionStopCommand() {
        var command = new CommandAPICommand("stop");
        if (Config.redisRanking) command.withOptionalArguments(new StringArgument("-allservers"));
        command.executes((sender, args) -> {
            boolean allServer = args.getOrDefault(1, "").equals("-allservers");
            if (allServer) {
                RedisManager.getInstance().sendRedisMessage("cf_competition", "stop");
            } else {
                FishingCompetition competition = CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition();
                if (competition != null) {
                    competition.stop();
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Stop_Competition);
                } else {
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_No_Competition_Ongoing);
                }
            }
        });
        return command;
    }
}
