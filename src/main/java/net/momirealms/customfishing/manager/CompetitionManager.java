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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.competition.CompetitionConfig;
import net.momirealms.customfishing.competition.CompetitionGoal;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.competition.bossbar.BossBarOverlay;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.object.action.CommandActionImpl;
import net.momirealms.customfishing.object.action.MessageActionImpl;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ConfigUtil;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.*;

public class CompetitionManager extends Function {

    public static HashMap<String, CompetitionConfig> competitionsT;
    public static HashMap<String, CompetitionConfig> competitionsC;
    private CompetitionSchedule competitionSchedule;

    @Override
    public void load() {
        if (ConfigManager.enableCompetition) {
            competitionsC = new HashMap<>();
            competitionsT = new HashMap<>();
            loadCompetitions();
            this.competitionSchedule = new CompetitionSchedule();
            this.competitionSchedule.load();
        }
    }

    @Override
    public void unload() {
        if (competitionsC != null) competitionsC.clear();
        if (competitionsT != null) competitionsT.clear();
        if (competitionSchedule != null) competitionSchedule.unload();
    }

    public void loadCompetitions() {
        YamlConfiguration config = ConfigUtil.getConfig("competition.yml");
        Set<String> keys = config.getKeys(false);
        keys.forEach(key -> {
            boolean enableBsb = config.getBoolean(key + ".bossbar.enable", false);
            BossBarConfig bossBarConfig = new BossBarConfig(
                    config.getStringList(key + ".bossbar.text").toArray(new String[0]),
                    BossBarOverlay.valueOf(config.getString(key + ".bossbar.overlay","SOLID").toUpperCase()),
                    BarColor.valueOf(config.getString(key + ".bossbar.color","WHITE").toUpperCase()),
                    config.getInt(key + ".bossbar.refresh-rate",10),
                    config.getInt(key + ".bossbar.switch-interval", 200)
            );

            HashMap<String, ActionInterface[]> rewardsMap = new HashMap<>();
            Objects.requireNonNull(config.getConfigurationSection(key + ".prize")).getKeys(false).forEach(rank -> {
                List<ActionInterface> rewards = new ArrayList<>();
                if (config.contains(key + ".prize." + rank + ".messages"))
                    rewards.add(new MessageActionImpl(config.getStringList(key + ".prize." + rank + ".messages").toArray(new String[0]), null));
                if (config.contains(key + ".prize." + rank + ".commands"))
                    rewards.add(new CommandActionImpl(config.getStringList(key + ".prize." + rank + ".commands").toArray(new String[0]), null));
                rewardsMap.put(rank, rewards.toArray(new ActionInterface[0]));
            });

            CompetitionConfig competitionConfig = new CompetitionConfig(
                    config.getInt(key + ".duration",600),
                    config.getInt(key + ".min-players",1),
                    config.getStringList(key + ".broadcast.start"),
                    config.getStringList(key + ".broadcast.end"),
                    config.getStringList(key + ".command.start"),
                    config.getStringList(key + ".command.end"),
                    config.getStringList(key + ".command.join"),
                    CompetitionGoal.valueOf(config.getString(key + ".goal", "RANDOM")),
                    bossBarConfig,
                    enableBsb,
                    rewardsMap
            );

            if (config.contains(key + ".start-weekday")) {
                List<Integer> days = new ArrayList<>();
                for (String weekDay : config.getStringList(key + ".start-weekday")) {
                    switch (weekDay) {
                        case "Sunday" -> days.add(1);
                        case "Monday" -> days.add(2);
                        case "Tuesday" -> days.add(3);
                        case "Wednesday" -> days.add(4);
                        case "Thursday" -> days.add(5);
                        case "Friday" -> days.add(6);
                        case "Saturday" -> days.add(7);
                        default -> AdventureUtil.consoleMessage("unknown weekday: " + weekDay);
                    }
                }
                competitionConfig.setWeekday(days);
            }

            if (config.contains(key + ".start-date")) {
                List<Integer> days = new ArrayList<>();
                for (String weekDay : config.getStringList(key + ".start-date")) {
                    days.add(Integer.parseInt(weekDay));
                }
                competitionConfig.setDate(days);
            }

            config.getStringList(key + ".start-time").forEach(time -> competitionsT.put(time, competitionConfig));
            competitionsC.put(key, competitionConfig);
        });
    }
}
