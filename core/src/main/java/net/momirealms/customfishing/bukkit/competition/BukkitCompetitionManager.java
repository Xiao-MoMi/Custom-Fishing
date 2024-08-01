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

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionSchedule;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.bukkit.storage.method.database.nosql.RedisManager;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BukkitCompetitionManager implements CompetitionManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<CompetitionSchedule, CompetitionConfig> timeConfigMap;
    private final HashMap<String, CompetitionConfig> commandConfigMap;
    private Competition currentCompetition;
    private SchedulerTask timerCheckTask;
    private int nextCompetitionSeconds;
    private int interval;
    private final UUID identifier;
    private final ConcurrentHashMap<UUID, PlayerCount> playerCountMap;
    private RedisPlayerCount redisPlayerCount;

    public BukkitCompetitionManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.identifier = UUID.randomUUID();
        this.timeConfigMap = new HashMap<>();
        this.commandConfigMap = new HashMap<>();
        this.playerCountMap = new ConcurrentHashMap<>();
        this.redisPlayerCount = null;
    }

    @Override
    public void load() {
        this.interval = 10;
        this.timerCheckTask = plugin.getScheduler().asyncRepeating(
                this::timerCheck,
                1,
                1,
                TimeUnit.SECONDS
        );
        plugin.debug("Loaded " + commandConfigMap.size() + " competitions");

        if (ConfigManager.redisRanking()) {
            if (this.redisPlayerCount == null) {
                this.redisPlayerCount = new RedisPlayerCount(this.interval);
            }
        } else {
            if (this.redisPlayerCount != null) {
                this.redisPlayerCount.cancel();
                this.redisPlayerCount = null;
            }
        }
    }

    @Override
    public void unload() {
        if (this.timerCheckTask != null)
            this.timerCheckTask.cancel();
        if (currentCompetition != null && currentCompetition.isOnGoing())
            this.currentCompetition.stop(true);
        if (this.redisPlayerCount != null)
            this.redisPlayerCount.cancel();
        this.commandConfigMap.clear();
        this.timeConfigMap.clear();
    }

    @Override
    public void disable() {
        if (this.timerCheckTask != null)
            this.timerCheckTask.cancel();
        if (currentCompetition != null && currentCompetition.isOnGoing())
            this.currentCompetition.stop(false);
        if (this.redisPlayerCount != null) {
            this.redisPlayerCount.cancel();
            this.redisPlayerCount = null;
        }
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
            int players = onlinePlayerCountProvider();
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
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
                out.writeUTF(serverGroup);
                out.writeUTF("competition");
                out.writeUTF("start");
                out.writeUTF(config.id());
                RedisManager.getInstance().publishRedisMessage(byteArrayOutputStream.toString(StandardCharsets.UTF_8));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
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

    @Override
    public int onlinePlayerCountProvider() {
        if (ConfigManager.redisRanking()) {
            int count = 0;
            List<UUID> toRemove = new ArrayList<>();
            for (Map.Entry<UUID, PlayerCount> entry : playerCountMap.entrySet()) {
                PlayerCount playerCount = entry.getValue();
                if ((System.currentTimeMillis() - playerCount.time) < interval * 1000L + 2333L) {
                    count += playerCount.count;
                } else {
                    toRemove.add(entry.getKey());
                }
            }
            for (UUID uuid : toRemove) {
                playerCountMap.remove(uuid);
            }
            return count;
        } else {
            return Bukkit.getOnlinePlayers().size();
        }
    }

    @Override
    public void updatePlayerCount(UUID uuid, int count) {
        playerCountMap.put(uuid, new PlayerCount(count, System.currentTimeMillis()));
    }

    private class RedisPlayerCount implements Runnable {
        private final SchedulerTask task;

        public RedisPlayerCount(int interval) {
            task = plugin.getScheduler().asyncRepeating(this, interval, interval, TimeUnit.SECONDS);
        }

        @Override
        public void run() {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 DataOutputStream out = new DataOutputStream(byteArrayOutputStream)) {
                out.writeUTF(ConfigManager.serverGroup());
                out.writeUTF("online");
                out.writeUTF(String.valueOf(identifier));
                out.writeInt(Bukkit.getOnlinePlayers().size());
                RedisManager.getInstance().publishRedisMessage(byteArrayOutputStream.toString(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            task.cancel();
        }
    }

    private static class PlayerCount {
        int count;
        long time;

        public PlayerCount(int count, long time) {
            this.count = count;
            this.time = time;
        }
    }
}
