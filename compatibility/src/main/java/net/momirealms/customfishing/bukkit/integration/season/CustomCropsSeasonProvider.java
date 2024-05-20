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

package net.momirealms.customfishing.bukkit.integration.season;

import net.momirealms.customcrops.api.CustomCropsPlugin;
import net.momirealms.customcrops.api.mechanic.world.season.Season;
import net.momirealms.customfishing.api.integration.SeasonProvider;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CustomCropsSeasonProvider implements SeasonProvider {

    @NotNull
    @Override
    public net.momirealms.customfishing.api.mechanic.misc.season.Season getSeason(@NotNull World world) {
        Season season = CustomCropsPlugin.get().getIntegrationManager().getSeasonInterface().getSeason(world);
        if (season == null) return net.momirealms.customfishing.api.mechanic.misc.season.Season.DISABLE;
        return net.momirealms.customfishing.api.mechanic.misc.season.Season.valueOf(season.name().toUpperCase(Locale.ENGLISH));
    }

    @Override
    public String identifier() {
        return "CustomCrops";
    }
}