/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.mechanic.competition.info;

/**
 * Configuration interface for competition actionbars.
 */
public interface ActionBarConfig {

    int DEFAULT_REFRESH_RATE = 20;
    int DEFAULT_SWITCH_INTERVAL = 200;
    boolean DEFAULT_VISIBILITY = true;
    String[] DEFAULT_TEXTS = new String[]{""};

    /**
     * Get the refresh rate for updating the competition information on the action bar.
     *
     * @return The refresh rate in ticks.
     */
    int refreshRate();

    /**
     * Get the switch interval for displaying different competition texts.
     *
     * @return The switch interval in ticks.
     */
    int switchInterval();

    /**
     * Check if competition information should be shown to all players.
     *
     * @return True if information is shown to all players, otherwise only to participants.
     */
    boolean showToAll();

    /**
     * Get an array of competition information texts.
     *
     * @return An array of competition information texts.
     */
    String[] texts();

    /**
     * Is action bar enabled
     *
     * @return enabled or not
     */
    boolean enabled();

    /**
     * Creates a new builder instance for constructing {@link ActionBarConfig} objects.
     *
     * @return A new {@link Builder} instance.
     */
    static Builder builder() {
        return new ActionBarConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing {@link ActionBarConfig} objects.
     */
    interface Builder {

        /**
         * Sets whether the competition information should be shown to all players.
         *
         * @param showToAll True to show information to all players, false to show only to participants.
         * @return The current {@link Builder} instance.
         */
        Builder showToAll(boolean showToAll);

        /**
         * Sets the refresh rate for updating the competition information.
         *
         * @param rate The refresh rate in ticks.
         * @return The current {@link Builder} instance.
         */
        Builder refreshRate(int rate);

        /**
         * Sets the interval for switching between different competition texts.
         *
         * @param interval The switch interval in ticks.
         * @return The current {@link Builder} instance.
         */
        Builder switchInterval(int interval);

        /**
         * Sets the texts to be displayed on the action bar during the competition.
         *
         * @param texts An array of competition information texts.
         * @return The current {@link Builder} instance.
         */
        Builder text(String[] texts);

        /**
         * Sets whether the actionbar is enabled
         *
         * @param enable enable or not
         * @return The current {@link Builder} instance.
         */
        Builder enable(boolean enable);

        /**
         * Builds the {@link ActionBarConfig} object with the configured settings.
         *
         * @return The constructed {@link ActionBarConfig} object.
         */
        ActionBarConfig build();
    }
}
