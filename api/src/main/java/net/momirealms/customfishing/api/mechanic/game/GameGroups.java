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

public class GameGroups implements GameConfig {

    private final List<Pair<String, Double>> gamesWithWeight;

    public GameGroups(List<Pair<String, Double>> gamesWithWeight) {
        this.gamesWithWeight = gamesWithWeight;
    }

    @Override
    public @Nullable Pair<GameInstance, GameSettings> getRandomGame(Effect effect) {
        String group = WeightUtils.getRandom(gamesWithWeight);
        GameConfig gameConfig = CustomFishingPlugin.get().getGameManager().getGameConfig(group);
        if (gameConfig == null) {
            CustomFishingPlugin.get().getLogger().warning(String.format("Game config %s doesn't exist!", group));
            return null;
        }
        if (!(gameConfig instanceof GameGroup gameGroup)) {
            CustomFishingPlugin.get().getLogger().warning(String.format("%s is not a game group!", group));
            return null;
        }
        return gameGroup.getRandomGame(effect);
    }
}
