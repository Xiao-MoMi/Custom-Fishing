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
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CompetitionManager {
    Set<String> getAllCompetitions();

    String getCompetitionLocale(CompetitionGoal goal);

    void startCompetition(String competition, boolean force, boolean allServers);

    @Nullable
    FishingCompetition getOnGoingCompetition();

    void startCompetition(CompetitionConfig config, boolean force, boolean allServers);

    int getNextCompetitionSeconds();

    CompletableFuture<Integer> getPlayerCount();

    @Nullable
    CompetitionConfig getConfig(String key);
}
