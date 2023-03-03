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

package net.momirealms.customfishing.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.fishing.competition.ranking.RankingInterface;
import net.momirealms.customfishing.manager.MessageManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CompetitionPapi extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "competition";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (Competition.currentCompetition == null) return "";
        RankingInterface ranking = Competition.currentCompetition.getRanking();
        switch (params) {
            case "rank" -> {
                return Competition.currentCompetition.getPlayerRank(player);
            }
            case "score" -> {
                return String.format("%.1f", Competition.currentCompetition.getScore(player));
            }
            case "time" -> {
                return String.valueOf(Competition.currentCompetition.getRemainingTime());
            }
            case "minute" -> {
                return String.format("%02d", Competition.currentCompetition.getRemainingTime() / 60);
            }
            case "second" -> {
                return String.format("%02d", Competition.currentCompetition.getRemainingTime() % 60);
            }
            case "1st_score" -> {
                return ranking.getScoreAt(1) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(1));
            }
            case "1st_player" -> {
                return Optional.ofNullable(ranking.getPlayerAt(1)).orElse(MessageManager.noPlayer);
            }
            case "2nd_score" -> {
                return ranking.getScoreAt(2) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(2));
            }
            case "2nd_player" -> {
                return Optional.ofNullable(ranking.getPlayerAt(2)).orElse(MessageManager.noPlayer);
            }
            case "3rd_score" -> {
                return ranking.getScoreAt(3) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(3));
            }
            case "3rd_player" -> {
                return Optional.ofNullable(ranking.getPlayerAt(3)).orElse(MessageManager.noPlayer);
            }
        }
        return "null";
    }
}
