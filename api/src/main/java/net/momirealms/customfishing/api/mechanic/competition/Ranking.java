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

import net.momirealms.customfishing.common.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public interface Ranking {

    /**
     * Clears the list of competition players.
     */
    void clear();

    /**
     * Retrieves a competition player by their name.
     *
     * @param player The name of the player to retrieve.
     * @return The CompetitionPlayer object if found, or null if not found.
     */
    @Nullable
    CompetitionPlayer getCompetitionPlayer(String player);

    /**
     * Retrieves a competition player by rank.
     *
     * @param rank The rank of the player.
     * @return The CompetitionPlayer object if found, or null if not found.
     */
    @Nullable
    CompetitionPlayer getCompetitionPlayer(int rank);

    /**
     * Add a player to ranking
     *
     * @param competitionPlayer player
     */
    void addPlayer(CompetitionPlayer competitionPlayer);

    /**
     * Remove a player from ranking
     *
     * @param player player
     */
    void removePlayer(String player);

    /**
     * Returns an iterator for iterating over pairs of player names and scores.
     *
     * @return An iterator for pairs of player names and scores.
     */
    Iterator<Pair<String, Double>> getIterator();

    /**
     * Returns the number of competition players.
     *
     * @return The number of competition players.
     */
    int getSize();

    /**
     * Returns the rank of a player based on their name.
     *
     * @param player The name of the player to get the rank for.
     * @return The rank of the player, or -1 if the player is not found.
     */
    int getPlayerRank(String player);

    /**
     * Returns the score of a player based on their name.
     *
     * @param player The name of the player to get the score for.
     * @return The score of the player, or 0 if the player is not found.
     */
    double getPlayerScore(String player);

    /**
     * Refreshes the data for a player by adding a score to their existing score or creating a new player.
     *
     * @param player The name of the player to update or create.
     * @param score  The score to add to the player's existing score or set as their initial score.
     */
    void refreshData(String player, double score);

    /**
     * Sets the data for a player, updating their score or creating a new player.
     *
     * @param player The name of the player to update or create.
     * @param score  The score to set for the player.
     */
    void setData(String player, double score);

    /**
     * Returns the name of a player at a given index.
     *
     * @param rank The index of the player to retrieve.
     * @return The name of the player at the specified index, or null if not found.
     */
    @Nullable
    String getPlayerAt(int rank);

    /**
     * Returns the score of a player at a given index.
     *
     * @param rank The index of the player to retrieve.
     * @return The score of the player at the specified index, or 0 if not found.
     */
    double getScoreAt(int rank);
}
