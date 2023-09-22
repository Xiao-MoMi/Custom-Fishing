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

package net.momirealms.customfishing.compatibility.season;

import net.momirealms.customcrops.api.CustomCropsAPI;
import net.momirealms.customcrops.api.object.CCWorldSeason;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class CustomCropsSeasonImpl implements SeasonInterface {

    private final CustomCropsAPI customCropsAPI;

    public CustomCropsSeasonImpl() {
        customCropsAPI = CustomCropsAPI.getInstance();
    }

    @NotNull
    @Override
    public String getSeason(World world) {
        CCWorldSeason season = customCropsAPI.getSeason(world.getName());
        if (season == null) return "disabled";
        return season.getSeason();
    }
}