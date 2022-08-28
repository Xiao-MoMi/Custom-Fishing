package net.momirealms.customfishing.object;

import java.util.HashMap;

public class Bonus {

    private HashMap<String, Double> weightMQ;
    private HashMap<String, Integer> weightPM;
    private double time;
    private double score;
    private int difficulty;
    private double doubleLoot;

    public HashMap<String, Double> getWeightMQ() {
        return weightMQ;
    }

    public void setWeightMQ(HashMap<String, Double> weightMQ) {
        this.weightMQ = weightMQ;
    }

    public HashMap<String, Integer> getWeightPM() {
        return weightPM;
    }

    public void setWeightPM(HashMap<String, Integer> weightPM) {
        this.weightPM = weightPM;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getDoubleLoot() {
        return doubleLoot;
    }

    public void setDoubleLoot(double doubleLoot) {
        this.doubleLoot = doubleLoot;
    }
}
