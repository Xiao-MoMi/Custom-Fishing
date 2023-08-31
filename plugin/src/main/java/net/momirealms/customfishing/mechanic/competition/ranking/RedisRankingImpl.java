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
import net.momirealms.customfishing.storage.method.database.nosql.RedisManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.util.Iterator;
import java.util.List;

public class RedisRankingImpl implements Ranking {

    @Override
    public void clear() {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zremrangeByRank("cf_competition",0,-1);
        }
    }

    @Override
    public CompetitionPlayer getCompetitionPlayer(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            Double score = jedis.zscore("cf_competition", player);
            if (score == null || score == 0) return null;
            return new CompetitionPlayer(player, Float.parseFloat(score.toString()));
        }
    }

    @Override
    public Iterator<Pair<String, Double>> getIterator() {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<Tuple> players = jedis.zrevrangeWithScores("cf_competition", 0, -1);
            return players.stream().map(it -> Pair.of(it.getElement(), it.getScore())).toList().iterator();
        }
    }

    @Override
    public int getSize() {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            long size = jedis.zcard("cf_competition");
            return (int) size;
        }
    }

    @Override
    public int getPlayerRank(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            Long rank = jedis.zrevrank("cf_competition", player);
            if (rank == null)
                return -1;
            return (int) (rank + 1);
        }
    }

    @Override
    public double getPlayerScore(String player) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            Double rank = jedis.zscore("cf_competition", player);
            if (rank == null)
                return 0;
            return rank.floatValue();
        }
    }

    @Override
    public void refreshData(String player, double score) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zincrby("cf_competition", score, player);
        }
    }

    @Override
    public void setData(String player, double score) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            jedis.zadd("cf_competition", score, player);
        }
    }

    @Override
    public String getPlayerAt(int rank) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<String> player = jedis.zrevrange("cf_competition", rank - 1, rank -1);
            if (player == null || player.size() == 0) return null;
            return player.get(0);
        }
    }

    @Override
    public double getScoreAt(int rank) {
        try (Jedis jedis = RedisManager.getInstance().getJedis()) {
            List<Tuple> players = jedis.zrevrangeWithScores("cf_competition", rank - 1, rank -1);
            if (players == null || players.size() == 0) return 0;
            return players.get(0).getScore();
        }
    }
}
