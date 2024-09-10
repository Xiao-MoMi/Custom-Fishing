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

package net.momirealms.customfishing.bukkit.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CompetitionPapi extends PlaceholderExpansion {

    private final BukkitCustomFishingPlugin plugin;

    public CompetitionPapi(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        super.register();
    }

    public void unload() {
        super.unregister();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "cfcompetition";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.2";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        switch (params) {
            case "goingon" -> {
                return String.valueOf(plugin.getCompetitionManager().getOnGoingCompetition() != null);
            }
            case "nextseconds" -> {
                return String.valueOf(plugin.getCompetitionManager().getNextCompetitionInSeconds());
            }
            case "nextsecond" -> {
                return plugin.getCompetitionManager().getNextCompetitionInSeconds() % 60 + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_SECOND.build().key());
            }
            case "nextminute" -> {
                int sec = plugin.getCompetitionManager().getNextCompetitionInSeconds();
                int min = (sec % 3600) / 60;
                return sec < 60 ? "" : min + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_MINUTE.build().key());
            }
            case "nexthour" -> {
                int sec = plugin.getCompetitionManager().getNextCompetitionInSeconds();
                int h = (sec % (3600 * 24)) / 3600;
                return sec < 3600 ? "" : h + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_HOUR.build().key());
            }
            case "nextday" -> {
                int sec = plugin.getCompetitionManager().getNextCompetitionInSeconds();
                int day = sec / (3600 * 24);
                return day == 0 ? "" : day + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_DAY.build().key());
            }
            case "rank" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                else return String.valueOf(competition.getRanking().getPlayerRank(player.getName()));
            }
            case "goal" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                else return competition.getGoal().toString();
            }
            case "seconds" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                return String.valueOf(competition.getPublicContext().arg(ContextKeys.SECONDS));
            }
            case "second" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                return String.valueOf(competition.getPublicContext().arg(ContextKeys.SECOND));
            }
            case "minute" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                return String.valueOf(competition.getPublicContext().arg(ContextKeys.MINUTE));
            }
            case "hour" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                return String.valueOf(competition.getPublicContext().arg(ContextKeys.HOUR));
            }
        }

        String[] split = params.split("_", 2);
        switch (split[0]) {
            case "score" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                double score;
                if (split.length == 1) {
                    score = competition.getRanking().getPlayerScore(player.getName());
                } else {
                    score = competition.getRanking().getScoreAt(Integer.parseInt(split[1]));
                }
                return String.format("%.2f", competition.getGoal().isReversed() ? -score : score);
            }
            case "player" -> {
                FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
                if (competition == null) return "";
                if (split.length == 1) return "Invalid format";
                return Optional.ofNullable(competition.getRanking().getPlayerAt(Integer.parseInt(split[1]))).orElse("");
            }
        }
        return "null";
    }
}
