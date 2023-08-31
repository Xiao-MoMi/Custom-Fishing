package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.manager.FishingManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class AbstractGame implements Game {

    public AbstractGame(ConfigurationSection config) {
    }

    @Override
    public abstract GamingPlayer start(Player player, GameSettings settings, FishingManager manager);
}
