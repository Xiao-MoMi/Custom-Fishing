package net.momirealms.customfishing.object.action;

import net.momirealms.customfishing.ConfigReader;
import org.bukkit.entity.Player;

public record FishingXPB(int amount) implements ActionB {

    @Override
    public void doOn(Player player) {
        if (ConfigReader.Config.skillXP != null){
            ConfigReader.Config.skillXP.addXp(player, amount);
        }
    }
}
