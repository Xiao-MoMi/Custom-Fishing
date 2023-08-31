package net.momirealms.customfishing.api.mechanic.competition;

import org.bukkit.boss.BarColor;

public class BossBarConfig extends AbstractCompetitionInfo {

    private BarColor color;
    private Overlay overlay;

    public BarColor getColor() {
        return color;
    }

    public Overlay getOverlay() {
        return overlay;
    }

    public static class Builder {

        private final BossBarConfig config;

        public Builder() {
            this.config = new BossBarConfig();
        }

        public Builder showToAll(boolean showToAll) {
            this.config.showToAll = showToAll;
            return this;
        }

        public Builder refreshRate(int rate) {
            this.config.refreshRate = rate;
            return this;
        }

        public Builder switchInterval(int interval) {
            this.config.switchInterval = interval;
            return this;
        }

        public Builder text(String[] texts) {
            this.config.texts = texts;
            return this;
        }

        public Builder color(BarColor color) {
            this.config.color = color;
            return this;
        }

        public Builder overlay(Overlay overlay) {
            this.config.overlay = overlay;
            return this;
        }

        public BossBarConfig build() {
            return this.config;
        }
    }

    public enum Overlay {
        NOTCHED_6,
        NOTCHED_10,
        NOTCHED_12,
        NOTCHED_20,
        PROGRESS
    }
}
