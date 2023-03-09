package net.momirealms.customfishing.commands.subcmd;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.commands.AbstractSubCommand;
import net.momirealms.customfishing.commands.SubCommand;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class StatisticsCommand extends AbstractSubCommand {

    public static final SubCommand INSTANCE = new StatisticsCommand();

    public StatisticsCommand() {
        super("statistics", null);
        regSubCommand(SetCommand.INSTANCE);
        regSubCommand(ResetCommand.INSTANCE);
    }

    public static class SetCommand extends AbstractSubCommand {

        public static final SubCommand INSTANCE = new SetCommand();

        public SetCommand() {
            super("set", null);
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (!ConfigManager.enableStatistics) return true;
            if (args.size() < 3) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return true;
            }
            int amount = Integer.parseInt(args.get(2));
            if (amount < 0) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.negativeStatistics);
                return true;
            }
            if (CustomFishing.getInstance().getLootManager().hasLoot(args.get(1))) {
                Player player = Bukkit.getPlayer(args.get(0));
                if (player != null) {
                    CustomFishing.getInstance().getStatisticsManager().setData(player.getUniqueId(), args.get(1), amount);
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.setStatistics.replace("{Player}", args.get(0)).replace("{Amount}", args.get(2)).replace("{Loot}", args.get(1)));
                }
                else {
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.notOnline.replace("{Player}", args.get(0)));
                }
            }
            else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.statisticsNotExists);
            }
            return true;
        }
        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return online_players().stream()
                        .filter(player_name -> player_name.startsWith(args.get(0)))
                        .collect(Collectors.toList());
            }
            if (args.size() == 2) {
                return CustomFishing.getInstance().getLootManager().getAllLoots().stream()
                        .filter(loot -> loot.getKey().startsWith(args.get(1)) && !loot.isDisableStats())
                        .map(Loot::getKey)
                        .collect(Collectors.toList());
            }
            if (args.size() == 3) {
                return List.of("0","1","2","4","8","16","32","64");
            }
            return super.onTabComplete(sender, args);
        }
    }

    public static class ResetCommand extends AbstractSubCommand {

        public static final SubCommand INSTANCE = new ResetCommand();

        public ResetCommand() {
            super("reset", null);
        }

        @Override
        public boolean onCommand(CommandSender sender, List<String> args) {
            if (!ConfigManager.enableStatistics) return true;
            if (args.size() < 1) {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.lackArgs);
                return true;
            }
            Player player = Bukkit.getPlayer(args.get(0));
            if (player != null) {
                if (CustomFishing.getInstance().getStatisticsManager().reset(player.getUniqueId())) {
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.resetStatistics);
                }
                else {
                    AdventureUtil.sendMessage(sender, MessageManager.prefix + "Internal Error, player's data is not loaded");
                }
            }
            else {
                AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.notOnline.replace("{Player}", args.get(0)));
            }
            return true;
        }

        @Override
        public List<String> onTabComplete(CommandSender sender, List<String> args) {
            if (args.size() == 1) {
                return online_players().stream()
                        .filter(player_name -> player_name.startsWith(args.get(0)))
                        .collect(Collectors.toList());
            }
            return null;
        }
    }
}
