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

package net.momirealms.customfishing.bukkit.command.feature;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

public class StopCompetitionCommand extends BukkitCommandFeature<CommandSender> {

    public StopCompetitionCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    FishingCompetition competition = BukkitCustomFishingPlugin.getInstance().getCompetitionManager().getOnGoingCompetition();
                    if (competition == null) {
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_FAILURE_NO_COMPETITION);
                    } else {
                        competition.stop(true);
                        handleFeedback(context, MessageConstants.COMMAND_COMPETITION_STOP_SUCCESS);
                    }
                });
    }

    @Override
    public String getFeatureID() {
        return "stop_competition";
    }
}
