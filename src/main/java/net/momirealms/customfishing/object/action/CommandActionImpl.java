package net.momirealms.customfishing.object.action;

import net.momirealms.customfishing.CustomFishing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record CommandActionImpl(String[] commands, String nick) implements ActionInterface {

    public CommandActionImpl(String[] commands, @Nullable String nick) {
        this.commands = commands;
        this.nick = nick == null ? "" : nick;
    }

    @Override
    public void doOn(Player player) {
        for (String command : commands) {
            CustomFishing.plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    command.replace("{player}", player.getName())
                            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
                            .replace("{loot}", nick)
                            .replace("{world}", player.getWorld().getName())
            );
        }
    }
}
