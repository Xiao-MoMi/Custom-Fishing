package net.momirealms.customfishing.competition.ranking;

import net.momirealms.customfishing.competition.CompetitionPlayer;

import java.util.Iterator;

public interface RankingInterface {

    void clear();
    CompetitionPlayer getCompetitionPlayer(String player);
    Iterator<String> getIterator();
    int getSize();
    String getPlayerRank(String player);
    CompetitionPlayer[] getTop3Player();
    void refreshData(String player, float score);
    float getFirstScore();
    String getFirstPlayer();
}
