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

import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.game.GameInstance;
import net.momirealms.customfishing.api.mechanic.game.GameSettings;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface FishingManager {

    /**
     * Removes a fishing hook entity associated with a given player's UUID.
     *
     * @param uuid The UUID of the player
     * @return {@code true} if the fishing hook was successfully removed, {@code false} otherwise.
     */
    boolean removeHook(UUID uuid);

    /**
     * Retrieves a FishHook object associated with the provided player's UUID
     *
     * @param uuid The UUID of the player
     * @return fishhook entity, null if not exists
     */
    @Nullable FishHook getHook(UUID uuid);

    /**
     * Sets the temporary fishing state for a player.
     *
     * @param player            The player for whom to set the temporary fishing state.
     * @param tempFishingState  The temporary fishing state to set for the player.
     */
    void setTempFishingState(Player player, TempFishingState tempFishingState);

    /**
     * Gets the {@link TempFishingState} object associated with the given UUID.
     *
     * @param uuid The UUID of the player.
     * @return The {@link TempFishingState} object if found, or {@code null} if not found.
     */
    @Nullable TempFishingState getTempFishingState(UUID uuid);

    /**
     * Removes the temporary fishing state associated with a player.
     *
     * @param player The player whose temporary fishing state should be removed.
     */
    @Nullable TempFishingState removeTempFishingState(Player player);

    /**
     * Processes the game result for a gaming player
     *
     * @param gamingPlayer The gaming player whose game result should be processed.
     */
    void processGameResult(GamingPlayer gamingPlayer);

    /**
     * Starts a fishing game for the specified player with the given condition and effect.
     *
     * @param player    The player starting the fishing game.
     * @param condition The condition used to determine the game.
     * @param effect    The effect applied to the game.
     */
    boolean startFishingGame(Player player, Condition condition, Effect effect);

    /**
     * Starts a fishing game for the specified player with the given settings and game instance.
     *
     * @param player       The player starting the fishing game.
     * @param settings     The game settings for the fishing game.
     * @param gameInstance The instance of the fishing game to start.
     */
    boolean startFishingGame(Player player, GameSettings settings, GameInstance gameInstance);

    /**
     * Checks if a player with the given UUID has cast their fishing hook.
     *
     * @param uuid The UUID of the player to check.
     * @return {@code true} if the player has cast their fishing hook, {@code false} otherwise.
     */
    boolean hasPlayerCastHook(UUID uuid);

    /**
     * Gets the {@link GamingPlayer} object associated with the given UUID.
     *
     * @param uuid The UUID of the player.
     * @return The {@link GamingPlayer} object if found, or {@code null} if not found.
     */
    @Nullable GamingPlayer getGamingPlayer(UUID uuid);
}
