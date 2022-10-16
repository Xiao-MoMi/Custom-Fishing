package net.momirealms.customfishing.competition;

import org.jetbrains.annotations.NotNull;

public class CompetitionPlayer implements Comparable<CompetitionPlayer>{

    private long time;
    private final String player;
    private float score;

    public static CompetitionPlayer emptyPlayer = new CompetitionPlayer(null, 0);

    public CompetitionPlayer(String player, float score) {
        this.player = player;
        this.score = score;
        this.time = System.currentTimeMillis();
    }

    public void addScore(float score){
        this.score += score;
        this.time = System.currentTimeMillis();
    }

    public float getScore() {
        return this.score;
    }

    public String getPlayer(){
        return this.player;
    }

    @Override
    public int compareTo(@NotNull CompetitionPlayer competitionPlayer) {
        if (competitionPlayer.getScore() != this.score) {
            return (competitionPlayer.getScore() > this.score) ? 1 : -1;
        } else {
            return (competitionPlayer.getScore() > this.time) ? 1 : -1;
        }
    }

    @Override
    public String toString() {
        return "CompetitionPlayer{" +
                "time=" + time +
                ", player='" + player + '\'' +
                ", score=" + score +
                '}';
    }
}
