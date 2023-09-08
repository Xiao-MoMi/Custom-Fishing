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

package net.momirealms.customfishing.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.CommandManager;
import net.momirealms.customfishing.command.sub.CompetitionCommand;
import net.momirealms.customfishing.command.sub.DebugCommand;
import net.momirealms.customfishing.command.sub.FishingBagCommand;
import net.momirealms.customfishing.command.sub.ItemCommand;
import net.momirealms.customfishing.setting.Locale;

public class CommandManagerImpl implements CommandManager {

    private final CustomFishingPlugin plugin;

    public CommandManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).silentLogs(true));
    }

    @Override
    public void loadCommands() {
        new CommandAPICommand("customfishing")
                .withAliases("cfishing")
                .withPermission(CommandPermission.OP)
                .withSubcommands(
                        getReloadCommand(),
                        CompetitionCommand.INSTANCE.getCompetitionCommand(),
                        ItemCommand.INSTANCE.getItemCommand(),
                        DebugCommand.INSTANCE.getDebugCommand()
                )
                .register();

        new CommandAPICommand("sellfish")
                .withPermission("customfishing.sellfish")
                .executesPlayer((player, args) -> {
                    plugin.getMarketManager().openMarketGUI(player);
                })
                .register();

        if (plugin.getBagManager().isBagEnabled()) {
            FishingBagCommand.INSTANCE.getBagCommand().register();
        }
    }

    private CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .executes((sender, args) -> {
                    long time = System.currentTimeMillis();
                    plugin.reload();
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, Locale.MSG_Reload.replace("{time}", String.valueOf(System.currentTimeMillis()-time)));
                });
    }
}
