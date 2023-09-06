package net.momirealms.customfishing.api.mechanic.game;

import org.bukkit.configuration.ConfigurationSection;

public interface GameFactory {

    GameInstance setArgs(ConfigurationSection section);

}