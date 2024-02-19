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
import net.momirealms.customfishing.api.event.CompetitionEvent;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.competition.Ranking;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.mechanic.competition.actionbar.ActionBarManager;
import net.momirealms.customfishing.mechanic.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.mechanic.competition.ranking.LocalRankingImpl;
import net.momirealms.customfishing.mechanic.competition.ranking.RedisRankingImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.setting.CFLocale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Competition implements FishingCompetition {

    private final CompetitionConfig config;
    private CancellableTask competitionTimerTask;
    private final CompetitionGoal goal;
    private final ConcurrentHashMap<String, String> publicPlaceholders;
    private final Ranking ranking;
    private float progress;
    private long remainingTime;
    private long startTime;
    private BossBarManager bossBarManager;
    private ActionBarManager actionBarManager;

    public Competition(CompetitionConfig config) {
        this.config = config;
        this.goal = config.getGoal() == CompetitionGoal.RANDOM ? CompetitionGoal.getRandom() : config.getGoal();
        if (CFConfig.redisRanking) this.ranking = new RedisRankingImpl();
                            else this.ranking = new LocalRankingImpl();
        this.publicPlaceholders = new ConcurrentHashMap<>();
        this.publicPlaceholders.put("{goal}", CustomFishingPlugin.get().getCompetitionManager().getCompetitionGoalLocale(goal));
    }

    /**
     * Starts the fishing competition, initializing its settings and actions.
     * This method sets the initial progress, remaining time, start time, and updates public placeholders.
     * It also arranges timer tasks for competition timing and initializes boss bar and action bar managers if configured.
     * Additionally, it triggers the start actions defined in the competition's configuration.
     */
    @Override
    public void start() {
        this.progress = 1;
        this.remainingTime = config.getDurationInSeconds();
        this.startTime = Instant.now().getEpochSecond();

        this.arrangeTimerTask();
        if (config.getBossBarConfig() != null) {
            this.bossBarManager = new BossBarManager(config.getBossBarConfig(), this);
            this.bossBarManager.load();
        }
        if (config.getActionBarConfig() != null) {
            this.actionBarManager = new ActionBarManager(config.getActionBarConfig(), this);
            this.actionBarManager.load();
        }

        Action[] actions = config.getStartActions();
        if (actions != null) {
            Condition condition = new Condition(null, null, this.publicPlaceholders);
            for (Action action : actions) {
                action.trigger(condition);
            }
        }

        this.ranking.clear();
        this.updatePublicPlaceholders();

        CompetitionEvent competitionStartEvent = new CompetitionEvent(CompetitionEvent.State.START, this);
        Bukkit.getPluginManager().callEvent(competitionStartEvent);
    }

    /**
     * Arranges the timer task for the fishing competition.
     * This method schedules a recurring task that updates the competition's remaining time and public placeholders.
     * If the remaining time reaches zero, the competition is ended.
     */
    private void arrangeTimerTask() {
        this.competitionTimerTask = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(() -> {
            if (decreaseTime()) {
                end(true);
                return;
            }
            updatePublicPlaceholders();
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Update public placeholders for the fishing competition.
     * This method updates placeholders representing player rankings, remaining time, and score in public messages.
     * Placeholders for player rankings include {1_player}, {1_score}, {2_player}, {2_score}, and so on.
     * The placeholders for time include {hour}, {minute}, {second}, and {seconds}.
     */
    private void updatePublicPlaceholders() {
        for (int i = 1; i < CFConfig.placeholderLimit + 1; i++) {
            int finalI = i;
            Optional.ofNullable(ranking.getPlayerAt(i)).ifPresentOrElse(player -> {
                publicPlaceholders.put("{" + finalI + "_player}", player);
                publicPlaceholders.put("{" + finalI + "_score}", String.format("%.2f", ranking.getScoreAt(finalI)));
            }, () -> {
                publicPlaceholders.put("{" + finalI + "_player}", CFLocale.MSG_No_Player);
                publicPlaceholders.put("{" + finalI + "_score}", CFLocale.MSG_No_Score);
            });
        }
        publicPlaceholders.put("{hour}", remainingTime < 3600 ? "" : (remainingTime / 3600) + CFLocale.FORMAT_Hour);
        publicPlaceholders.put("{minute}", remainingTime < 60 ? "" : (remainingTime % 3600) / 60 + CFLocale.FORMAT_Minute);
        publicPlaceholders.put("{second}", remainingTime == 0 ? "" : remainingTime % 60 + CFLocale.FORMAT_Second);
        publicPlaceholders.put("{seconds}", String.valueOf(remainingTime));
    }

    /**
     * Stop the fishing competition.
     * This method cancels the competition timer task, unloads boss bars and action bars, clears the ranking,
     * and sets the remaining time to zero.
     */
    @Override
    public void stop(boolean triggerEvent) {
        if (!competitionTimerTask.isCancelled()) this.competitionTimerTask.cancel();
        if (this.bossBarManager != null) this.bossBarManager.unload();
        if (this.actionBarManager != null) this.actionBarManager.unload();
        this.ranking.clear();
        this.remainingTime = 0;

        if (triggerEvent) {
            CompetitionEvent competitionEvent = new CompetitionEvent(CompetitionEvent.State.STOP, this);
            Bukkit.getPluginManager().callEvent(competitionEvent);
        }
    }

    /**
     * End the fishing competition.
     * This method marks the competition as ended, cancels sub-tasks such as timers and bar management,
     * gives prizes to top participants and participation rewards, performs end actions, and clears the ranking.
     */
    @Override
    public void end(boolean triggerEvent) {
        // mark it as ended
        this.remainingTime = 0;

        // cancel some sub tasks
        if (!competitionTimerTask.isCancelled()) this.competitionTimerTask.cancel();
        if (this.bossBarManager != null) this.bossBarManager.unload();
        if (this.actionBarManager != null) this.actionBarManager.unload();

        // give prizes
        HashMap<String, Action[]> rewardsMap = config.getRewards();
        if (ranking.getSize() != 0 && rewardsMap != null) {
            Iterator<Pair<String, Double>> iterator = ranking.getIterator();
            int i = 1;
            while (iterator.hasNext()) {
                Pair<String, Double> competitionPlayer = iterator.next();
                this.publicPlaceholders.put("{" + i + "_player}", competitionPlayer.left());
                this.publicPlaceholders.put("{" + i + "_score}", String.format("%.2f", competitionPlayer.right()));
                if (i < rewardsMap.size()) {
                    Player player = Bukkit.getPlayer(competitionPlayer.left());
                    if (player != null)
                        for (Action action : rewardsMap.get(String.valueOf(i)))
                            action.trigger(new Condition(player, this.publicPlaceholders));
                } else {
                    Action[] actions = rewardsMap.get("participation");
                    if (actions != null) {
                        Player player = Bukkit.getPlayer(competitionPlayer.left()); {
                            if (player != null)
                                for (Action action : actions)
                                    action.trigger(new Condition(player, this.publicPlaceholders));
                        }
                    }
                }
                i++;
            }
        }

        // do end actions
        Action[] actions = config.getEndActions();
        if (actions != null) {
            Condition condition = new Condition(null, null, new HashMap<>(publicPlaceholders));
            for (Action action : actions) {
                action.trigger(condition);
            }
        }

        // call event
        if (triggerEvent) {
            CompetitionEvent competitionEndEvent = new CompetitionEvent(CompetitionEvent.State.END, this);
            Bukkit.getPluginManager().callEvent(competitionEndEvent);
        }

        // 1 seconds delay for other servers to read the redis data
        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(this.ranking::clear, 1, TimeUnit.SECONDS);
    }

    /**
     * Check if the fishing competition is ongoing.
     *
     * @return {@code true} if the competition is still ongoing, {@code false} if it has ended.
     */
    @Override
    public boolean isOnGoing() {
        return remainingTime > 0;
    }

    /**
     * Decreases the remaining time for the fishing competition and updates the progress.
     *
     * @return {@code true} if the remaining time becomes zero or less, indicating the competition has ended.
     */
    private boolean decreaseTime() {
        long current = Instant.now().getEpochSecond();
        int duration = config.getDurationInSeconds();
        remainingTime = duration - (current - startTime);
        progress = (float) remainingTime / duration;
        return remainingTime <= 0;
    }

    /**
     * Refreshes the data for a player in the fishing competition, including updating their score and triggering
     * actions if it's their first time joining the competition.
     *
     * @param player The player whose data needs to be refreshed.
     * @param score The player's current score in the competition.
     */
    @Override
    public void refreshData(Player player, double score) {
        // if player join for the first time, trigger join actions
        if (!hasPlayerJoined(player)) {
            Action[] actions = config.getJoinActions();
            if (actions != null) {
                Condition condition = new Condition(player);
                for (Action action : actions) {
                    action.trigger(condition);
                }
            }
        }

        // show competition info
        if (this.bossBarManager != null) this.bossBarManager.showBossBarTo(player);
        if (this.actionBarManager != null) this.actionBarManager.showActionBarTo(player);

        // refresh data
        switch (this.goal) {
            case CATCH_AMOUNT -> ranking.refreshData(player.getName(), 1);
            case TOTAL_SIZE, TOTAL_SCORE -> ranking.refreshData(player.getName(), score);
            case MAX_SIZE -> {
                if (score > ranking.getPlayerScore(player.getName())) {
                    ranking.setData(player.getName(), score);
                }
            }
        }
    }

    /**
     * Checks if a player has joined the fishing competition based on their name.
     *
     * @param player The player to check for participation.
     * @return {@code true} if the player has joined the competition; {@code false} otherwise.
     */
    @Override
    public boolean hasPlayerJoined(OfflinePlayer player) {
        return ranking.getPlayerRank(player.getName()) != -1;
    }

    /**
     * Gets the progress of the fishing competition as a float value (0~1).
     *
     * @return The progress of the fishing competition as a float.
     */
    @Override
    public float getProgress() {
        return progress;
    }

    /**
     * Gets the remaining time in seconds for the fishing competition.
     *
     * @return The remaining time in seconds.
     */
    @Override
    public long getRemainingTime() {
        return remainingTime;
    }

    /**
     * Gets the start time of the fishing competition.
     *
     * @return The start time of the fishing competition.
     */
    @Override
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the configuration of the fishing competition.
     *
     * @return The configuration of the fishing competition.
     */
    @NotNull
    @Override
    public CompetitionConfig getConfig() {
        return config;
    }

    /**
     * Gets the goal of the fishing competition.
     *
     * @return The goal of the fishing competition.
     */
    @NotNull
    @Override
    public CompetitionGoal getGoal() {
        return goal;
    }

    /**
     * Gets the ranking data for the fishing competition.
     *
     * @return The ranking data for the fishing competition.
     */
    @NotNull
    @Override
    public Ranking getRanking() {
        return ranking;
    }

    /**
     * Gets the cached placeholders for the fishing competition.
     *
     * @return A ConcurrentHashMap containing cached placeholders.
     */
    @NotNull
    @Override
    public Map<String, String> getCachedPlaceholders() {
        return publicPlaceholders;
    }

    /**
     * Gets a specific cached placeholder value by its key.
     *
     * @param papi The key of the cached placeholder.
     * @return The cached placeholder value as a string, or null if not found.
     */
    @Override
    public String getCachedPlaceholder(String papi) {
        return publicPlaceholders.get(papi);
    }
}
