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

public interface GameManager {

    /**
     * Registers a new game type with the specified type identifier.
     *
     * @param type         The type identifier for the game.
     * @param gameFactory  The {@link GameFactory} that creates instances of the game.
     * @return {@code true} if the registration was successful, {@code false} if the type identifier is already registered.
     */
    boolean registerGameType(String type, GameFactory gameFactory);

    /**
     * Unregisters a game type with the specified type identifier.
     *
     * @param type The type identifier of the game to unregister.
     * @return {@code true} if the game type was successfully unregistered, {@code false} if the type identifier was not found.
     */
    boolean unregisterGameType(String type);

    /**
     * Retrieves the game factory associated with the specified game type.
     *
     * @param type The type identifier of the game.
     * @return The {@code GameFactory} for the specified game type, or {@code null} if not found.
     */
    @Nullable GameFactory getGameFactory(String type);

    /**
     * Retrieves a game instance and its basic configuration associated with the specified key.
     *
     * @param key The key identifying the game instance.
     * @return An {@code Optional} containing a {@code Pair} of the basic game configuration and the game instance
     *         if found, or an empty {@code Optional} if not found.
     */
    @Nullable Pair<BasicGameConfig, GameInstance> getGameInstance(String key);

    /**
     * Retrieves a map of game names and their associated weights based on the specified conditions.
     *
     * @param condition The condition to evaluate game weights.
     * @return A {@code HashMap} containing game names as keys and their associated weights as values.
     */
    HashMap<String, Double> getGameWithWeight(Condition condition);
}
