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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.game.BasicGameConfig;
import net.momirealms.customfishing.api.mechanic.game.GameFactory;
import net.momirealms.customfishing.api.mechanic.game.GameInstance;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;

public interface GameManager {


    boolean registerGameType(String type, GameFactory gameFactory);

    boolean unregisterGameType(String type);

    @Nullable GameFactory getGameFactory(String type);

    Optional<Pair<BasicGameConfig, GameInstance>> getGame(String key);

    HashMap<String, Double> getGameWithWeight(Condition condition);
}
