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

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;

public class CompetitionSchedule {

    public static Competition competition;
    public static boolean hasBossBar;
    private int doubleCheckTime;
    private int checkTaskID;

    public CompetitionSchedule(){
        hasBossBar = false;
    }

    public static boolean startCompetition(String competitionName){
        CompetitionConfig competitionConfig = ConfigReader.CompetitionsCommand.get(competitionName);
        if (competitionConfig == null) return false;
        if (competition != null){
            competition.end();
        }
        competition = new Competition(competitionConfig);
        competition.begin(true);
        hasBossBar = competitionConfig.isEnableBossBar();
        return true;
    }

    public static void endCompetition(){
        if (competition != null){
            competition.end();
        }
    }

    public static void cancelCompetition(){
        if (competition != null){
            competition.cancel();
        }
    }

    public void startCompetition(CompetitionConfig competitionConfig){
        if (competition != null){
            competition.end();
        }
        competition = new Competition(competitionConfig);
        competition.begin(false);
        hasBossBar = competitionConfig.isEnableBossBar();
    }

    public void checkTime() {
        BukkitTask checkTimeTask = new BukkitRunnable(){
            public void run(){
                if (isANewMinute()){
                    CompetitionConfig competitionConfig = ConfigReader.Competitions.get(getCurrentTime());
                    if (competitionConfig != null){
                        startCompetition(competitionConfig);
                    }
                }
            }
        }.runTaskTimer(CustomFishing.instance, (60- LocalTime.now().getSecond())*20, 1200);
        checkTaskID = checkTimeTask.getTaskId();
    }

    public void stopCheck(){
        Bukkit.getScheduler().cancelTask(checkTaskID);
    }

    public String getCurrentTime() {
        return LocalTime.now().getHour() + ":" + String.format("%02d", LocalTime.now().getMinute());
    }

    private boolean isANewMinute() {
        int minute = LocalTime.now().getMinute();
        if (doubleCheckTime != minute) {
            doubleCheckTime = minute;
            return true;
        }else {
            return false;
        }
    }
}
