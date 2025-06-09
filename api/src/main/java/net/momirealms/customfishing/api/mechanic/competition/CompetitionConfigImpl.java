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

package net.momirealms.customfishing.api.mechanic.competition;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.competition.info.ActionBarConfig;
import net.momirealms.customfishing.api.mechanic.competition.info.BossBarConfig;
import net.momirealms.customfishing.api.mechanic.competition.info.BroadcastConfig;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CompetitionConfigImpl implements CompetitionConfig {

    private final String key;
    private final CompetitionGoal goal;
    private final int duration;
    private final int minPlayers;
    private final Requirement<Player>[] joinRequirements;
    private final Action<Player>[] skipActions;
    private final Action<Player>[] startActions;
    private final Action<Player>[] endActions;
    private final Action<Player>[] joinActions;
    private final HashMap<String, Action<Player>[]> rewards;
    private final BossBarConfig bossBarConfig;
    private final ActionBarConfig actionBarConfig;
    private final BroadcastConfig broadcastConfig;
    private final List<CompetitionSchedule> schedules;

    public CompetitionConfigImpl(String key, CompetitionGoal goal, int duration, int minPlayers, Requirement<Player>[] joinRequirements,
                                 Action<Player>[] skipActions, Action<Player>[] startActions, Action<Player>[] endActions, Action<Player>[] joinActions, HashMap<String, Action<Player>[]> rewards,
                                 BossBarConfig bossBarConfig, ActionBarConfig actionBarConfig, BroadcastConfig broadcastConfig,
                                 List<CompetitionSchedule> schedules) {
        this.key = key;
        this.goal = goal;
        this.duration = duration;
        this.minPlayers = minPlayers;
        this.joinRequirements = joinRequirements;
        this.skipActions = skipActions;
        this.startActions = startActions;
        this.endActions = endActions;
        this.joinActions = joinActions;
        this.rewards = rewards;
        this.bossBarConfig = bossBarConfig;
        this.actionBarConfig = actionBarConfig;
        this.broadcastConfig = broadcastConfig;
        this.schedules = schedules;
    }

    @Override
    public String id() {
        return key;
    }

    @Override
    public int durationInSeconds() {
        return duration;
    }

    @Override
    public int minPlayersToStart() {
        return minPlayers;
    }

    @Override
    public Action<Player>[] startActions() {
        return startActions;
    }

    @Override
    public Action<Player>[] endActions() {
        return endActions;
    }

    @Override
    public Action<Player>[] joinActions() {
        return joinActions;
    }

    @Override
    public Action<Player>[] skipActions() {
        return skipActions;
    }

    @Override
    public Requirement<Player>[] joinRequirements() {
        return joinRequirements;
    }

    @Override
    public CompetitionGoal goal() {
        return goal;
    }

    @Override
    public HashMap<String, Action<Player>[]> rewards() {
        return rewards;
    }

    @Override
    public BossBarConfig bossBarConfig() {
        return bossBarConfig;
    }

    @Override
    public ActionBarConfig actionBarConfig() {
        return actionBarConfig;
    }

    @Override
    public BroadcastConfig broadcastConfig() {
        return broadcastConfig;
    }

    @Override
    public List<CompetitionSchedule> schedules() {
        return schedules;
    }

    public static class BuilderImpl implements Builder {
        private String key;
        private CompetitionGoal goal = DEFAULT_GOAL;
        private int duration = DEFAULT_DURATION;
        private int minPlayers = DEFAULT_MIN_PLAYERS;
        private Requirement<Player>[] joinRequirements = DEFAULT_REQUIREMENTS;
        private Action<Player>[] skipActions = DEFAULT_SKIP_ACTIONS;
        private Action<Player>[] startActions = DEFAULT_START_ACTIONS;
        private Action<Player>[] endActions = DEFAULT_END_ACTIONS;
        private Action<Player>[] joinActions = DEFAULT_JOIN_ACTIONS;
        private HashMap<String, Action<Player>[]> rewards = DEFAULT_REWARDS;
        private BossBarConfig bossBarConfig;
        private ActionBarConfig actionBarConfig;
        private BroadcastConfig broadcastConfig;
        private final List<CompetitionSchedule> schedules = new ArrayList<>();
        @Override
        public Builder id(String key) {
            this.key = key;
            return this;
        }
        @Override
        public Builder goal(CompetitionGoal goal) {
            this.goal = goal;
            return this;
        }
        @Override
        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }
        @Override
        public Builder minPlayers(int minPlayers) {
            this.minPlayers = minPlayers;
            return this;
        }
        @Override
        public Builder joinRequirements(Requirement<Player>[] joinRequirements) {
            this.joinRequirements = joinRequirements;
            return this;
        }
        @Override
        public Builder skipActions(Action<Player>[] skipActions) {
            this.skipActions = skipActions;
            return this;
        }
        @Override
        public Builder startActions(Action<Player>[] startActions) {
            this.startActions = startActions;
            return this;
        }
        @Override
        public Builder endActions(Action<Player>[] endActions) {
            this.endActions = endActions;
            return this;
        }
        @Override
        public Builder joinActions(Action<Player>[] joinActions) {
            this.joinActions = joinActions;
            return this;
        }
        @Override
        public Builder rewards(HashMap<String, Action<Player>[]> rewards) {
            this.rewards = rewards;
            return this;
        }
        @Override
        public Builder bossBarConfig(BossBarConfig bossBarConfig) {
            this.bossBarConfig = bossBarConfig;
            return this;
        }
        @Override
        public Builder actionBarConfig(ActionBarConfig actionBarConfig) {
            this.actionBarConfig = actionBarConfig;
            return this;
        }
        @Override
        public Builder broadcastConfig(BroadcastConfig broadcastConfig) {
            this.broadcastConfig = broadcastConfig;
            return this;
        }
        @Override
        public Builder schedules(List<CompetitionSchedule> schedules) {
            this.schedules.addAll(schedules);
            return this;
        }
        @Override
        public CompetitionConfig build() {
            return new CompetitionConfigImpl(key, goal, duration, minPlayers, joinRequirements, skipActions, startActions, endActions, joinActions, rewards, bossBarConfig, actionBarConfig, broadcastConfig, schedules);
        }
    }
}
