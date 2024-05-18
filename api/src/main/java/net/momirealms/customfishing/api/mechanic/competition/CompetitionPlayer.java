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

import org.jetbrains.annotations.NotNull;

/**
 * Represents a player participating in a fishing competition.
 */
public class CompetitionPlayer implements Comparable<CompetitionPlayer> {

    private final String player;
    private long time;
    private double score;

    /**
     * Constructs a new CompetitionPlayer with the specified player name and initial score.
     *
     * @param player the name of the player.
     * @param score  the initial score of the player.
     */
    public CompetitionPlayer(String player, double score) {
        this.player = player;
        this.score = score;
        this.time = System.currentTimeMillis();
    }

    /**
     * Adds the specified score to the player's current score.
     * If the added score is positive, updates the player's time to the current time.
     *
     * @param score the score to add.
     */
    public void addScore(double score) {
        this.score += score;
        if (score <= 0) return;
        this.time = System.currentTimeMillis();
    }

    /**
     * Sets the player's score to the specified value and updates the player's time to the current time.
     *
     * @param score the new score for the player.
     */
    public void setScore(double score) {
        this.score = score;
        this.time = System.currentTimeMillis();
    }

    /**
     * Gets the time when the player's score was last updated.
     *
     * @return the last update time in milliseconds.
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets the player's current score.
     *
     * @return the current score.
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Gets the name of the player.
     *
     * @return the player's name.
     */
    public String getPlayer() {
        return this.player;
    }

    /**
     * Compares this player to another CompetitionPlayer for ordering.
     * Players are compared first by score, then by time if scores are equal.
     *
     * @param another the other player to compare to.
     */
    @Override
    public int compareTo(@NotNull CompetitionPlayer another) {
        if (another.getScore() != this.score) {
            return (another.getScore() > this.score) ? 1 : -1;
        } else if (another.getTime() != this.time) {
            return (another.getTime() > this.time) ? 1 : -1;
        } else {
            return 0;
        }
    }

    /**
     * Returns a string representation of the CompetitionPlayer.
     *
     * @return a string containing the player's name, score, and last update time.
     */
    @Override
    public String toString() {
        return "CompetitionPlayer[" +
                "time=" + time +
                ", player='" + player + '\'' +
                ", score=" + score +
                ']';
    }
}
