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

package net.momirealms.customfishing.fishing.requirements;

import net.momirealms.customfishing.fishing.FishingCondition;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WeatherImpl extends Requirement implements RequirementInterface {

    private final List<String> weathers;

    public WeatherImpl(@Nullable String[] msg, List<String> weathers) {
        super(msg);
        this.weathers = weathers;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        World world = fishingCondition.getLocation().getWorld();
        String currentWeather;
        if (world.isThundering()) currentWeather = "thunder";
        else if (world.isClearWeather()) currentWeather = "clear";
        else currentWeather = "rain";
        for (String weather : weathers) {
            if (weather.equalsIgnoreCase(currentWeather)) {
                return true;
            }
        }
        notMetMessage(fishingCondition.getPlayer());
        return false;
    }
}
