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

package net.momirealms.customfishing.command.sub;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.setting.CFLocale;
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
        Set<String> allCompetitions = CustomFishingPlugin.get().getCompetitionManager().getAllCompetitionKeys();
        var command = new CommandAPICommand("start")
                .withArguments(
                    new StringArgument("id")
                        .replaceSuggestions(
                            ArgumentSuggestions.strings(allCompetitions)
                        )
                );
        if (CFConfig.redisRanking) command.withOptionalArguments(new StringArgument("server-group"));
        command.executes((sender, args) -> {
            String id = (String) args.get(0);
            assert id != null;
            if (!allCompetitions.contains(id)) {
                AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Competition_Not_Exist.replace("{id}", id));
                return;
            }
            Object server = args.get("server-group");
            if (server != null) {
                CustomFishingPlugin.get().getCompetitionManager().startCompetition(id, true, (String) server);
            } else {
                CustomFishingPlugin.get().getCompetitionManager().startCompetition(id, true, null);
            }
        });
        return command;
    }

    private CommandAPICommand getCompetitionEndCommand() {
        var command = new CommandAPICommand("end");
        if (CFConfig.redisRanking) command.withOptionalArguments(new StringArgument("server-group"));
        command.executes((sender, args) -> {
            Object server = args.get("server-group");
            if (server != null) {
                RedisManager.getInstance().publishRedisMessage((String) server, "end");
            } else {
                FishingCompetition competition = CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition();
                if (competition != null) {
                    competition.end();
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_End_Competition);
                } else {
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_No_Competition_Ongoing);
                }
            }
        });
        return command;
    }

    private CommandAPICommand getCompetitionStopCommand() {
        var command = new CommandAPICommand("stop");
        if (CFConfig.redisRanking) command.withOptionalArguments(new StringArgument("server-group"));
        command.executes((sender, args) -> {
            Object server = args.get("server-group");
            if (server != null) {
                RedisManager.getInstance().publishRedisMessage((String) server, "stop");
            } else {
                FishingCompetition competition = CustomFishingPlugin.get().getCompetitionManager().getOnGoingCompetition();
                if (competition != null) {
                    competition.stop();
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Stop_Competition);
                } else {
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_No_Competition_Ongoing);
                }
            }
        });
        return command;
    }
}
