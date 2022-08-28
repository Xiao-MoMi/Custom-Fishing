package net.momirealms.customfishing.object.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public record CommandA(List<String> commands, String nick) implements ActionB {
    @Override
    public void doOn(Player player) {
        commands.forEach(command -> {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
                    command.
                            replaceAll("\\{x}", String.valueOf(Math.round(player.getLocation().getX()))).
                            replaceAll("\\{y}", String.valueOf(Math.round(player.getLocation().getY()))).
                            replaceAll("\\{z}", String.valueOf(Math.round(player.getLocation().getZ()))).
                            replaceAll("\\{player}", player.getName()).
                            replaceAll("\\{world}", player.getWorld().getName()).
                            replaceAll("\\{loot}", nick)
            );
        });
    }
}
