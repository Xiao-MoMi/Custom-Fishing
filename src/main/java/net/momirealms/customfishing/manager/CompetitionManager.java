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

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.action.CommandActionImpl;
import net.momirealms.customfishing.fishing.action.MessageActionImpl;
import net.momirealms.customfishing.fishing.competition.CompetitionConfig;
import net.momirealms.customfishing.fishing.competition.CompetitionGoal;
import net.momirealms.customfishing.fishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.fishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.fishing.competition.bossbar.BossBarOverlay;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class CompetitionManager extends Function {

    private final CustomFishing plugin;
    // Competitions that can be triggered at a specified time
    private final HashMap<String, CompetitionConfig> competitionsT;
    // Competitions that can be triggered with a command
    private final HashMap<String, CompetitionConfig> competitionsC;
    private CompetitionSchedule competitionSchedule;

    public CompetitionManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.competitionsC = new HashMap<>();
        this.competitionsT = new HashMap<>();
    }

    @Override
    public void load() {
        if (ConfigManager.enableCompetition) {
            loadCompetitions();
            this.competitionSchedule = new CompetitionSchedule();
            this.competitionSchedule.load();
        }
    }

    @Override
    public void unload() {
        this.competitionsC.clear();
        this.competitionsT.clear();
        if (this.competitionSchedule != null) {
            this.competitionSchedule.unload();
        }
    }

    private void loadCompetitions() {
        File competition_file = new File(plugin.getDataFolder() + File.separator + "competitions");
        if (!competition_file.exists()) {
            if (!competition_file.mkdir()) return;
            plugin.saveResource("competitions" + File.separator + "default.yml", false);
        }
        File[] files = competition_file.listFiles();
        if (files == null) return;
        int amount = 0;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection competitionSection = config.getConfigurationSection(key);
                if (competitionSection == null) continue;
                boolean enableBsb = competitionSection.getBoolean("bossbar.enable", false);
                BossBarConfig bossBarConfig = new BossBarConfig(
                        competitionSection.getStringList("bossbar.text").toArray(new String[0]),
                        BossBarOverlay.valueOf(competitionSection.getString("bossbar.overlay","SOLID").toUpperCase()),
                        BarColor.valueOf(competitionSection.getString("bossbar.color","WHITE").toUpperCase()),
                        competitionSection.getInt("bossbar.refresh-rate",10),
                        competitionSection.getInt("bossbar.switch-interval", 200)
                );

                HashMap<String, Action[]> rewardsMap = new HashMap<>();
                Objects.requireNonNull(competitionSection.getConfigurationSection("prize")).getKeys(false).forEach(rank -> {
                    List<Action> rewards = new ArrayList<>();
                    if (competitionSection.contains("prize." + rank + ".messages"))
                        rewards.add(new MessageActionImpl(competitionSection.getStringList("prize." + rank + ".messages").toArray(new String[0]), null));
                    if (competitionSection.contains("prize." + rank + ".commands"))
                        rewards.add(new CommandActionImpl(competitionSection.getStringList("prize." + rank + ".commands").toArray(new String[0]), null));
                    rewardsMap.put(rank, rewards.toArray(new Action[0]));
                });

                CompetitionConfig competitionConfig = new CompetitionConfig(
                        competitionSection.getInt("duration",600),
                        competitionSection.getInt("min-players",1),
                        competitionSection.getStringList("broadcast.start"),
                        competitionSection.getStringList("broadcast.end"),
                        competitionSection.getStringList("command.start"),
                        competitionSection.getStringList("command.end"),
                        competitionSection.getStringList("command.join"),
                        CompetitionGoal.valueOf(competitionSection.getString("goal", "RANDOM")),
                        bossBarConfig,
                        enableBsb,
                        rewardsMap
                );

                if (competitionSection.contains("start-weekday")) {
                    List<Integer> days = new ArrayList<>();
                    for (String weekDay : competitionSection.getStringList("start-weekday")) {
                        switch (weekDay) {
                            case "Sunday" -> days.add(1);
                            case "Monday" -> days.add(2);
                            case "Tuesday" -> days.add(3);
                            case "Wednesday" -> days.add(4);
                            case "Thursday" -> days.add(5);
                            case "Friday" -> days.add(6);
                            case "Saturday" -> days.add(7);
                            default -> AdventureUtil.consoleMessage("[CustomFishing] Unknown weekday: " + weekDay);
                        }
                    }
                    competitionConfig.setWeekday(days);
                }

                if (competitionSection.contains("start-date")) {
                    List<Integer> days = new ArrayList<>();
                    for (String weekDay : competitionSection.getStringList("start-date")) {
                        days.add(Integer.parseInt(weekDay));
                    }
                    competitionConfig.setDate(days);
                }
                competitionSection.getStringList("start-time").forEach(time -> competitionsT.put(time, competitionConfig));
                competitionsC.put(key, competitionConfig);
                amount++;
            }
        }
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + amount + " <gray>competition(s)");
    }

    public HashMap<String, CompetitionConfig> getCompetitionsT() {
        return competitionsT;
    }

    public HashMap<String, CompetitionConfig> getCompetitionsC() {
        return competitionsC;
    }
}
