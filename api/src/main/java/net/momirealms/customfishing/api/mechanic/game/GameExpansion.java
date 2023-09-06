package net.momirealms.customfishing.api.mechanic.game;

public abstract class GameExpansion {

    public abstract String getVersion();

    public abstract String getAuthor();

    public abstract String getGameType();

    public abstract GameFactory getGameFactory();
}
