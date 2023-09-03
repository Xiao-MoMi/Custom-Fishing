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

package net.momirealms.customfishing.api.mechanic.competition;

public abstract class AbstractCompetitionInfo {

    protected int refreshRate;
    protected int switchInterval;
    protected boolean showToAll;
    protected String[] texts;

    public int getRefreshRate() {
        return refreshRate;
    }

    public int getSwitchInterval() {
        return switchInterval;
    }

    public boolean isShowToAll() {
        return showToAll;
    }

    public String[] getTexts() {
        return texts;
    }
}
