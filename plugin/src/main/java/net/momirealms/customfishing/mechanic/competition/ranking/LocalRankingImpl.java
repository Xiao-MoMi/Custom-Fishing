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

public class LocalRankingImpl implements Ranking {

    private final Set<CompetitionPlayer> competitionPlayers;

    public LocalRankingImpl() {
        competitionPlayers = Collections.synchronizedSet(new TreeSet<>());
    }

    public void addPlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.add(competitionPlayer);
    }

    public void removePlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.removeIf(e -> e.equals(competitionPlayer));
    }

    @Override
    public void clear() {
        competitionPlayers.clear();
    }

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
    public Iterator<Pair<String, Double>> getIterator() {
        List<Pair<String, Double>> players = new ArrayList<>();
        for (CompetitionPlayer competitionPlayer: competitionPlayers){
            players.add(Pair.of(competitionPlayer.getPlayer(), competitionPlayer.getScore()));
        }
        return players.iterator();
    }

    @Override
    public int getSize() {
        return competitionPlayers.size();
    }

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

    @Override
    public double getPlayerScore(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer.getScore();
            }
        }
        return 0;
    }

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
