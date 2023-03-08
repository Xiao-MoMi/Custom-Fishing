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

package net.momirealms.customfishing.object;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.fishing.competition.ranking.RankingInterface;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.manager.MessageManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DynamicText {

    private final Player owner;
    private String originalValue;
    private String latestValue;
    private String[] ownerPlaceholders;

    public DynamicText(Player owner, String rawValue) {
        this.owner = owner;
        analyze(rawValue);
    }

    private void analyze(String value) {
        List<String> placeholdersOwner = new ArrayList<>(CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager().detectBasicPlaceholders(value));
        String origin = value;
        for (String placeholder : placeholdersOwner) {
            origin = origin.replace(placeholder, "%s");
        }
        originalValue = origin;
        ownerPlaceholders = placeholdersOwner.toArray(new String[0]);
        latestValue = originalValue;
        update();
    }

    public String getLatestValue() {
        return latestValue;
    }

    public boolean update() {
        String string = originalValue;
        if (ownerPlaceholders.length != 0) {
            PlaceholderManager placeholderManager = CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager();
            if ("%s".equals(originalValue)) {
                string = placeholderManager.parse(owner, ownerPlaceholders[0]);
            }
            else {
                Object[] values = new String[ownerPlaceholders.length];
                for (int i = 0; i < ownerPlaceholders.length; i++) {
                    values[i] = placeholderManager.parse(owner, ownerPlaceholders[i]);
                }
                string = String.format(originalValue, values);
            }
        }

        RankingInterface ranking = Competition.currentCompetition.getRanking();

        string = string .replace("{rank}", Competition.currentCompetition.getPlayerRank(owner))
                        .replace("{time}", String.valueOf(Competition.currentCompetition.getRemainingTime()))
                        .replace("{minute}", String.format("%02d", Competition.currentCompetition.getRemainingTime() / 60))
                        .replace("{second}",String.format("%02d", Competition.currentCompetition.getRemainingTime() % 60))
                        .replace("{score}", String.format("%.1f", Competition.currentCompetition.getScore(owner)))
                        .replace("{1st_player}", Optional.ofNullable(ranking.getPlayerAt(1)).orElse(MessageManager.noPlayer))
                        .replace("{1st_score}", ranking.getScoreAt(1) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(1)))
                        .replace("{2nd_player}", Optional.ofNullable(ranking.getPlayerAt(2)).orElse(MessageManager.noPlayer))
                        .replace("{2nd_score}", ranking.getScoreAt(2) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(2)))
                        .replace("{3rd_player}", Optional.ofNullable(ranking.getPlayerAt(3)).orElse(MessageManager.noPlayer))
                        .replace("{3rd_score}", ranking.getScoreAt(3) <= 0 ? MessageManager.noScore : String.format("%.1f", ranking.getScoreAt(3)));

        if (!latestValue.equals(string)) {
            latestValue = string;
            return true;
        }
        return false;
    }
}
