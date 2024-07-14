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
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;

public class OpenBagCommand extends BukkitCommandFeature<CommandSender> {

    public OpenBagCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .required("player", PlayerParser.playerParser())
                .flag(manager.flagBuilder("silent").withAliases("s").build())
                .handler(context -> {
                    final Player player = context.get("player");
                    BukkitCustomFishingPlugin.getInstance().getBagManager().openBag(player, player.getUniqueId()).whenComplete((result, e) -> {
                        if (!result || e != null) {
                            handleFeedback(context, MessageConstants.COMMAND_BAG_OPEN_FAILURE_NOT_LOADED, Component.text(player.getName()));
                        } else {
                            handleFeedback(context, MessageConstants.COMMAND_BAG_OPEN_SUCCESS, Component.text(player.getName()));
                        }
                    });
                });
    }

    @Override
    public String getFeatureID() {
        return "open_bag";
    }
}
