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
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private Requirement[] requirements;
    private CompetitionGoal goal;
    private HashMap<String, Action[]> rewards;

    public CompetitionConfig(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getDurationInSeconds() {
        return duration;
    }

    public int getMinPlayersToStart() {
        return minPlayers;
    }

    @Nullable
    public Action[] getStartActions() {
        return startActions;
    }

    @Nullable
    public Action[] getEndActions() {
        return endActions;
    }

    /**
     * Get the actions to perform if player joined the competition
     *
     * @return actions
     */
    @Nullable
    public Action[] getJoinActions() {
        return joinActions;
    }

    /**
     * Get the actions to perform if the amount of players doesn't meet the requirement
     *
     * @return actions
     */
    @Nullable
    public Action[] getSkipActions() {
        return skipActions;
    }

    /**
     * Get the requirements for participating the competition
     *
     * @return requirements
     */
    @Nullable
    public Requirement[] getRequirements() {
        return requirements;
    }

    @NotNull
    public CompetitionGoal getGoal() {
        return goal;
    }

    /**
     * Get the reward map
     *
     * @return reward map
     */
    public HashMap<String, Action[]> getRewards() {
        return rewards;
    }

    @Nullable
    public BossBarConfig getBossBarConfig() {
        return bossBarConfig;
    }

    @Nullable
    public ActionBarConfig getActionBarConfig() {
        return actionBarConfig;
    }

    public static Builder builder(String key) {
        return new Builder(key);
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

        @SuppressWarnings("UnusedReturnValue")
        public Builder actionbar(ActionBarConfig actionBarConfig) {
            config.actionBarConfig = actionBarConfig;
            return this;
        }

        @SuppressWarnings("UnusedReturnValue")
        public Builder bossbar(BossBarConfig bossBarConfig) {
            config.bossBarConfig = bossBarConfig;
            return this;
        }

        public Builder requirements(Requirement[] requirements) {
            config.requirements = requirements;
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
