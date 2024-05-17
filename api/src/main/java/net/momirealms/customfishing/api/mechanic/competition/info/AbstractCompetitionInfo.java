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

/**
 * Abstract base class for competition information.
 * Contains common properties and methods for competition info.
 */
public abstract class AbstractCompetitionInfo {
    
    protected int refreshRate;
    protected int switchInterval;
    protected boolean showToAll;
    protected String[] texts;

    protected AbstractCompetitionInfo(int refreshRate, int switchInterval, boolean showToAll, String[] texts) {
        this.refreshRate = refreshRate;
        this.switchInterval = switchInterval;
        this.showToAll = showToAll;
        this.texts = texts;
    }

    /**
     * Get the refresh rate for updating competition information.
     *
     * @return The refresh rate in ticks.
     */
    public int refreshRate() {
        return refreshRate;
    }

    /**
     * Get the switch interval for displaying different competition texts.
     *
     * @return The switch interval in ticks.
     */
    public int switchInterval() {
        return switchInterval;
    }

    /**
     * Check if competition information should be shown to all players.
     *
     * @return True if information is shown to all players, otherwise only to participants.
     */
    public boolean showToAll() {
        return showToAll;
    }

    /**
     * Get an array of competition information texts.
     *
     * @return An array of competition information texts.
     */
    public String[] texts() {
        return texts;
    }
}
