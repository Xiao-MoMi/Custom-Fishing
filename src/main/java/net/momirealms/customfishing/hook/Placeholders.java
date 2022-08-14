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

package net.momirealms.customfishing.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.competition.Competition;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class Placeholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "competition";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XiaoMoMi";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("timeleft")){
            if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()){
                return String.valueOf(Competition.remainingTime);
            }else {
                return "0";
            }
        }
        if (params.equalsIgnoreCase("rank")){
            if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()){
                return Optional.ofNullable(CompetitionSchedule.competition.getRanking().getPlayerRank(player.getName())).orElse(ConfigReader.Message.noRank);
            }else {
                return ConfigReader.Message.noRank;
            }
        }
        return null;
    }
}
