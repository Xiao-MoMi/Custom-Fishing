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

package net.momirealms.customfishing.fishing.competition;

import net.momirealms.customfishing.manager.MessageManager;

public enum CompetitionGoal {

    CATCH_AMOUNT(MessageManager.CATCH_AMOUNT),
    TOTAL_SCORE(MessageManager.TOTAL_SCORE),
    MAX_SIZE(MessageManager.MAX_SIZE),
    TOTAL_SIZE(MessageManager.TOTAL_SIZE),
    RANDOM("Random");

    private final String display;
    CompetitionGoal(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

}
