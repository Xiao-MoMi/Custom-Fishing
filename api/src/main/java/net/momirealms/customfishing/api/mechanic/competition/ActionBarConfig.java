package net.momirealms.customfishing.api.mechanic.competition;

public class ActionBarConfig extends AbstractCompetitionInfo {

    public static class Builder {

        private final ActionBarConfig config;

        public Builder() {
            this.config = new ActionBarConfig();
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

        public ActionBarConfig build() {
            return this.config;
        }
    }
}
