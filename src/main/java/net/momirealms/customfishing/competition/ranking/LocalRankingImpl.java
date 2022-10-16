package net.momirealms.customfishing.competition.ranking;

import net.momirealms.customfishing.competition.CompetitionPlayer;
import net.momirealms.customfishing.manager.MessageManager;

import java.util.*;

public class LocalRankingImpl implements RankingInterface {

    private final Set<CompetitionPlayer> competitionPlayers = Collections.synchronizedSet(new TreeSet<>());

    public void addPlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.add(competitionPlayer);
    }

    public void removePlayer(CompetitionPlayer competitionPlayer) {
        competitionPlayers.removeIf(e -> e == competitionPlayer);
    }

    @Override
    public void clear() {
        competitionPlayers.clear();
    }

    @Override
    public CompetitionPlayer getCompetitionPlayer(String player) {
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return competitionPlayer;
            }
        }
        return null;
    }

    @Override
    public Iterator<String> getIterator() {
        List<String> players = new ArrayList<>();
        for (CompetitionPlayer competitionPlayer: competitionPlayers){
            players.add(competitionPlayer.getPlayer());
        }
        return players.iterator();
    }

    @Override
    public int getSize() {
        return competitionPlayers.size();
    }

    @Override
    public String getPlayerRank(String player) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (competitionPlayer.getPlayer().equals(player)) {
                return String.valueOf(index);
            }else {
                index++;
            }
        }
        return null;
    }

    @Override
    public CompetitionPlayer[] getTop3Player() {
        CompetitionPlayer[] competitionPlayers = new CompetitionPlayer[3];
        int index = 1;
        for (CompetitionPlayer competitionPlayer : this.competitionPlayers) {
            if (index == 1) {
                competitionPlayers[0] = competitionPlayer;
            }
            if (index == 2) {
                competitionPlayers[1] = competitionPlayer;
            }
            if (index == 3) {
                competitionPlayers[2] = competitionPlayer;
                return competitionPlayers;
            }
            index++;
        }
        return competitionPlayers;
    }


    public String getPlayerAt(int i) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (index == i) {
                return competitionPlayer.getPlayer();
            }
            index++;
        }
        return null;
    }

    public float getScoreAt(int i) {
        int index = 1;
        for (CompetitionPlayer competitionPlayer : competitionPlayers) {
            if (index == i) {
                return competitionPlayer.getScore();
            }
            index++;
        }
        return 0f;
    }

    @Override
    public void refreshData(String player, float score) {
        CompetitionPlayer competitionPlayer = getCompetitionPlayer(player);
        if (competitionPlayer != null) {
            removePlayer(competitionPlayer);
            competitionPlayer.addScore(score);
            addPlayer(competitionPlayer);
        } else {
            competitionPlayer = new CompetitionPlayer(player, score);
            addPlayer(competitionPlayer);
        }
    }

    @Override
    public float getFirstScore() {
        return getScoreAt(1);
    }

    @Override
    public String getFirstPlayer() {
        return Optional.ofNullable(getPlayerAt(1)).orElse(MessageManager.noPlayer);
    }
}
