package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.jetbrains.annotations.NotNull;

public interface GameBasics {

    int minTime();

    int maxTime();

    int minDifficulty();

    int maxDifficulty();

    static GameBasics.Builder builder() {
        return new GameBasicsImpl.BuilderImpl();
    }

    @NotNull
    GameSetting toGameSetting(Effect effect);

    interface Builder {

        Builder difficulty(int value);

        Builder difficulty(int min, int max);

        Builder time(int value);

        Builder time(int min, int max);

        GameBasics build();
    }
}
