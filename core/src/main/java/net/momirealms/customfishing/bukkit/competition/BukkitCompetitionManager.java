/*
 *  Copyright (C) <2024> <XiaoMoMi>
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
import net.momirealms.customfishing.api.mechanic.competition.*;
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
        this.timerCheckTask = plugin.getScheduler().asyncRepeating(
                this::timerCheck,
                1,
                1,
                TimeUnit.SECONDS
        );
        plugin.debug("Loaded " + commandConfigMap.size() + " competitions");
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

    @Override
    public boolean registerCompetition(CompetitionConfig competitionConfig) {
        if (commandConfigMap.containsKey(competitionConfig.id())) {
            return false;
        }
        for (CompetitionSchedule schedule : competitionConfig.schedules()) {
            timeConfigMap.put(schedule, competitionConfig);
        }
        commandConfigMap.put(competitionConfig.id(), competitionConfig);
        return true;
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
            out.writeUTF(config.id());
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
