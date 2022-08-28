package net.momirealms.customfishing.object.action;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public record XPB(int amount) implements ActionB {

    @Override
    public void doOn(Player player) {
        player.giveExp(amount, false);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1,1);
    }
}
