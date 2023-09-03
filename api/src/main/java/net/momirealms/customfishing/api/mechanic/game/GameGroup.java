/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
