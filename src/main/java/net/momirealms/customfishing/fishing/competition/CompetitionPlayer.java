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

package net.momirealms.customfishing.fishing.competition;

import org.jetbrains.annotations.NotNull;

public class CompetitionPlayer implements Comparable<CompetitionPlayer>{

    private long time;
    private final String player;
    private float score;

    public static CompetitionPlayer emptyPlayer = new CompetitionPlayer(null, 0);

    public CompetitionPlayer(String player, float score) {
        this.player = player;
        this.score = score;
        this.time = System.currentTimeMillis();
    }

    public void addScore(float score){
        this.score += score;
        this.time = System.currentTimeMillis();
    }

    public void setScore(float score){
        this.score = score;
        this.time = System.currentTimeMillis();
    }

    public float getScore() {
        return this.score;
    }

    public String getPlayer(){
        return this.player;
    }

    @Override
    public int compareTo(@NotNull CompetitionPlayer competitionPlayer) {
        if (competitionPlayer.getScore() != this.score) {
            return (competitionPlayer.getScore() > this.score) ? 1 : -1;
        } else {
            return (competitionPlayer.getScore() > this.time) ? 1 : -1;
        }
    }

    @Override
    public String toString() {
        return "CompetitionPlayer{" +
                "time=" + time +
                ", player='" + player + '\'' +
                ", score=" + score +
                '}';
    }
}
