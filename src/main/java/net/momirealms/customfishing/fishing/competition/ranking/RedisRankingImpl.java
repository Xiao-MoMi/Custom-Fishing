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

package net.momirealms.customfishing.fishing.competition.ranking;

import net.momirealms.customfishing.fishing.competition.CompetitionPlayer;
import net.momirealms.customfishing.util.JedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.util.Iterator;
import java.util.List;

public class RedisRankingImpl implements RankingInterface {

    @Override
    public void clear() {
        Jedis jedis = JedisUtil.getJedis();
        jedis.zremrangeByRank("cf_competition",0,-1);
        jedis.close();
    }

    @Override
    public CompetitionPlayer getCompetitionPlayer(String player) {
        Jedis jedis = JedisUtil.getJedis();
        Double score = jedis.zscore("cf_competition", player);
        jedis.close();
        if (score == null || score == 0) return null;
        return new CompetitionPlayer(player, Float.parseFloat(score.toString()));
    }

    @Override
    public Iterator<String> getIterator() {
        Jedis jedis = JedisUtil.getJedis();
        List<String> players = jedis.zrevrange("cf_competition", 0, -1);
        jedis.close();
        return players.iterator();
    }

    @Override
    public int getSize() {
        Jedis jedis = JedisUtil.getJedis();
        long size = jedis.zcard("cf_competition");
        jedis.close();
        return (int) size;
    }

    @Override
    public String getPlayerRank(String player) {
        Jedis jedis = JedisUtil.getJedis();
        Long rank = jedis.zrevrank("cf_competition", player);
        jedis.close();
        if(rank == null){
            return null;
        }
        return String.valueOf(rank + 1);
    }

    @Override
    public float getPlayerScore(String player) {
        Jedis jedis = JedisUtil.getJedis();
        Double rank = jedis.zscore("cf_competition", player);
        jedis.close();
        if(rank == null) {
            return 0;
        }
        return rank.floatValue();
    }

    @Override
    public void refreshData(String player, float score) {
        Jedis jedis = JedisUtil.getJedis();
        jedis.zincrby("cf_competition", score, player);
        jedis.close();
    }

    @Override
    public void setData(String player, float score) {
        Jedis jedis = JedisUtil.getJedis();
        jedis.zadd("cf_competition", score, player);
        jedis.close();
    }

    @Override
    public String getPlayerAt(int rank) {
        Jedis jedis = JedisUtil.getJedis();
        List<String> player = jedis.zrevrange("cf_competition", rank - 1, rank -1);
        jedis.close();
        if (player == null) return null;
        if (player.size() == 0) return null;
        return player.get(0);
    }

    @Override
    public float getScoreAt(int rank) {
        Jedis jedis = JedisUtil.getJedis();
        List<Tuple> players = jedis.zrevrangeWithScores("cf_competition", rank - 1, rank -1);
        jedis.close();
        if (players == null) return 0;
        if (players.size() == 0) return 0;
        return (float) players.get(0).getScore();
    }
}
