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

package net.momirealms.customfishing.mechanic.competition;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.competition.*;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.setting.CFLocale;
import net.momirealms.customfishing.storage.method.database.nosql.RedisManager;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
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
                () -> {
                    try {
                        timerCheck();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
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

    /**
     * Retrieves a set of all competition names.
     *
     * @return A set of competition names.
     */
    @NotNull
    @Override
    public Set<String> getAllCompetitionKeys() {
        return commandConfigMap.keySet();
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("competition")) {
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
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
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
                        .stream().map(it -> ConfigUtils.splitStringIntegerArgs(it, ":")).toList();
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

    /**
     * Gets prize actions from a configuration section.
     *
     * @param section The configuration section containing prize actions.
     * @return A HashMap where keys are action names and values are arrays of Action objects.
     */
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

    /**
     * Checks the timer for the next competition and starts it if necessary.
     */
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
            startCompetition(config, false, null);
        }
    }

    /**
     * Retrieves the localization key for a given competition goal.
     *
     * @param goal The competition goal to retrieve the localization key for.
     * @return The localization key for the specified competition goal.
     */
    @NotNull
    @Override
    public String getCompetitionGoalLocale(CompetitionGoal goal) {
        switch (goal) {
            case MAX_SIZE -> {
                return CFLocale.MSG_Max_Size;
            }
            case CATCH_AMOUNT -> {
                return CFLocale.MSG_Catch_Amount;
            }
            case TOTAL_SCORE -> {
                return CFLocale.MSG_Total_Score;
            }
            case TOTAL_SIZE -> {
                return CFLocale.MSG_Total_Size;
            }
        }
        return "";
    }

    @Override
    public boolean startCompetition(String competition, boolean force, String serverGroup) {
        CompetitionConfig config = commandConfigMap.get(competition);
        if (config == null) {
            LogUtils.warn("Competition " + competition + " doesn't exist.");
            return false;
        }
        return startCompetition(config, force, serverGroup);
    }

    /**
     * Gets the ongoing fishing competition, if one is currently in progress.
     *
     * @return The ongoing fishing competition, or null if there is none.
     */
    @Override
    @Nullable
    public FishingCompetition getOnGoingCompetition() {
        if (currentCompetition == null) return null;
        return currentCompetition.isOnGoing() ? currentCompetition : null;
    }

    @Override
    public boolean startCompetition(CompetitionConfig config, boolean force, @Nullable String serverGroup) {
        if (!force) {
            int players = Bukkit.getOnlinePlayers().size();
            if (players < config.getMinPlayersToStart()) {
                var actions = config.getSkipActions();
                if (actions != null) {
                    Condition condition = new Condition(null, null, new HashMap<>());
                    for (Action action : actions) {
                        action.trigger(condition);
                    }
                }
                return false;
            }
            start(config);
            return true;
        } else if (serverGroup == null) {
            start(config);
            return true;
        } else {
            RedisManager.getInstance().publishRedisMessage(serverGroup, "start;" + config.getKey());
            return true;
        }
    }

    private void start(CompetitionConfig config) {
        if (getOnGoingCompetition() != null) {
            // END
            currentCompetition.end();
            plugin.getScheduler().runTaskAsyncLater(() -> {
                // start one second later
                this.currentCompetition = new Competition(config);
                this.currentCompetition.start();
            }, 1, TimeUnit.SECONDS);
        } else {
            // start instantly
            plugin.getScheduler().runTaskAsync(() -> {
                this.currentCompetition = new Competition(config);
                this.currentCompetition.start();
            });
        }
    }

    /**
     * Gets the number of seconds until the next competition.
     *
     * @return The number of seconds until the next competition.
     */
    @Override
    public int getNextCompetitionSeconds() {
        return nextCompetitionSeconds;
    }

    /**
     * Retrieves the configuration for a competition based on its key.
     *
     * @param key The key of the competition configuration to retrieve.
     * @return The {@link CompetitionConfig} for the specified key, or {@code null} if no configuration exists with that key.
     */
    @Nullable
    @Override
    public CompetitionConfig getConfig(String key) {
        return commandConfigMap.get(key);
    }
}
