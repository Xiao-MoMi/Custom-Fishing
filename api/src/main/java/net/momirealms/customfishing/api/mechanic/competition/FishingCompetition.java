package net.momirealms.customfishing.api.mechanic.competition;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public interface FishingCompetition {
    void start();

    void stop();

    void end();

    boolean isOnGoing();

    void refreshData(Player player, double score, boolean doubleScore);

    boolean hasPlayerJoined(OfflinePlayer player);

    float getProgress();

    long getRemainingTime();

    long getStartTime();

    CompetitionConfig getConfig();

    CompetitionGoal getGoal();

    Ranking getRanking();
}
