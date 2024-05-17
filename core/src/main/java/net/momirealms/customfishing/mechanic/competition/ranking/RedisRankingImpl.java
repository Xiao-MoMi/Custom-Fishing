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
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.storage.method.database.nosql.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.util.Iterator;
import java.util.List;

public class RedisRankingImpl implements Ranking {

    /**
     * Clears the ranking data by removing all players and scores.
     */
    @Override
    public void clear() {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.del("cf_competition_" + CFConfig.serverGroup);
        }
    }

    /**
     * Retrieves a competition player by their name from the Redis ranking.
     *
     * @param player The name of the player to retrieve.
     * @return The CompetitionPlayer object if found, or null if not found.
     */
    @Override
    public CompetitionPlayer getCompetitionPlayer(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            Double score = jedis.zscore("cf_competition_" + CFConfig.serverGroup, player);
            if (score == null || score == 0) return null;
            return new CompetitionPlayer(player, Float.parseFloat(score.toString()));
        }
    }

    @Override
    public CompetitionPlayer getCompetitionPlayer(int rank) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<Tuple> player = jedis.zrevrangeWithScores("cf_competition_" + CFConfig.serverGroup, rank - 1, rank -1);
            if (player == null || player.size() == 0) return null;
            return new CompetitionPlayer(player.get(0).getElement(), player.get(0).getScore());
        }
    }

    @Override
    public void addPlayer(CompetitionPlayer competitionPlayer) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zincrby("cf_competition_" + CFConfig.serverGroup, competitionPlayer.getScore(), competitionPlayer.getPlayer());
        }
    }

    @Override
    public void removePlayer(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zrem("cf_competition_" + CFConfig.serverGroup, player);
        }
    }

    /**
     * Returns an iterator for iterating over pairs of player names and scores in descending order.
     *
     * @return An iterator for pairs of player names and scores.
     */
    @Override
    public Iterator<Pair<String, Double>> getIterator() {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<Tuple> players = jedis.zrevrangeWithScores("cf_competition_" + CFConfig.serverGroup, 0, -1);
            return players.stream().map(it -> Pair.of(it.getElement(), it.getScore())).toList().iterator();
        }
    }

    /**
     * Returns the number of players in the Redis ranking.
     *
     * @return The number of players in the ranking.
     */
    @Override
    public int getSize() {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            long size = jedis.zcard("cf_competition_" + CFConfig.serverGroup);
            return (int) size;
        }
    }

    /**
     * Returns the rank of a player based on their name in descending order (1-based).
     *
     * @param player The name of the player to get the rank for.
     * @return The rank of the player, or -1 if the player is not found.
     */
    @Override
    public int getPlayerRank(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            Long rank = jedis.zrevrank("cf_competition_" + CFConfig.serverGroup, player);
            if (rank == null)
                return -1;
            return (int) (rank + 1);
        }
    }

    /**
     * Returns the score of a player based on their name from the Redis ranking.
     *
     * @param player The name of the player to get the score for.
     * @return The score of the player, or 0 if the player is not found.
     */
    @Override
    public double getPlayerScore(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            Double rank = jedis.zscore("cf_competition_" + CFConfig.serverGroup, player);
            if (rank == null)
                return 0;
            return rank.floatValue();
        }
    }

    /**
     * Refreshes the data for a player in the Redis ranking by adding a score to their existing score or creating a new player.
     *
     * @param player The name of the player to update or create.
     * @param score  The score to add to the player's existing score or set as their initial score.
     */
    @Override
    public void refreshData(String player, double score) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zincrby("cf_competition_" + CFConfig.serverGroup, score, player);
        }
    }

    /**
     * Sets the data for a player in the Redis ranking, updating their score or creating a new player.
     *
     * @param player The name of the player to update or create.
     * @param score  The score to set for the player.
     */
    @Override
    public void setData(String player, double score) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zadd("cf_competition_" + CFConfig.serverGroup, score, player);
        }
    }

    /**
     * Returns the name of the player at a given rank in descending order.
     *
     * @param rank The rank of the player to retrieve (1-based).
     * @return The name of the player at the specified rank, or null if not found.
     */
    @Override
    public String getPlayerAt(int rank) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<String> player = jedis.zrevrange("cf_competition_" + CFConfig.serverGroup, rank - 1, rank -1);
            if (player == null || player.size() == 0) return null;
            return player.get(0);
        }
    }

    /**
     * Returns the score of the player at a given rank in descending order.
     *
     * @param rank The rank of the player to retrieve (1-based).
     * @return The score of the player at the specified rank, or 0 if not found.
     */
    @Override
    public double getScoreAt(int rank) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<Tuple> players = jedis.zrevrangeWithScores("cf_competition_" + CFConfig.serverGroup, rank - 1, rank -1);
            if (players == null || players.size() == 0) return 0;
            return players.get(0).getScore();
        }
    }
}
