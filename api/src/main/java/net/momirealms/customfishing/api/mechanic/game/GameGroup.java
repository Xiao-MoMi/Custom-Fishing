package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.util.WeightUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GameGroup implements GameConfig {

    private final List<Pair<String, Double>> gamePairs;
    private int minTime;
    private int maxTime;
    private int minDifficulty;
    private int maxDifficulty;

    public GameGroup(List<Pair<String, Double>> gamePairs) {
        this.gamePairs = gamePairs;
    }

    public GameGroup difficulty(int value) {
        minDifficulty = (maxDifficulty = value);
        return this;
    }

    public GameGroup time(int value) {
        minTime = (maxTime = value);
        return this;
    }

    public GameGroup difficulty(int min, int max) {
        minDifficulty = min;
        maxDifficulty = max;
        return this;
    }

    public GameGroup time(int min, int max) {
        minTime = min;
        maxTime = max;
        return this;
    }

    @Override
    @Nullable
    public Pair<Game, GameSettings> getRandomGame(Effect effect) {
        String key = WeightUtils.getRandom(gamePairs);
        Game game = CustomFishingPlugin.get().getGameManager().getGame(key);
        if (game == null) {
            CustomFishingPlugin.get().getLogger().warning(String.format("Game %s doesn't exist!", key));
            return null;
        }
        GameSettings settings = new GameSettings(
                ThreadLocalRandom.current().nextInt(minTime, maxTime + 1),
                (int) (ThreadLocalRandom.current().nextInt(minDifficulty, maxDifficulty + 1) + effect.getDifficultyModifier())
        );
        return Pair.of(game, settings);
    }
}
