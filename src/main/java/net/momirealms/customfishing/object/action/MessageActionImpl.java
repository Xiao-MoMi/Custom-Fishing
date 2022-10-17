package net.momirealms.customfishing.object.action;

import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record MessageActionImpl(String[] messages, String nick) implements ActionInterface {

    public MessageActionImpl(String[] messages, String nick) {
        this.messages = messages;
        this.nick = nick == null ? "" : nick;
    }

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        for (String message : messages) {
            AdventureUtil.playerMessage(player,
                    message.replace("{player}", player.getName())
                            .replace("{world}", player.getWorld().getName())
                            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
                            .replace("{loot}", nick)
                            .replace("{activator}", anotherPlayer == null ? "" : anotherPlayer.getName())
            );
        }
    }
}