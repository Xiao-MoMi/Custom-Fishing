package net.momirealms.customfishing.object.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.entity.Player;

public record SoundActionImpl(String sound) implements ActionInterface {

    @Override
    public void doOn(Player player, Player another) {
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key(sound), 1, 1);
    }
}
