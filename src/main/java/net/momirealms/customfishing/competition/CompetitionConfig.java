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

import net.momirealms.customfishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.competition.reward.Reward;

import java.util.HashMap;
import java.util.List;

public class CompetitionConfig {

    private int duration;
    private int minPlayers;
    private List<String> startMessage;
    private List<String> endMessage;
    private List<String> startCommand;
    private List<String> endCommand;
    private List<String> joinCommand;
    private Goal goal;
    private BossBarConfig bossBarConfig;
    private final boolean enableBossBar;
    private HashMap<String, List<Reward>> rewards;

    public CompetitionConfig(boolean enableBossBar){this.enableBossBar = enableBossBar;}

    public void setDuration(int duration) {this.duration = duration;}
    public void setBossBarConfig(BossBarConfig bossBarConfig) {this.bossBarConfig = bossBarConfig;}
    public void setGoal(Goal goal) {this.goal = goal;}
    public void setEndMessage(List<String> endMessage) {this.endMessage = endMessage;}
    public void setStartMessage(List<String> startMessage) {this.startMessage = startMessage;}
    public void setStartCommand(List<String> startCommand) {this.startCommand = startCommand;}
    public void setEndCommand(List<String> endCommand) {this.endCommand = endCommand;}
    public void setJoinCommand(List<String> joinCommand) {this.joinCommand = joinCommand;}
    public void setMinPlayers(int minPlayers) {this.minPlayers = minPlayers;}
    public HashMap<String, List<Reward>> getRewards() {return rewards;}

    public Goal getGoal() {return goal;}
    public int getMinPlayers() {return minPlayers;}
    public int getDuration() {return duration;}
    public BossBarConfig getBossBarConfig() {return bossBarConfig;}
    public boolean isEnableBossBar() {return enableBossBar;}
    public List<String> getEndMessage() {return endMessage;}
    public List<String> getStartMessage() {return startMessage;}
    public void setRewards(HashMap<String, List<Reward>> rewards) {this.rewards = rewards;}
    public List<String> getEndCommand() {return endCommand;}
    public List<String> getJoinCommand() {return joinCommand;}
    public List<String> getStartCommand() {return startCommand;}
}
