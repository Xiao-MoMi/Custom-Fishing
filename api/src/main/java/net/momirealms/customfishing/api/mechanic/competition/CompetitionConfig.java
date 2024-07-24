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
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

/**
 * Interface representing the configuration for a fishing competition.
 */
public interface CompetitionConfig {

    CompetitionGoal DEFAULT_GOAL = CompetitionGoal.CATCH_AMOUNT;
    int DEFAULT_DURATION = 300;
    int DEFAULT_MIN_PLAYERS = 0;
    Requirement<Player>[] DEFAULT_REQUIREMENTS = null;
    Action<Player>[] DEFAULT_SKIP_ACTIONS = null;
    Action<Player>[] DEFAULT_START_ACTIONS = null;
    Action<Player>[] DEFAULT_END_ACTIONS = null;
    Action<Player>[] DEFAULT_JOIN_ACTIONS = null;
    HashMap<String, Action<Player>[]> DEFAULT_REWARDS = new HashMap<>();

    /**
     * Gets the unique key for the competition.
     *
     * @return the key for the competition.
     */
    String id();

    /**
     * Gets the duration of the competition in seconds.
     *
     * @return the duration in seconds.
     */
    int durationInSeconds();

    /**
     * Gets the minimum number of players required to start the competition.
     *
     * @return the minimum number of players.
     */
    int minPlayersToStart();

    /**
     * Gets the actions to be performed when the competition starts.
     *
     * @return an array of start actions.
     */
    Action<Player>[] startActions();

    /**
     * Gets the actions to be performed when the competition ends.
     *
     * @return an array of end actions.
     */
    Action<Player>[] endActions();

    /**
     * Gets the actions to be performed when a player joins the competition.
     *
     * @return an array of join actions.
     */
    Action<Player>[] joinActions();

    /**
     * Gets the actions to be performed when a player skips the competition.
     *
     * @return an array of skip actions.
     */
    Action<Player>[] skipActions();

    /**
     * Gets the requirements that players must meet to join the competition.
     *
     * @return an array of join requirements.
     */
    Requirement<Player>[] joinRequirements();

    /**
     * Gets the goal of the competition.
     *
     * @return the competition goal.
     */
    CompetitionGoal goal();

    /**
     * Gets the rewards for the competition.
     *
     * @return a hashmap where the key is a string identifier and the value is an array of actions.
     */
    HashMap<String, Action<Player>[]> rewards();

    /**
     * Gets the configuration for the boss bar during the competition.
     *
     * @return the boss bar configuration.
     */
    BossBarConfig bossBarConfig();

    /**
     * Gets the configuration for the action bar during the competition.
     *
     * @return the action bar configuration.
     */
    ActionBarConfig actionBarConfig();

    /**
     * Get the time to start competition
     *
     * @return schedules
     */
    List<CompetitionSchedule> schedules();

    /**
     * Creates a new builder for the competition configuration.
     *
     * @return a new builder instance.
     */
    static Builder builder() {
        return new CompetitionConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing a CompetitionConfig instance.
     */
    interface Builder {

        /**
         * Sets the unique key for the competition.
         *
         * @param key the key for the competition.
         * @return the builder instance.
         */
        Builder id(String key);

        /**
         * Sets the goal of the competition.
         *
         * @param goal the competition goal.
         * @return the builder instance.
         */
        Builder goal(CompetitionGoal goal);

        /**
         * Sets the duration of the competition.
         *
         * @param duration the duration in seconds.
         * @return the builder instance.
         */
        Builder duration(int duration);

        /**
         * Sets the minimum number of players required to start the competition.
         *
         * @param minPlayers the minimum number of players.
         * @return the builder instance.
         */
        Builder minPlayers(int minPlayers);

        /**
         * Sets the requirements that players must meet to join the competition.
         *
         * @param joinRequirements an array of join requirements.
         * @return the builder instance.
         */
        Builder joinRequirements(Requirement<Player>[] joinRequirements);

        /**
         * Sets the actions to be performed when a player skips the competition.
         *
         * @param skipActions an array of skip actions.
         * @return the builder instance.
         */
        Builder skipActions(Action<Player>[] skipActions);

        /**
         * Sets the actions to be performed when the competition starts.
         *
         * @param startActions an array of start actions.
         * @return the builder instance.
         */
        Builder startActions(Action<Player>[] startActions);

        /**
         * Sets the actions to be performed when the competition ends.
         *
         * @param endActions an array of end actions.
         * @return the builder instance.
         */
        Builder endActions(Action<Player>[] endActions);

        /**
         * Sets the actions to be performed when a player joins the competition.
         *
         * @param joinActions an array of join actions.
         * @return the builder instance.
         */
        Builder joinActions(Action<Player>[] joinActions);

        /**
         * Sets the rewards for the competition.
         *
         * @param rewards a hashmap where the key is a string identifier and the value is an array of actions.
         * @return the builder instance.
         */
        Builder rewards(HashMap<String, Action<Player>[]> rewards);

        /**
         * Sets the configuration for the boss bar during the competition.
         *
         * @param bossBarConfig the boss bar configuration.
         * @return the builder instance.
         */
        Builder bossBarConfig(BossBarConfig bossBarConfig);

        /**
         * Sets the configuration for the action bar during the competition.
         *
         * @param actionBarConfig the action bar configuration.
         * @return the builder instance.
         */
        Builder actionBarConfig(ActionBarConfig actionBarConfig);

        /**
         * Sets the configuration for schedules of the competition.
         *
         * @param schedules the schedules
         * @return the builder instance.
         */
        Builder schedules(List<CompetitionSchedule> schedules);

        /**
         * Builds and returns the CompetitionConfig instance.
         *
         * @return the constructed CompetitionConfig instance.
         */
        CompetitionConfig build();
    }
}
