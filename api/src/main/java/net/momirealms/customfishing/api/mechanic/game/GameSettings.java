package net.momirealms.customfishing.api.mechanic.game;

public class GameSettings {

    private final int time;
    private final int difficulty;

    public GameSettings(int time, int difficulty) {
        this.time = time;
        this.difficulty = difficulty;
    }

    public int getTime() {
        return time;
    }

    public int getDifficulty() {
        return difficulty;
    }
}
