package net.momirealms.customfishing.api.integration;

import org.bukkit.entity.Player;

public interface LevelInterface {

    void addXp(Player player, String target, double amount);
    int getLevel(Player player, String target);
}
