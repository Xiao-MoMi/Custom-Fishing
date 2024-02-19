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

package net.momirealms.customfishing.api.mechanic.competition;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface FishingCompetition {

    /**
     * Start the fishing competition
     */
    void start();

    /**
     * Stop the fishing competition
     */
    void stop(boolean triggerEvent);

    /**
     * End the fishing competition
     */
    void end(boolean triggerEvent);

    /**
     * Check if the fishing competition is ongoing.
     *
     * @return {@code true} if the competition is still ongoing, {@code false} if it has ended.
     */
    boolean isOnGoing();

    /**
     * Refreshes the data for a player in the fishing competition, including updating their score and triggering
     * actions if it's their first time joining the competition.
     *
     * @param player The player whose data needs to be refreshed.
     * @param score The player's current score in the competition.
     */
    void refreshData(Player player, double score);

    /**
     * Checks if a player has joined the fishing competition based on their name.
     *
     * @param player The player to check for participation.
     * @return {@code true} if the player has joined the competition; {@code false} otherwise.
     */
    boolean hasPlayerJoined(OfflinePlayer player);

    /**
     * Gets the progress of the fishing competition as a float value (0~1).
     *
     * @return The progress of the fishing competition as a float.
     */
    float getProgress();

    /**
     * Gets the remaining time in seconds for the fishing competition.
     *
     * @return The remaining time in seconds.
     */
    long getRemainingTime();

    /**
     * Gets the start time of the fishing competition.
     *
     * @return The start time of the fishing competition.
     */
    long getStartTime();

    /**
     * Gets the configuration of the fishing competition.
     *
     * @return The configuration of the fishing competition.
     */
    @NotNull CompetitionConfig getConfig();

    /**
     * Gets the goal of the fishing competition.
     *
     * @return The goal of the fishing competition.
     */
    @NotNull CompetitionGoal getGoal();

    /**
     * Gets the ranking data for the fishing competition.
     *
     * @return The ranking data for the fishing competition.
     */
    @NotNull Ranking getRanking();

    /**
     * Gets the cached placeholders for the fishing competition.
     *
     * @return A ConcurrentHashMap containing cached placeholders.
     */
    @NotNull Map<String, String> getCachedPlaceholders();

    /**
     * Gets a specific cached placeholder value by its key.
     *
     * @param papi The key of the cached placeholder.
     * @return The cached placeholder value as a string, or null if not found.
     */
    @Nullable String getCachedPlaceholder(String papi);
}
