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

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.Function;

import java.time.LocalTime;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CompetitionSchedule extends Function {

    @Override
    public void load() {
        checkTime();
    }

    @Override
    public void unload() {
        stopCheck();
        cancelCompetition();
    }

    private ScheduledFuture<?> checkTimeTask;
    private int doubleCheckTime;

    public static boolean startCompetition(String competitionName) {
        CompetitionConfig competitionConfig = CustomFishing.getInstance().getCompetitionManager().getCompetitionsC().get(competitionName);
        if (competitionConfig == null) return false;
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.end();
        }
        Competition.currentCompetition = new Competition(competitionConfig);
        Competition.currentCompetition.begin(true);
        return true;
    }

    public static void cancelCompetition() {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.cancel();
        }
    }

    public static void endCompetition() {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.end();
        }
    }

    public void startCompetition(CompetitionConfig competitionConfig) {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.end();
        }
        Competition.currentCompetition = new Competition(competitionConfig);
        Competition.currentCompetition.begin(false);
    }

    public void checkTime() {
        this.checkTimeTask = CustomFishing.getInstance().getScheduler().runTaskTimerAsync(() -> {
            if (isANewMinute()) {
                CompetitionConfig competitionConfig = CustomFishing.getInstance().getCompetitionManager().getCompetitionsT().get(getCurrentTime());
                if (competitionConfig != null && competitionConfig.canStart()) {
                    startCompetition(competitionConfig);
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void stopCheck() {
        if (this.checkTimeTask != null && !checkTimeTask.isCancelled()) {
            checkTimeTask.cancel(false);
        }
    }

    public String getCurrentTime() {
        return LocalTime.now().getHour() + ":" + String.format("%02d", LocalTime.now().getMinute());
    }

    private boolean isANewMinute() {
        int minute = LocalTime.now().getMinute();
        if (doubleCheckTime != minute) {
            doubleCheckTime = minute;
            return true;
        } else {
            return false;
        }
    }
}
