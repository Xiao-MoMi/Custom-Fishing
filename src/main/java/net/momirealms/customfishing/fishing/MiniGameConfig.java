package net.momirealms.customfishing.fishing;

import net.momirealms.customfishing.fishing.bar.FishingBar;

import java.util.Random;

public class MiniGameConfig {

    private final int time;
    private final FishingBar[] bars;
    private final int[] difficulties;

    public MiniGameConfig(int time, FishingBar[] bars, int[] difficulties) {
        this.time = time;
        this.bars = bars;
        this.difficulties = difficulties;
    }

    public FishingBar getRandomBar() {
        return bars[new Random().nextInt(bars.length)];
    }

    public int getRandomDifficulty() {
        return difficulties[new Random().nextInt(difficulties.length)];
    }

    public int getTime() {
        return this.time;
    }
}
