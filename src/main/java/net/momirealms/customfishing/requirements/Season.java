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

import me.clip.placeholderapi.PlaceholderAPI;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.hook.CustomCropsSeason;
import net.momirealms.customfishing.hook.RealisticSeason;
import org.bukkit.ChatColor;

import java.util.List;

public record Season(List<String> seasons) implements Requirement {

    public List<String> getSeasons() {
        return this.seasons;
    }

    @Override
    public boolean isConditionMet(FishingCondition fishingCondition) {
        String currentSeason;
        if (ConfigReader.Config.rsSeason){
            currentSeason = RealisticSeason.getSeason(fishingCondition.getLocation().getWorld());
        }else if(ConfigReader.Config.ccSeason){
            currentSeason = CustomCropsSeason.getSeason(fishingCondition.getLocation().getWorld());
        }else {
            currentSeason = ChatColor.stripColor(PlaceholderAPI.setPlaceholders(fishingCondition.getPlayer(), ConfigReader.Config.season_papi));
        }
        for (String season : seasons) {
            if (season.equalsIgnoreCase(currentSeason)) {
                return true;
            }
        }
        return false;
    }
}