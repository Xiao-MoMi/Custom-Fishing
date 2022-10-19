package net.momirealms.customfishing.commands;

import net.momirealms.customfishing.commands.subcmd.OpenCommand;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingBagCommand implements TabExecutor {

    private final Map<String, SubCommand> subCommandMap;

    public FishingBagCommand() {
        subCommandMap = new ConcurrentHashMap<>();
        regDefaultSubCommands();
    }

    private void regDefaultSubCommands() {
        regSubCommand(OpenCommand.INSTANCE);
    }

    public void regSubCommand(SubCommand executor) {
        subCommandMap.put(executor.getSubCommand(), executor);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.nonArgs);
            return true;
        }
        SubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onCommand(sender, argList.subList(1, argList.size()));
        else {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.unavailableArgs);
            return true;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() <= 1) {
            List<String> returnList = new ArrayList<>(subCommandMap.keySet());
            returnList.removeIf(str -> !str.startsWith(args[0]));
            return returnList;
        }
        SubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onTabComplete(sender, argList.subList(1, argList.size()));
        else
            return Collections.singletonList("");
    }

    public Map<String, SubCommand> getSubCommandMap() {
        return subCommandMap;
    }
}
