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
 *
 */

package net.momirealms.customfishing.mechanic.competition;

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

    public int getWeekday() {
        return weekday;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public int getTotalSeconds() {
        return  second +
                minute * 60 +
                hour * 60 * 60 +
                weekday * 24 * 60 * 60;
    }

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
