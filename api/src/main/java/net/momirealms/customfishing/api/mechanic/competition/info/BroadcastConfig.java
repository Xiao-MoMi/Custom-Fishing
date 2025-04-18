package net.momirealms.customfishing.api.mechanic.competition.info;

public interface BroadcastConfig {
    int DEFAULT_INTERVAL = 200;
    boolean DEFAULT_SHOW_TO_ALL = true;
    String[] DEFAULT_TEXTS = new String[]{""};
    boolean DEFAULT_ENABLED = true;

    /**
     * Get the interval for broadcasting competition information.
     *
     * @return The interval in ticks.
     */
    int interval();

    /**
     * Check if competition information should be shown to all players.
     *
     * @return True if information is shown to all players, otherwise only to participants.
     */
    boolean showToAll();

    /**
     * Get an array of competition information texts to be broadcasted.
     *
     * @return An array of broadcast texts.
     */
    String[] texts();

    /**
     * Check if the broadcast is enabled.
     *
     * @return True if the broadcast is enabled, false otherwise.
     */
    boolean enabled();

    /**
     * Creates a new builder instance for constructing {@link BroadcastConfig} objects.
     *
     * @return A new {@link Builder} instance.
     */
    static Builder builder() {
        return new BroadcastConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing {@link BroadcastConfig} objects.
     */
    interface Builder {

        /**
         * Sets the interval between broadcasting messages.
         *
         * @param interval The interval in ticks.
         * @return The current {@link Builder} instance.
         */
        Builder interval(int interval);

        /**
         * Sets whether the broadcast should be visible to all players.
         *
         * @param showToAll True to show to all players, false for participants only.
         * @return The current {@link Builder} instance.
         */
        Builder showToAll(boolean showToAll);

        /**
         * Sets the texts to be broadcasted during the competition.
         *
         * @param texts An array of broadcast texts.
         * @return The current {@link Builder} instance.
         */
        Builder texts(String[] texts);

        /**
         * Enables or disables the broadcast.
         *
         * @param enable True to enable the broadcast, false to disable.
         * @return The current {@link Builder} instance.
         */
        Builder enable(boolean enable);

        /**
         * Builds the {@link BroadcastConfig} object with the configured settings.
         *
         * @return The constructed {@link BroadcastConfig} object.
         */
        BroadcastConfig build();
    }
}