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
                        ItemCommand.INSTANCE.getItemCommand()
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
