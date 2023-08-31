package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.game.Game;
import net.momirealms.customfishing.api.mechanic.game.GameConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

public interface GameManager {


    boolean registerGameType(String type, GameCreator gameCreator);

    boolean unregisterGameType(String type);

    @Nullable GameCreator getGameCreator(String type);

    @Nullable Game getGame(String key);

    @Nullable GameConfig getGameConfig(String key);

    Game getRandomGame();

    GameConfig getRandomGameConfig();

    public interface GameCreator {

        Game setArgs(ConfigurationSection section);
    }
}
