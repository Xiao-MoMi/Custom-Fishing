package net.momirealms.customfishing.integration.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.competition.Competition;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        if (Competition.currentCompetition == null) return "null";
        switch (params) {
            case "rank" -> {
                return Competition.currentCompetition.getPlayerRank(player);
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
                return String.format("%.1f", Competition.currentCompetition.getFirstScore());
            }
            case "1st_player" -> {
                return Competition.currentCompetition.getFirstPlayer();
            }
        }
        return "null";
    }
}
