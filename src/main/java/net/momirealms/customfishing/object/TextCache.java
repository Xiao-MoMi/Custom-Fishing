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
import net.momirealms.customfishing.competition.Competition;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TextCache {

    //所属玩家
    private final Player owner;
    //初始值
    private final String rawValue;
    //原始文字加工后的值
    private String originalValue;
    //最近一次替换值
    private String latestValue;
    //持有者占位符
    private String[] ownerPlaceholders;

    public TextCache(Player owner, String rawValue) {
        this.owner = owner;
        this.rawValue = rawValue;
        analyze(this.rawValue);
    }

    private void analyze(String value) {
        List<String> placeholdersOwner = new ArrayList<>(CustomFishing.plugin.getIntegrationManager().getPlaceholderManager().detectPlaceholders(value));
        String origin = value;
        for (String placeholder : placeholdersOwner) {
            origin = origin.replace(placeholder, "%s");
        }
        originalValue = origin;
        ownerPlaceholders = placeholdersOwner.toArray(new String[0]);
        latestValue = originalValue;
        update();
    }

    public String getRawValue() {
        return rawValue;
    }

    public String updateAndGet() {
        update();
        return getLatestValue();
    }

    public String getLatestValue() {
        return latestValue;
    }

    //返回更新结果是否不一致
    public boolean update() {
        String string = originalValue;
        if (ownerPlaceholders.length != 0) {
            PlaceholderManager placeholderManager = CustomFishing.plugin.getIntegrationManager().getPlaceholderManager();
            if (placeholderManager != null) {
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
        }
        string = string.replace("{rank}", Competition.currentCompetition.getPlayerRank(owner))
                        .replace("{time}", String.valueOf(Competition.currentCompetition.getRemainingTime()))
                        .replace("{minute}", String.format("%02d", Competition.currentCompetition.getRemainingTime() / 60))
                        .replace("{second}",String.format("%02d", Competition.currentCompetition.getRemainingTime() % 60))
                        .replace("{score}", String.format("%.1f", Competition.currentCompetition.getScore(owner)))
                        .replace("{1st_score}", String.format("%.1f", Competition.currentCompetition.getFirstScore()))
                        .replace("{1st_player}", Competition.currentCompetition.getFirstPlayer());
        if (!latestValue.equals(string)) {
            latestValue = string;
            return true;
        }
        return false;
    }

}
