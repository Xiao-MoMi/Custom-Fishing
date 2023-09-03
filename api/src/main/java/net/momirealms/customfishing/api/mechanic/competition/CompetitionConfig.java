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

import net.momirealms.customfishing.api.mechanic.action.Action;

import java.util.HashMap;

public class CompetitionConfig {

    private final String key;
    private int duration;
    private int minPlayers;
    private BossBarConfig bossBarConfig;
    private ActionBarConfig actionBarConfig;
    private Action[] skipActions;
    private Action[] startActions;
    private Action[] endActions;
    private Action[] joinActions;
    private CompetitionGoal goal;
    private HashMap<String, Action[]> rewards;

    public CompetitionConfig(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getDuration() {
        return duration;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public Action[] getStartActions() {
        return startActions;
    }

    public Action[] getEndActions() {
        return endActions;
    }

    public Action[] getJoinActions() {
        return joinActions;
    }

    public Action[] getSkipActions() {
        return skipActions;
    }

    public CompetitionGoal getGoal() {
        return goal;
    }

    public HashMap<String, Action[]> getRewards() {
        return rewards;
    }

    public BossBarConfig getBossBarConfig() {
        return bossBarConfig;
    }

    public ActionBarConfig getActionBarConfig() {
        return actionBarConfig;
    }

    public static class Builder {

        private final CompetitionConfig config;

        public Builder(String key) {
            this.config = new CompetitionConfig(key);
        }

        public Builder duration(int duration) {
            config.duration = duration;
            return this;
        }

        public Builder minPlayers(int min) {
            config.minPlayers = min;
            return this;
        }

        public Builder startActions(Action[] startActions) {
            config.startActions = startActions;
            return this;
        }

        public Builder endActions(Action[] endActions) {
            config.endActions = endActions;
            return this;
        }

        public Builder skipActions(Action[] skipActions) {
            config.skipActions = skipActions;
            return this;
        }

        public Builder joinActions(Action[] joinActions) {
            config.joinActions = joinActions;
            return this;
        }

        public Builder actionbar(ActionBarConfig actionBarConfig) {
            config.actionBarConfig = actionBarConfig;
            return this;
        }

        public Builder bossbar(BossBarConfig bossBarConfig) {
            config.bossBarConfig = bossBarConfig;
            return this;
        }

        public Builder goal(CompetitionGoal goal) {
            config.goal = goal;
            return this;
        }

        public Builder rewards(HashMap<String, Action[]> rewards) {
            config.rewards = rewards;
            return this;
        }

        public CompetitionConfig build() {
            return config;
        }
    }
}
