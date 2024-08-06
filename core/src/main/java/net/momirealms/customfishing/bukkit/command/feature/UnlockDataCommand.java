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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.parser.standard.EitherParser;
import org.incendo.cloud.parser.standard.UUIDParser;
import org.incendo.cloud.type.Either;

import java.util.UUID;

public class UnlockDataCommand extends BukkitCommandFeature<CommandSender> {

    public UnlockDataCommand(CustomFishingCommandManager<CommandSender> commandManager) {
        super(commandManager);
    }

    @Override
    public Command.Builder<? extends CommandSender> assembleCommand(CommandManager<CommandSender> manager, Command.Builder<CommandSender> builder) {
        return builder
                .flag(manager.flagBuilder("silent").withAliases("s"))
                .required("uuid", EitherParser.eitherParser(UUIDParser.uuidParser(), PlayerParser.playerParser()))
                .handler(context -> {
                    Either<UUID, Player> either = context.get("uuid");
                    UUID uuid = either.primaryOrMapFallback(Entity::getUniqueId);
                    BukkitCustomFishingPlugin.getInstance().getStorageManager().getDataSource().lockOrUnlockPlayerData(uuid, false);
                    handleFeedback(context, MessageConstants.COMMAND_DATA_UNLOCK_SUCCESS, Component.text(uuid.toString()));
                });
    }

    @Override
    public String getFeatureID() {
        return "data_unlock";
    }
}
