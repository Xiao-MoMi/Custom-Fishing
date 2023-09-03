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
 *
 */

package net.momirealms.customfishing.mechanic.competition;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.competition.*;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.Config;
import net.momirealms.customfishing.storage.method.database.nosql.RedisManager;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class CompetitionManagerImpl implements CompetitionManager {

    private final CustomFishingPlugin plugin;
    private final HashMap<CompetitionSchedule, CompetitionConfig> timeConfigMap;
    private final HashMap<String, CompetitionConfig> commandConfigMap;
    private Competition currentCompetition;
    private CancellableTask timerCheckTask;
    private int nextCompetitionSeconds;

    public CompetitionManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.timeConfigMap = new HashMap<>();
        this.commandConfigMap = new HashMap<>();
    }

    public void load() {
        loadConfig();
        this.timerCheckTask = plugin.getScheduler().runTaskAsyncTimer(
                this::timerCheck,
                1,
                1,
                TimeUnit.SECONDS
        );
    }

    public void unload() {
        if (this.timerCheckTask != null && !this.timerCheckTask.isCancelled())
            this.timerCheckTask.cancel();
        this.commandConfigMap.clear();
        this.timeConfigMap.clear();
        if (currentCompetition != null && currentCompetition.isOnGoing())
            currentCompetition.end();
    }

    public void disable() {
        if (this.timerCheckTask != null && !this.timerCheckTask.isCancelled())
            this.timerCheckTask.cancel();
        this.commandConfigMap.clear();
        this.timeConfigMap.clear();
        if (currentCompetition != null && currentCompetition.isOnGoing())
            currentCompetition.stop();
    }

    @Override
    public Set<String> getAllCompetitions() {
        return commandConfigMap.keySet();
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("competitions")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile()) {
                        this.loadSingleFileCompetition(subFile);
                    }
                }
            }
        }
    }

    private void loadSingleFileCompetition(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {

                CompetitionConfig.Builder builder = new CompetitionConfig.Builder(entry.getKey())
                        .goal(CompetitionGoal.valueOf(section.getString("goal", "TOTAL_SCORE").toUpperCase(Locale.ENGLISH)))
                        .minPlayers(section.getInt("min-players", 0))
                        .duration(section.getInt("duration", 300))
                        .rewards(getPrizeActions(section.getConfigurationSection("rewards")))
                        .startActions(plugin.getActionManager().getActions(section.getConfigurationSection("start-actions")))
                        .endActions(plugin.getActionManager().getActions(section.getConfigurationSection("end-actions")))
                        .skipActions(plugin.getActionManager().getActions(section.getConfigurationSection("skip-actions")));

                if (section.getBoolean("bossbar.enable", false)) {
                    builder.bossbar(new BossBarConfig.Builder()
                            .color(BarColor.valueOf(section.getString("bossbar.color", "WHITE").toUpperCase(Locale.ENGLISH)))
                            .overlay(BossBarConfig.Overlay.valueOf(section.getString("bossbar.overlay", "PROGRESS").toUpperCase(Locale.ENGLISH)))
                            .refreshRate(section.getInt("bossbar.refresh-rate", 20))
                            .switchInterval(section.getInt("bossbar.switch-interval", 200))
                            .showToAll(!section.getBoolean("bossbar.only-show-to-participants", true))
                            .text(section.getStringList("bossbar.text").toArray(new String[0]))
                            .build());
                }

                if (section.getBoolean("actionbar.enable", false)) {
                    builder.actionbar(new ActionBarConfig.Builder()
                            .refreshRate(section.getInt("actionbar.refresh-rate", 5))
                            .switchInterval(section.getInt("actionbar.switch-interval", 200))
                            .showToAll(!section.getBoolean("actionbar.only-show-to-participants", true))
                            .text(section.getStringList("actionbar.text").toArray(new String[0]))
                            .build());
                }

                CompetitionConfig competitionConfig = builder.build();
                List<Pair<Integer, Integer>> timePairs = section.getStringList("start-time")
                        .stream().map(this::getTimePair).toList();
                List<Integer> weekdays = section.getIntegerList("start-weekday");
                if (weekdays.size() == 0) {
                    weekdays.addAll(List.of(1,2,3,4,5,6,7));
                }
                for (Integer weekday : weekdays) {
                    for (Pair<Integer, Integer> timePair : timePairs) {
                        CompetitionSchedule schedule = new CompetitionSchedule(weekday, timePair.left(), timePair.right(), 0);
                        timeConfigMap.put(schedule, competitionConfig);
                    }
                }
                commandConfigMap.put(entry.getKey(), competitionConfig);
            }
        }
    }

    public HashMap<String, Action[]> getPrizeActions(ConfigurationSection section) {
        HashMap<String, Action[]> map = new HashMap<>();
        if (section == null) return map;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection innerSection) {
                map.put(entry.getKey(), plugin.getActionManager().getActions(innerSection));
            }
        }
        return map;
    }

    public Pair<Integer, Integer> getTimePair(String time) {
        String[] split = time.split(":");
        return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public void timerCheck() {
        LocalDateTime now = LocalDateTime.now();
        CompetitionSchedule competitionSchedule = new CompetitionSchedule(
                now.getDayOfWeek().getValue(),
                now.getHour(),
                now.getMinute(),
                now.getSecond()
        );
        int seconds = competitionSchedule.getTotalSeconds();
        int nextCompetitionTime = 7 * 24 * 60 * 60;
        for (CompetitionSchedule schedule : timeConfigMap.keySet()) {
            nextCompetitionTime = Math.min(nextCompetitionTime, schedule.getTimeDelta(seconds));
        }
        this.nextCompetitionSeconds = nextCompetitionTime;
        CompetitionConfig config = timeConfigMap.get(competitionSchedule);
        if (config != null) {
            startCompetition(config, false, false);
        }
    }

    @Override
    public void startCompetition(String competition, boolean force, boolean allServer) {
        CompetitionConfig config = commandConfigMap.get(competition);
        if (config == null) {
            LogUtils.warn("Competition " + competition + " doesn't exist.");
            return;
        }
        startCompetition(config, force, allServer);
    }

    @Override
    @Nullable
    public FishingCompetition getOnGoingCompetition() {
        if (currentCompetition == null) return null;
        return currentCompetition.isOnGoing() ? currentCompetition : null;
    }

    @Override
    public void startCompetition(CompetitionConfig config, boolean force, boolean allServer) {
        if (!force)
            this.getPlayerCount().thenAccept(count -> {
               if (count < config.getMinPlayers()) {
                   var actions = config.getSkipActions();
                    if (actions != null)
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            for (Action action : actions) {
                                action.trigger(new Condition(player));
                            }
                        }
                   return;
               }
               start(config);
            });
        else if (!allServer) {
            start(config);
        } else {
            RedisManager.getInstance().sendRedisMessage("cf_competition", "start;" + config.getKey());
        }
    }

    private void start(CompetitionConfig config) {
        if (getOnGoingCompetition() != null) {
            currentCompetition.end();
            plugin.getScheduler().runTaskAsyncLater(() -> {
                this.currentCompetition = new Competition(config);
                this.currentCompetition.start();
            }, 1, TimeUnit.SECONDS);
        } else {
            this.currentCompetition = new Competition(config);
            this.currentCompetition.start();
        }
    }

    @Override
    public int getNextCompetitionSeconds() {
        return nextCompetitionSeconds;
    }

    @Override
    public CompletableFuture<Integer> getPlayerCount() {
        if (!Config.redisRanking) {
            return CompletableFuture.completedFuture(Bukkit.getOnlinePlayers().size());
        } else {
            return plugin.getStorageManager().getRedisPlayerCount();
        }
    }

    @Nullable
    @Override
    public CompetitionConfig getConfig(String key) {
        return commandConfigMap.get(key);
    }
}
