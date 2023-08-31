package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.manager.FishingManager;
import org.bukkit.entity.Player;

public interface Game {
    
    GamingPlayer start(Player player, GameSettings settings, FishingManager manager);
}
