package net.momirealms.customfishing.object.action;

import net.momirealms.customfishing.utils.AdventureUtil;
import org.bukkit.entity.Player;

import java.util.List;

public record MessageB(List<String> messages) implements ActionB {

    @Override
    public void doOn(Player player) {
        messages.forEach(message -> {
            AdventureUtil.playerMessage(player, message);
        });
    }
}
