package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class BasicGameConfig {

    private int minTime;
    private int maxTime;
    private int minDifficulty;
    private int maxDifficulty;

    public static class Builder {

        private final BasicGameConfig basicGameConfig;

        public Builder() {
            basicGameConfig = new BasicGameConfig();
        }

        public Builder difficulty(int value) {
            basicGameConfig.minDifficulty = (basicGameConfig.maxDifficulty = value);
            return this;
        }

        public Builder difficulty(int min, int max) {
            basicGameConfig.minDifficulty = min;
            basicGameConfig.maxDifficulty = max;
            return this;
        }

        public Builder time(int value) {
            basicGameConfig.minTime = (basicGameConfig.maxTime = value);
            return this;
        }

        public Builder time(int min, int max) {
            basicGameConfig.minTime = min;
            basicGameConfig.maxTime = max;
            return this;
        }

        public BasicGameConfig build() {
            return basicGameConfig;
        }
    }

    @Nullable
    public GameSettings getGameSetting(Effect effect) {
        return new GameSettings(
                ThreadLocalRandom.current().nextInt(minTime, maxTime + 1),
                (int) Math.min(100, Math.max(1, ThreadLocalRandom.current().nextInt(minDifficulty, maxDifficulty + 1) + effect.getDifficultyModifier()))
        );
    }
}