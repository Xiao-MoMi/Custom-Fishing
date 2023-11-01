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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface CompetitionManager {

    /**
     * Retrieves a set of all competition names.
     *
     * @return A set of competition names.
     */
    @NotNull Set<String> getAllCompetitionKeys();

    /**
     * Retrieves the localization key for a given competition goal.
     *
     * @param goal The competition goal to retrieve the localization key for.
     * @return The localization key for the specified competition goal.
     */
    @NotNull String getCompetitionGoalLocale(CompetitionGoal goal);

    /**
     * Starts a competition with the specified name, allowing for the option to force start it or apply it to the entire server.
     *
     * @param competition The name of the competition to start.
     * @param force       Whether to force start the competition even if amount of the online players is lower than the requirement
     * @param serverGroup   Whether to apply the competition to the servers that connected to Redis.
     * @return {@code true} if the competition was started successfully, {@code false} otherwise.
     */
    boolean startCompetition(String competition, boolean force, @Nullable String serverGroup);

    /**
     * Gets the ongoing fishing competition, if one is currently in progress.
     *
     * @return The ongoing fishing competition, or null if there is none.
     */
    @Nullable FishingCompetition getOnGoingCompetition();

    /**
     * Starts a competition using the specified configuration.
     *
     * @param config    The configuration of the competition to start.
     * @param force     Whether to force start the competition even if amount of the online players is lower than the requirement
     * @param serverGroup Whether the competition should start across all servers that connected to Redis
     * @return True if the competition was started successfully, false otherwise.
     */
    boolean startCompetition(CompetitionConfig config, boolean force, @Nullable String serverGroup);

    /**
     * Gets the number of seconds until the next competition.
     *
     * @return The number of seconds until the next competition.
     */
    int getNextCompetitionSeconds();

    /**
     * Retrieves the configuration for a competition based on its key.
     *
     * @param key The key of the competition configuration to retrieve.
     * @return The {@link CompetitionConfig} for the specified key, or {@code null} if no configuration exists with that key.
     */
    @Nullable CompetitionConfig getConfig(String key);
}
