package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.jetbrains.annotations.Nullable;

public interface GameConfig {

    @Nullable
    Pair<Game, GameSettings> getRandomGame(Effect effect);
}
