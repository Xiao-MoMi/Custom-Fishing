package net.momirealms.customfishing.api.mechanic.game;

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

public interface Game {
    
    GamingPlayer start(Player player, FishHook hook, GameSettings settings);
}
