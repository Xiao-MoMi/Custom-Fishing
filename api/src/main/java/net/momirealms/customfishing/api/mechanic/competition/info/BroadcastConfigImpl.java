package net.momirealms.customfishing.api.mechanic.competition.info;

public class BroadcastConfigImpl implements BroadcastConfig {
    private final String[] texts;
    private final boolean showToAll;
    private final boolean enabled;
    private final int interval;

    public BroadcastConfigImpl(boolean enable, int interval, String[] texts, boolean showToAll) {
        this.texts = texts;
        this.showToAll = showToAll;
        this.enabled = enable;
        this.interval = interval;
    }

    @Override
    public int interval() {
        return this.interval;
    }

    @Override
    public boolean showToAll() {
        return this.showToAll;
    }

    @Override
    public String[] texts() {
        return this.texts;
    }

    @Override
    public boolean enabled() {
        return this.enabled;
    }

    public static class BuilderImpl implements BroadcastConfig.Builder {
        private int interval = DEFAULT_INTERVAL;
        private boolean showToAll = DEFAULT_SHOW_TO_ALL;
        private String[] texts = DEFAULT_TEXTS;
        private boolean enabled = DEFAULT_ENABLED;

        @Override
        public Builder interval(int interval) {
            this.interval = interval;
            return this;
        }

        @Override
        public Builder showToAll(boolean showToAll) {
            this.showToAll = showToAll;
            return this;
        }

        @Override
        public Builder texts(String[] texts) {
            this.texts = texts;
            return this;
        }

        @Override
        public Builder enable(boolean enable) {
            this.enabled = enable;
            return this;
        }

        @Override
        public BroadcastConfig build() {
            return new BroadcastConfigImpl(enabled, interval, texts, showToAll);
        }
    }
}
