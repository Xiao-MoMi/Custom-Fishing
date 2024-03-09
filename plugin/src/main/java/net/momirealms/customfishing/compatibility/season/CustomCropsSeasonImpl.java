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

import net.momirealms.customcrops.api.CustomCropsPlugin;
import net.momirealms.customcrops.api.mechanic.world.season.Season;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class CustomCropsSeasonImpl implements SeasonInterface {

    @NotNull
    @Override
    public String getSeason(World world) {
        Season season = CustomCropsPlugin.get().getIntegrationManager().getSeason(world);
        if (season == null) return "disabled";
        return season.name().toUpperCase(Locale.ENGLISH);
    }
}