package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.biomeapi.BiomeAPI;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.integration.SeasonInterface;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DebugCommand extends AbstractSubCommand {

    public static final DebugCommand INSTANCE = new DebugCommand();

    public DebugCommand() {
        super("debug");
    }

    @Override
    public boolean onCommand(CommandSender sender, List<String> args) {
        if (lackArgs(sender, 1, args.size()) || noConsoleExecute(sender)) return true;
        Player player = (Player) sender;
        switch (args.get(0)) {
            case "biome" -> {
                sender.sendMessage(BiomeAPI.getBiome(player.getLocation()));
            }
            case "time" -> {
                sender.sendMessage(String.valueOf(player.getWorld().getTime()));
            }
            case "world" -> {
                sender.sendMessage(player.getWorld().getName());
            }
            case "season" -> {
                SeasonInterface seasonInterface = CustomFishing.getInstance().getIntegrationManager().getSeasonInterface();
                if (seasonInterface == null) return true;
                sender.sendMessage(seasonInterface.getSeason(player.getLocation().getWorld()));
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, List<String> args) {
        if (args.size() == 1) {
            return filterStartingWith(List.of("biome", "time", "world", "season"), args.get(0));
        }
        return null;
    }
}
