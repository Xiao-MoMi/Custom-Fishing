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

package net.momirealms.customfishing.competition;

import java.util.TreeSet;

public class Ranking {

    private final TreeSet<CompetitionPlayer> competitionPlayers = new TreeSet<>();

    public void addPlayer(String player, float score) {
        CompetitionPlayer competitionPlayer = new CompetitionPlayer(player, score);
        competitionPlayers.add(competitionPlayer);
    }

    public void addPlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.add(competitionPlayer);
    }

    public void removePlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.removeIf(e -> e == competitionPlayer);
    }

    public void clear() {
        competitionPlayers.clear();
    }

    public boolean contains(CompetitionPlayer competitionPlayer) {
        return competitionPlayers.contains(competitionPlayer);
    }

    public CompetitionPlayer getCompetitionPlayer(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer;
            }
        }
        return null;
    }

    public String getPlayerRank(String player) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return String.valueOf(index);
            }else {
                index++;
            }
        }
        return null;
    }

    public CompetitionPlayer getFirst() {
        return competitionPlayers.first();
    }

    public String getPlayerAt(int i) {
        int count = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (count == i) {
                return competitionPlayer.getPlayer();
            }
            count++;
        }
        return null;
    }

    public float getScoreAt(int i) {
        int count = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (count == i) {
                return competitionPlayer.getScore();
            }
            count++;
        }
        return -1.0f;
    }
}
