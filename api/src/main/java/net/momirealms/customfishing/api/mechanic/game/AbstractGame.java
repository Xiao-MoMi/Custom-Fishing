package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;

import java.util.function.BiFunction;

public abstract class AbstractGame implements Game {

    private final GameBasics basics;
    private final String id;

    public AbstractGame(String id, GameBasics basics) {
        this.basics = basics;
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public GamingPlayer start(CustomFishingHook hook, Effect effect) {
        return gamingPlayerProvider().apply(hook, basics.toGameSetting(effect));
    }

    public abstract BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider();
}
