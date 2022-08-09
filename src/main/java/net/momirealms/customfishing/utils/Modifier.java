package net.momirealms.customfishing.utils;

public class Modifier {

    private int difficulty;
    private double score;
    private boolean willDouble;

    public int getDifficultyModifier() {
        return difficulty;
    }

    public void setDifficultyModifier(int difficulty) {
        this.difficulty = difficulty;
    }

    public double getScoreModifier() {
        return score;
    }

    public void setScoreModifier(double score) {
        this.score = score;
    }

    public boolean willDouble() {
        return willDouble;
    }

    public void setWillDouble(boolean willDouble) {
        this.willDouble = willDouble;
    }
}
