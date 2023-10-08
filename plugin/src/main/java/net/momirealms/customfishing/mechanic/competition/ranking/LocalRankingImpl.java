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

package net.momirealms.customfishing.mechanic.competition.ranking;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionPlayer;
import net.momirealms.customfishing.api.mechanic.competition.Ranking;

import java.util.*;

/**
 * Implementation of the Ranking interface that manages the ranking of competition players locally.
 */
public class LocalRankingImpl implements Ranking {

    private final Set<CompetitionPlayer> competitionPlayers;

    public LocalRankingImpl() {
        competitionPlayers = Collections.synchronizedSet(new TreeSet<>());
    }

    /**
     * Adds a competition player to the ranking.
     *
     * @param competitionPlayer The CompetitionPlayer to add.
     */
    @Override
    public void addPlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.add(competitionPlayer);
    }

    /**
     * Removes a competition player from the ranking.
     *
     * @param player player's name
     */
    @Override
    public void removePlayer(String player) {
        competitionPlayers.removeIf(e -> e.getPlayer().equals(player));
    }

    /**
     * Removes a competition player from the ranking.
     *
     * @param competitionPlayer The CompetitionPlayer to remove.
     */
    public void removePlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.removeIf(e -> e.equals(competitionPlayer));
    }

    /**
     * Clears the list of competition players.
     */
    @Override
    public void clear() {
        competitionPlayers.clear();
    }

    /**
     * Retrieves a competition player by their name.
     *
     * @param player The name of the player to retrieve.
     * @return The CompetitionPlayer object if found, or null if not found.
     */
    @Override
    public CompetitionPlayer getCompetitionPlayer(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer;
            }
        }
        return null;
    }

    @Override
    public CompetitionPlayer getCompetitionPlayer(int rank) {
        int i = 1;
        int size = getSize();
        if (rank > size) return null;
        for (CompetitionPlayer player : competitionPlayers) {
            if (rank == i) {
                return player;
            }
            i++;
        }
        return null;
    }

    /**
     * Returns an iterator for iterating over pairs of player names and scores.
     *
     * @return An iterator for pairs of player names and scores.
     */
    @Override
    public Iterator<Pair<String, Double>> getIterator() {
        List<Pair<String, Double>> players = new ArrayList<>();
        for (CompetitionPlayer competitionPlayer: competitionPlayers){
            players.add(Pair.of(competitionPlayer.getPlayer(), competitionPlayer.getScore()));
        }
        return players.iterator();
    }

    /**
     * Returns the number of competition players.
     *
     * @return The number of competition players.
     */
    @Override
    public int getSize() {
        return competitionPlayers.size();
    }

    /**
     * Returns the rank of a player based on their name.
     *
     * @param player The name of the player to get the rank for.
     * @return The rank of the player, or -1 if the player is not found.
     */
    @Override
    public int getPlayerRank(String player) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return index;
            }else {
                index++;
            }
        }
        return -1;
    }

    /**
     * Returns the score of a player based on their name.
     *
     * @param player The name of the player to get the score for.
     * @return The score of the player, or 0 if the player is not found.
     */
    @Override
    public double getPlayerScore(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer.getScore();
            }
        }
        return 0;
    }

    /**
     * Returns the name of a player at a given index.
     *
     * @param i The index of the player to retrieve.
     * @return The name of the player at the specified index, or null if not found.
     */
    @Override
    public String getPlayerAt(int i) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (index == i) {
                return competitionPlayer.getPlayer();
            }
            index++;
        }
        return null;
    }

    /**
     * Returns the score of a player at a given index.
     *
     * @param i The index of the player to retrieve.
     * @return The score of the player at the specified index, or 0 if not found.
     */
    @Override
    public double getScoreAt(int i) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (index == i) {
                return competitionPlayer.getScore();
            }
            index++;
        }
        return 0f;
    }

    /**
     * Refreshes the data for a player by adding a score to their existing score or creating a new player.
     *
     * @param player The name of the player to update or create.
     * @param score  The score to add to the player's existing score or set as their initial score.
     */
    @Override
    public void refreshData(String player, double score) {
        CompetitionPlayer competitionPlayer = getCompetitionPlayer(player);
        if (competitionPlayer != null) {
            removePlayer(competitionPlayer);
            competitionPlayer.addScore(score);
            addPlayer(competitionPlayer);
        } else {
            competitionPlayer = new CompetitionPlayer(player, score);
            addPlayer(competitionPlayer);
        }
    }

    /**
     * Sets the data for a player, updating their score or creating a new player.
     *
     * @param player The name of the player to update or create.
     * @param score  The score to set for the player.
     */
    @Override
    public void setData(String player, double score) {
        CompetitionPlayer competitionPlayer = getCompetitionPlayer(player);
        if (competitionPlayer != null) {
            removePlayer(competitionPlayer);
            competitionPlayer.setScore(score);
            addPlayer(competitionPlayer);
        } else {
            competitionPlayer = new CompetitionPlayer(player, score);
            addPlayer(competitionPlayer);
        }
    }
}
