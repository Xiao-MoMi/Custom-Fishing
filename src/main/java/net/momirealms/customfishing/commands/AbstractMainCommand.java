package net.momirealms.customfishing.commands;

import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMainCommand implements TabExecutor {

    protected final Map<String, AbstractSubCommand> subCommandMap;

    public AbstractMainCommand() {
        this.subCommandMap = new ConcurrentHashMap<>();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() < 1) {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.nonArgs);
            return true;
        }
        AbstractSubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onCommand(sender, argList.subList(1, argList.size()));
        else {
            AdventureUtil.sendMessage(sender, MessageManager.prefix + MessageManager.unavailableArgs);
            return true;
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> argList = Arrays.asList(args);
        if (argList.size() <= 1) {
            List<String> returnList = new ArrayList<>(subCommandMap.keySet());
            returnList.removeIf(str -> !str.startsWith(args[0]));
            return returnList;
        }
        AbstractSubCommand subCommand = subCommandMap.get(argList.get(0));
        if (subCommand != null)
            return subCommand.onTabComplete(sender, argList.subList(1, argList.size()));
        else
            return Collections.singletonList("");
    }

    public void regSubCommand(AbstractSubCommand executor) {
        subCommandMap.put(executor.getSubCommand(), executor);
    }

    public Map<String, AbstractSubCommand> getSubCommandMap() {
        return subCommandMap;
    }
}
