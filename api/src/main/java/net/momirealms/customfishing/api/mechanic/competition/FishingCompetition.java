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

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public interface FishingCompetition {
    void start();

    void stop();

    void end();

    boolean isOnGoing();

    void refreshData(Player player, double score);

    boolean hasPlayerJoined(OfflinePlayer player);

    float getProgress();

    long getRemainingTime();

    long getStartTime();

    CompetitionConfig getConfig();

    CompetitionGoal getGoal();

    Ranking getRanking();

    ConcurrentHashMap<String, String> getCachedPlaceholders();

    String getCachedPlaceholder(String papi);
}
