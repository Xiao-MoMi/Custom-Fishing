/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

import net.kyori.adventure.text.Component;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.statistic.FishingStatistics;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.parser.standard.EnumParser;

public class QueryStatisticsCommand extends BukkitCommandFeature<CommandSender> {

    public QueryStatisticsCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s"))
                .required("player", PlayerParser.playerParser())
                .required("type", EnumParser.enumParser(FishingStatistics.Type.class))
                .handler(context -> {
                    Player player = context.get("player");
                    FishingStatistics.Type type = context.get("type");
                    BukkitCustomFishingPlugin.getInstance().getStorageManager().getOnlineUser(player.getUniqueId()).ifPresentOrElse(userData -> {
                        if (type == FishingStatistics.Type.AMOUNT_OF_FISH_CAUGHT) {
                            handleFeedback(context, MessageConstants.COMMAND_STATISTICS_QUERY_AMOUNT, Component.text(userData.statistics().amountMap().toString()));
                        } else if (type == FishingStatistics.Type.MAX_SIZE) {
                            handleFeedback(context, MessageConstants.COMMAND_STATISTICS_QUERY_SIZE, Component.text(userData.statistics().sizeMap().toString()));
                        }
                    }, () -> handleFeedback(context, MessageConstants.COMMAND_STATISTICS_FAILURE_NOT_LOADED));
                });
    }

    @Override
    public String getFeatureID() {
        return "statistics_query";
    }
}
