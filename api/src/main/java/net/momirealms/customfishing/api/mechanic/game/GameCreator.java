package net.momirealms.customfishing.api.mechanic.game;

import org.bukkit.configuration.ConfigurationSection;

public interface GameCreator {

    Game setArgs(ConfigurationSection section);

}