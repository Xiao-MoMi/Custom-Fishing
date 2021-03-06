package net.momirealms.customfishing.utils;

import net.momirealms.customfishing.timer.Timer;

public record FishingPlayer(Long fishingTime, Timer timer) {

    public Long getFishingTime() {
        return this.fishingTime;
    }

    public Timer getTimer() {
        return this.timer;
    }
}