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
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.UUIDArgument;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.CommandManager;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.command.sub.*;
import net.momirealms.customfishing.setting.CFLocale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Collection;
import java.util.UUID;

public class CommandManagerImpl implements CommandManager {

    private final CustomFishingPlugin plugin;

    public CommandManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        if (!CommandAPI.isLoaded())
            CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin).silentLogs(true));
    }

    @Override
    public void load() {
        new CommandAPICommand("customfishing")
                .withAliases("cfishing")
                .withPermission("customfishing.admin")
                .withSubcommands(
                        getReloadCommand(),
                        getMarketCommand(),
                        getAboutCommand(),
                        GUIEditorCommand.INSTANCE.getEditorCommand(),
                        DataCommand.INSTANCE.getDataCommand(),
                        CompetitionCommand.INSTANCE.getCompetitionCommand(),
                        ItemCommand.INSTANCE.getItemCommand(),
                        DebugCommand.INSTANCE.getDebugCommand(),
                        StatisticsCommand.INSTANCE.getStatisticsCommand()
                )
                .register();

        if (plugin.getMarketManager().isEnable()) {
            new CommandAPICommand("sellfish")
                    .withPermission("customfishing.sellfish")
                    .executesPlayer((player, args) -> {
                        if (plugin.getMarketManager().isEnable())
                            plugin.getMarketManager().openMarketGUI(player);
                    })
                    .register();
        }

        if (plugin.getBagManager().isEnabled()) {
            FishingBagCommand.INSTANCE.getBagCommand().register();
        }
    }

    @Override
    public void unload() {
    }

    private CommandAPICommand getReloadCommand() {
        return new CommandAPICommand("reload")
                .executes((sender, args) -> {
                    long time = System.currentTimeMillis();
                    plugin.reload();
                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Reload.replace("{time}", String.valueOf(System.currentTimeMillis()-time)));
                });
    }

    @SuppressWarnings("unchecked")
    private CommandAPICommand getMarketCommand() {
        CommandAPICommand command = new CommandAPICommand("open");
        if (plugin.getMarketManager().isEnable()) {
            command.withSubcommands(
                    new CommandAPICommand("market")
                    .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                    .executes((sender, args) -> {
                        Collection<Player> players = (Collection<Player>) args.get("player");
                        assert players != null;
                        for (Player player : players) {
                            plugin.getMarketManager().openMarketGUI(player);
                            AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Market_GUI_Open.replace("{player}", player.getName()));
                        }
                    }),
                    new CommandAPICommand("market-uuid")
                            .withArguments(new UUIDArgument("uuid"))
                            .executes((sender, args) -> {
                                UUID uuid = (UUID) args.get("uuid");
                                Player player = Bukkit.getPlayer(uuid);
                                if (player == null) return;
                                plugin.getMarketManager().openMarketGUI(player);
                                AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Market_GUI_Open.replace("{player}", player.getName()));
                            })
            );
        }
        if (plugin.getBagManager().isEnabled()) {
            command.withSubcommands(
                    new CommandAPICommand("bag")
                    .withArguments(new EntitySelectorArgument.ManyPlayers("player"))
                    .executes((sender, args) -> {
                        Collection<Player> players = (Collection<Player>) args.get("player");
                        assert players != null;
                        for (Player player : players) {
                            Inventory inventory = plugin.getBagManager().getOnlineBagInventory(player.getUniqueId());
                            if (inventory != null) {
                                player.openInventory(inventory);
                                AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Fishing_Bag_Open.replace("{player}", player.getName()));
                            } else {
                                LogUtils.warn("Player " + player.getName() + "'s bag data has not been loaded.");
                            }
                        }
                    }),
                    new CommandAPICommand("bag-uuid")
                            .withArguments(new UUIDArgument("uuid"))
                            .executes((sender, args) -> {
                                UUID uuid = (UUID) args.get("uuid");
                                Player player = Bukkit.getPlayer(uuid);
                                if (player == null) return;
                                Inventory inventory = plugin.getBagManager().getOnlineBagInventory(uuid);
                                if (inventory != null) {
                                    player.openInventory(inventory);
                                    AdventureManagerImpl.getInstance().sendMessageWithPrefix(sender, CFLocale.MSG_Fishing_Bag_Open.replace("{player}", player.getName()));
                                } else {
                                    LogUtils.warn("Player " + player.getName() + "'s bag data has not been loaded.");
                                }
                            })
            );
        }
        return command;
    }

    private CommandAPICommand getAboutCommand() {
        return new CommandAPICommand("about").executes((sender, args) -> {
            AdventureManagerImpl.getInstance().sendMessage(sender, "<#00BFFF>\uD83C\uDFA3 CustomFishing <gray>- <#87CEEB>" + CustomFishingPlugin.getInstance().getVersionManager().getPluginVersion());
            AdventureManagerImpl.getInstance().sendMessage(sender, "<#B0C4DE>A fishing plugin that provides innovative mechanics and powerful loot system");
            AdventureManagerImpl.getInstance().sendMessage(sender, "<#DA70D6>\uD83E\uDDEA Author: <#FFC0CB>XiaoMoMi");
            AdventureManagerImpl.getInstance().sendMessage(sender, "<#FF7F50>\uD83D\uDD25 Contributors: <#FFA07A>0ft3n<white>, <#FFA07A>Peng_Lx<white>, <#FFA07A>Masaki<white>, <#FFA07A>g2213swo");
            AdventureManagerImpl.getInstance().sendMessage(sender, "<#FFD700>⭐ <click:open_url:https://mo-mi.gitbook.io/xiaomomi-plugins/plugin-wiki/customfishing>Document</click> <#A9A9A9>| <#FAFAD2>⛏ <click:open_url:https://github.com/Xiao-MoMi/Custom-Fishing>Github</click> <#A9A9A9>| <#48D1CC>\uD83D\uDD14 <click:open_url:https://polymart.org/resource/customfishing.2723>Polymart</click>");
        });
    }
}
