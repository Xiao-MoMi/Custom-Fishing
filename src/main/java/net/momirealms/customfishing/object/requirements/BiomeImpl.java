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

package net.momirealms.customfishing.object.requirements;

import net.momirealms.biomeapi.BiomeAPI;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.fishing.FishingCondition;

import java.util.List;

public record BiomeImpl(List<String> biomes) implements RequirementInterface {

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        String currentBiome = BiomeAPI.getBiome(fishingCondition.getLocation());
        for (String biome : biomes) {
            if (currentBiome.equalsIgnoreCase(biome)) {
                return true;
            }
        }
        return false;
    }
}
