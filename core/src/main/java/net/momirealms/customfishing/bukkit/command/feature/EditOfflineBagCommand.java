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

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.bukkit.command.BukkitCommandFeature;
import net.momirealms.customfishing.common.command.CustomFishingCommandManager;
import net.momirealms.customfishing.common.locale.MessageConstants;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.parser.standard.UUIDParser;

import java.util.UUID;

public class EditOfflineBagCommand extends BukkitCommandFeature<CommandSender> {

    public EditOfflineBagCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .senderType(Player.class)
                .required("uuid", UUIDParser.uuidParser())
                .handler(context -> {
                    Player admin = context.sender();
                    UUID uuid = context.get("uuid");
                    BukkitCustomFishingPlugin.getInstance().getBagManager().openBag(admin, uuid).whenComplete((result, throwable) -> {
                        if (throwable != null) {
                            handleFeedback(context, MessageConstants.COMMAND_BAG_EDIT_FAILURE_UNSAFE);
                            return;
                        }
                        if (!result) {
                            handleFeedback(context, MessageConstants.COMMAND_BAG_EDIT_FAILURE_NEVER_PLAYED);
                            return;
                        }
                    });
                });
    }

    @Override
    public String getFeatureID() {
        return "edit_offline_bag";
    }
}
