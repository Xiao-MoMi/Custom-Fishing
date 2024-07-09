/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

import net.kyori.adventure.bossbar.BossBar;

public interface BossBarConfig {

    int DEFAULT_REFRESH_RATE = 20;
    int DEFAULT_SWITCH_INTERVAL = 200;
    boolean DEFAULT_VISIBILITY = true;
    String[] DEFAULT_TEXTS = new String[]{""};
    BossBar.Color DEFAULT_COLOR = BossBar.Color.BLUE;
    BossBar.Overlay DEFAULT_OVERLAY = BossBar.Overlay.PROGRESS;

    /**
     * Get the refresh rate for updating competition information.
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
     * Gets the color of the boss bar.
     *
     * @return The color of the boss bar.
     */
    BossBar.Color color();

    /**
     * Gets the overlay style of the boss bar.
     *
     * @return The overlay style of the boss bar.
     */
    BossBar.Overlay overlay();

    /**
     * Is boss bar enabled
     *
     * @return enabled or not
     */
    boolean enabled();

    /**
     * Creates a new builder instance for constructing {@code BossBarConfig} objects.
     *
     * @return A new {@code Builder} instance.
     */
    static Builder builder() {
        return new BossBarConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing {@code BossBarConfig} objects.
     */
    interface Builder {

        /**
         * Sets whether the competition information should be shown to all players.
         *
         * @param showToAll True to show information to all players, false to show only to participants.
         * @return The current {@code Builder} instance.
         */
        Builder showToAll(boolean showToAll);

        /**
         * Sets the refresh rate for updating the competition information.
         *
         * @param rate The refresh rate in ticks.
         * @return The current {@code Builder} instance.
         */
        Builder refreshRate(int rate);

        /**
         * Sets the interval for switching between different competition texts.
         *
         * @param interval The switch interval in ticks.
         * @return The current {@code Builder} instance.
         */
        Builder switchInterval(int interval);

        /**
         * Sets the texts to be displayed on the boss bar during the competition.
         *
         * @param texts An array of competition information texts.
         * @return The current {@code Builder} instance.
         */
        Builder text(String[] texts);

        /**
         * Sets the color of the boss bar.
         *
         * @param color The color of the boss bar.
         * @return The current {@code Builder} instance.
         */
        Builder color(BossBar.Color color);

        /**
         * Sets the overlay style of the boss bar.
         *
         * @param overlay The overlay style of the boss bar.
         * @return The current {@code Builder} instance.
         */
        Builder overlay(BossBar.Overlay overlay);

        Builder enable(boolean enable);

        /**
         * Builds the {@code BossBarConfig} object with the configured settings.
         *
         * @return The constructed {@code BossBarConfig} object.
         */
        BossBarConfig build();
    }
}
