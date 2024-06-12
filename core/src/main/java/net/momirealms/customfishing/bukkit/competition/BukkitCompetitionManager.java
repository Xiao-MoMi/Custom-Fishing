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

package net.momirealms.customfishing.bukkit.competition;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.bossbar.BossBar;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.competition.info.ActionBarConfig;
import net.momirealms.customfishing.api.mechanic.competition.info.BossBarConfig;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.bukkit.storage.method.database.nosql.RedisManager;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BukkitCompetitionManager implements CompetitionManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<CompetitionSchedule, CompetitionConfig> timeConfigMap;
    private final HashMap<String, CompetitionConfig> commandConfigMap;
    private Competition currentCompetition;
    private SchedulerTask timerCheckTask;
    private int nextCompetitionSeconds;

    public BukkitCompetitionManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.timeConfigMap = new HashMap<>();
        this.commandConfigMap = new HashMap<>();
    }

    public void load() {
        loadConfig();
        this.timerCheckTask = plugin.getScheduler().asyncRepeating(
                this::timerCheck,
                1,
                1,
                TimeUnit.SECONDS
        );
    }

    public void unload() {
        if (this.timerCheckTask != null)
            this.timerCheckTask.cancel();
        if (currentCompetition != null && currentCompetition.isOnGoing())
            this.currentCompetition.stop(true);
        this.commandConfigMap.clear();
        this.timeConfigMap.clear();
    }

    public void disable() {
        if (this.timerCheckTask != null)
            this.timerCheckTask.cancel();
        if (currentCompetition != null && currentCompetition.isOnGoing())
            this.currentCompetition.stop(false);
        this.commandConfigMap.clear();
        this.timeConfigMap.clear();
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("competition")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.getBoostrap().saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
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
        YamlDocument document = plugin.getConfigManager().loadData(file);
        for (Map.Entry<String, Object> entry : document.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section section) {
                CompetitionConfig.Builder builder = CompetitionConfig.builder()
                        .key(entry.getKey())
                        .goal(CompetitionGoal.index().value(section.getString("goal", "TOTAL_SCORE").toLowerCase(Locale.ENGLISH)))
                        .minPlayers(section.getInt("min-players", 0))
                        .duration(section.getInt("duration", 300))
                        .rewards(getPrizeActions(section.getSection("rewards")))
                        .joinRequirements(plugin.getRequirementManager().parseRequirements(section.getSection("participate-requirements"), false))
                        .joinActions(plugin.getActionManager().parseActions(section.getSection("participate-actions")))
                        .startActions(plugin.getActionManager().parseActions(section.getSection("start-actions")))
                        .endActions(plugin.getActionManager().parseActions(section.getSection("end-actions")))
                        .skipActions(plugin.getActionManager().parseActions(section.getSection("skip-actions")));;
                if (section.getBoolean("bossbar.enable", false)) {
                    builder.bossBarConfig(
                            BossBarConfig.builder()
                                .enable(true)
                                .color(BossBar.Color.valueOf(section.getString("bossbar.color", "WHITE").toUpperCase(Locale.ENGLISH)))
                                .overlay(BossBar.Overlay.valueOf(section.getString("bossbar.overlay", "PROGRESS").toUpperCase(Locale.ENGLISH)))
                                .refreshRate(section.getInt("bossbar.refresh-rate", 20))
                                .switchInterval(section.getInt("bossbar.switch-interval", 200))
                                .showToAll(!section.getBoolean("bossbar.only-show-to-participants", true))
                                .text(section.getStringList("bossbar.text").toArray(new String[0]))
                                .build()
                    );
                }
                if (section.getBoolean("actionbar.enable", false)) {
                    builder.actionBarConfig(
                            ActionBarConfig.builder()
                                .enable(true)
                                .refreshRate(section.getInt("actionbar.refresh-rate", 5))
                                .switchInterval(section.getInt("actionbar.switch-interval", 200))
                                .showToAll(!section.getBoolean("actionbar.only-show-to-participants", true))
                                .text(section.getStringList("actionbar.text").toArray(new String[0]))
                                .build()
                    );
                }
                CompetitionConfig competitionConfig = builder.build();
                List<Pair<Integer, Integer>> timePairs = section.getStringList("start-time")
                        .stream().map(it -> {
                            String[] split = it.split(":");
                            return Pair.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                        }).toList();
                List<Integer> weekdays = section.getIntList("start-weekday");
                if (weekdays.isEmpty()) {
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
    public HashMap<String, Action<Player>[]> getPrizeActions(Section section) {
        HashMap<String, Action<Player>[]> map = new HashMap<>();
        if (section == null) return map;
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section innerSection) {
                map.put(entry.getKey(), plugin.getActionManager().parseActions(innerSection));
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

    @Override
    public boolean startCompetition(String competition, boolean force, String serverGroup) {
        CompetitionConfig config = commandConfigMap.get(competition);
        if (config == null) {
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
            if (players < config.minPlayersToStart()) {
                ActionManager.trigger(Context.player(null), config.skipActions());
                return false;
            }
            start(config);
            return true;
        } else if (serverGroup == null) {
            start(config);
            return true;
        } else {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF(serverGroup);
            out.writeUTF("competition");
            out.writeUTF("start");
            out.writeUTF(config.key());
            RedisManager.getInstance().publishRedisMessage(Arrays.toString(out.toByteArray()));
            return true;
        }
    }

    private void start(CompetitionConfig config) {
        if (getOnGoingCompetition() != null) {
            // END
            currentCompetition.end(true);
            plugin.getScheduler().asyncLater(() -> {
                // start one second later
                this.currentCompetition = new Competition(plugin, config);
                this.currentCompetition.start(true);
            }, 1, TimeUnit.SECONDS);
        } else {
            // start instantly
            plugin.getScheduler().async().execute(() -> {
                this.currentCompetition = new Competition(plugin, config);
                this.currentCompetition.start(true);
            });
        }
    }

    /**
     * Gets the number of seconds until the next competition.
     *
     * @return The number of seconds until the next competition.
     */
    @Override
    public int getNextCompetitionInSeconds() {
        return nextCompetitionSeconds;
    }

    @Nullable
    @Override
    public CompetitionConfig getCompetition(String key) {
        return commandConfigMap.get(key);
    }

    @Override
    public Collection<String> getCompetitionIDs() {
        return commandConfigMap.keySet();
    }
}
