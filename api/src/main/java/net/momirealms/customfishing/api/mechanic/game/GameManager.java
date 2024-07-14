/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Interface for managing games.
 */
public interface GameManager extends Reloadable {

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
     * @return The {@link GameFactory} for the specified game type, or {@code null} if not found.
     */
    @Nullable
    GameFactory getGameFactory(String type);

    /**
     * Retrieves a game instance by its identifier.
     *
     * @param id The identifier of the game.
     * @return An {@link Optional} containing the game if found, or an empty {@link Optional} if not found.
     */
    Optional<Game> getGame(String id);

    /**
     * Registers a game instance.
     *
     * @param game The game instance to register.
     * @return {@code true} if the game was successfully registered, {@code false} otherwise.
     */
    boolean registerGame(Game game);

    /**
     * Retrieves the next game to be played based on the specified effect and context.
     *
     * @param effect  The effect influencing the game selection.
     * @param context The context of the player.
     * @return The next game to be played, or {@code null} if no suitable game is found.
     */
    @Nullable
    Game getNextGame(Effect effect, Context<Player> context);
}
