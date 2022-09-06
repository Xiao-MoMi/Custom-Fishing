package net.momirealms.customfishing.object.action;

import net.kyori.adventure.key.Key;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.utils.AdventureUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public record XPB(int amount) implements ActionB {

    @Override
    public void doOn(Player player) {
        if (ConfigReader.Config.isSpigot) player.giveExp(amount);
        else player.giveExp(amount, false);
        AdventureUtil.playerSound(player, net.kyori.adventure.sound.Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"));
    }
}
