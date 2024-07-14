/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.competition;

public class CompetitionSchedule {

    private final int weekday;
    private final int hour;
    private final int minute;
    private final int second;

    public CompetitionSchedule(int weekday, int hour, int minute, int second) {
        this.weekday = weekday;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
    }

    /**
     * Gets the weekday associated with this time schedule.
     *
     * @return The weekday value (e.g., 1 for Monday, 2 for Tuesday, etc.).
     */
    public int getWeekday() {
        return weekday;
    }

    /**
     * Gets the hour of the day associated with this time schedule.
     *
     * @return The hour value (0-23).
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute of the hour associated with this time schedule.
     *
     * @return The minute value (0-59).
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second of the minute associated with this time schedule.
     *
     * @return The second value (0-59).
     */
    public int getSecond() {
        return second;
    }

    /**
     * Calculates the total number of seconds represented by this time schedule.
     *
     * @return The total number of seconds.
     */
    public int getTotalSeconds() {
        return  second +
                minute * 60 +
                hour * 60 * 60 +
                weekday * 24 * 60 * 60;
    }

    /**
     * Calculates the time difference (delta) in seconds between this time schedule and a given total seconds value.
     *
     * @param totalSeconds The total seconds value to compare against.
     * @return The time difference in seconds.
     */
    public int getTimeDelta(int totalSeconds) {
        int thisSeconds = getTotalSeconds();
        if (thisSeconds >= totalSeconds) {
            return thisSeconds - totalSeconds;
        } else {
            return (7 * 24 * 60 * 60) - (totalSeconds - thisSeconds);
        }
    }

    @Override
    public int hashCode() {
        final int prime = 7;
        int result = 1;
        result = prime * result + weekday;
        result = prime * result + hour;
        result = prime * result + minute;
        result = prime * result + second;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        CompetitionSchedule other = (CompetitionSchedule) obj;
        if (weekday != other.weekday)
            return false;
        if (hour != other.hour)
            return false;
        if (minute != other.minute)
            return false;
        return true;
    }
}
