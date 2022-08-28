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

package net.momirealms.customfishing.requirements;

import net.momirealms.customfishing.ConfigReader;

import java.util.List;

public record Season(List<String> seasons) implements Requirement {

    public List<String> getSeasons() {
        return this.seasons;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        String currentSeason = ConfigReader.Config.season.getSeason(fishingCondition.getLocation().getWorld());
        for (String season : seasons) {
            if (season.equalsIgnoreCase(currentSeason)) {
                return true;
            }
        }
        return false;
    }
}