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
import net.momirealms.customfishing.api.event.CompetitionEvent;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionConfig;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.competition.RankingProvider;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.bukkit.competition.actionbar.ActionBarManager;
import net.momirealms.customfishing.bukkit.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.bukkit.competition.ranking.LocalRankingProvider;
import net.momirealms.customfishing.bukkit.competition.ranking.RedisRankingProvider;
import net.momirealms.customfishing.common.locale.MessageConstants;
import net.momirealms.customfishing.common.locale.TranslationManager;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Competition implements FishingCompetition {

    private final BukkitCustomFishingPlugin plugin;
    private final CompetitionConfig config;
    private SchedulerTask competitionTimerTask;
    private final CompetitionGoal goal;
    private final Context<Player> publicContext;
    private final RankingProvider rankingProvider;
    private float progress;
    private int remainingTime;
    private long startTime;
    private BossBarManager bossBarManager;
    private ActionBarManager actionBarManager;

    public Competition(BukkitCustomFishingPlugin plugin, CompetitionConfig config) {
        this.config = config;
        this.plugin = plugin;
        this.goal = config.goal() == CompetitionGoal.RANDOM ? CompetitionGoal.getRandom() : config.goal();
        if (ConfigManager.redisRanking()) this.rankingProvider = new RedisRankingProvider();
                              else this.rankingProvider = new LocalRankingProvider();
        this.publicContext = Context.player(null, true);
        this.publicContext.arg(ContextKeys.GOAL, goal);
    }

    @Override
    public void start(boolean triggerEvent) {
        this.progress = 1;
        this.remainingTime = this.config.durationInSeconds();
        this.startTime = Instant.now().getEpochSecond();

        this.arrangeTimerTask();
        if (this.config.bossBarConfig() != null && this.config.bossBarConfig().enabled()) {
            this.bossBarManager = new BossBarManager(this.config.bossBarConfig(), this);
            this.bossBarManager.load();
        }
        if (this.config.actionBarConfig() != null && this.config.actionBarConfig().enabled()) {
            this.actionBarManager = new ActionBarManager(this.config.actionBarConfig(), this);
            this.actionBarManager.load();
        }

        this.updatePublicPlaceholders();
        ActionManager.trigger(this.publicContext, this.config.startActions());
        this.rankingProvider.clear();
        if (triggerEvent) {
            this.plugin.getScheduler().async().execute(() -> {
                CompetitionEvent competitionStartEvent = new CompetitionEvent(CompetitionEvent.State.START, this);
                Bukkit.getPluginManager().callEvent(competitionStartEvent);
            });
        }
    }

    @Override
    public void stop(boolean triggerEvent) {
        if (this.competitionTimerTask != null)
            this.competitionTimerTask.cancel();
        if (this.bossBarManager != null)
            this.bossBarManager.unload();
        if (this.actionBarManager != null)
            this.actionBarManager.unload();
        this.rankingProvider.clear();
        this.remainingTime = 0;
        if (triggerEvent) {
            plugin.getScheduler().async().execute(() -> {
                CompetitionEvent competitionEvent = new CompetitionEvent(CompetitionEvent.State.STOP, this);
                Bukkit.getPluginManager().callEvent(competitionEvent);
            });
        }
    }

    @Override
    public void end(boolean triggerEvent) {
        // mark it as ended
        this.remainingTime = 0;

        // cancel some sub tasks
        if (competitionTimerTask != null)
            this.competitionTimerTask.cancel();
        if (this.bossBarManager != null)
            this.bossBarManager.unload();
        if (this.actionBarManager != null)
            this.actionBarManager.unload();

        // give prizes
        HashMap<String, Action<Player>[]> rewardsMap = config.rewards();
        if (rankingProvider.getSize() != 0 && rewardsMap != null) {
            Iterator<Pair<String, Double>> iterator = rankingProvider.getIterator();
            int i = 1;
            while (iterator.hasNext()) {
                Pair<String, Double> competitionPlayer = iterator.next();
                this.publicContext.arg(ContextKeys.of(i + "_player", String.class), competitionPlayer.left());
                this.publicContext.arg(ContextKeys.of(i + "_score", String.class), String.format("%.2f", goal.isReversed() ? -competitionPlayer.right() : competitionPlayer.right()));
                if (i < rewardsMap.size()) {
                    Player player = Bukkit.getPlayer(competitionPlayer.left());
                    if (player != null) {
                        ActionManager.trigger(Context.player(player).combine(this.publicContext), rewardsMap.get(String.valueOf(i)));
                    }
                } else {
                    Action<Player>[] actions = rewardsMap.get("participation");
                    if (actions != null) {
                        Player player = Bukkit.getPlayer(competitionPlayer.left()); {
                            if (player != null) {
                                ActionManager.trigger(Context.player(player).combine(this.publicContext), actions);
                            }
                        }
                    }
                }
                i++;
            }
        }

        // end actions
        ActionManager.trigger(publicContext, config.endActions());

        // call event
        if (triggerEvent) {
            plugin.getScheduler().async().execute(() -> {
                CompetitionEvent competitionEndEvent = new CompetitionEvent(CompetitionEvent.State.END, this);
                Bukkit.getPluginManager().callEvent(competitionEndEvent);
            });
        }

        // 1 seconds delay for other servers to read the redis data
        plugin.getScheduler().asyncLater(this.rankingProvider::clear, 1, TimeUnit.SECONDS);
    }

    private void arrangeTimerTask() {
        this.competitionTimerTask = this.plugin.getScheduler().asyncRepeating(() -> {
            if (decreaseTime()) {
                end(true);
                return;
            }
            updatePublicPlaceholders();
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void updatePublicPlaceholders() {
        for (int i = 1; i < ConfigManager.placeholderLimit() + 1; i++) {
            Optional<String> player = Optional.ofNullable(this.rankingProvider.getPlayerAt(i));
            if (player.isPresent()) {
                this.publicContext.arg(ContextKeys.of(i + "_player", String.class), player.get());
                this.publicContext.arg(ContextKeys.of(i + "_score", String.class), String.format("%.2f", getScore(i)));
            } else {
                this.publicContext.arg(ContextKeys.of(i + "_player", String.class), TranslationManager.miniMessageTranslation(MessageConstants.COMPETITION_NO_PLAYER.build().key()));
                this.publicContext.arg(ContextKeys.of(i + "_score", String.class), TranslationManager.miniMessageTranslation(MessageConstants.COMPETITION_NO_SCORE.build().key()));
            }
        }
        this.publicContext.arg(ContextKeys.HOUR, remainingTime < 3600 ? "" : (remainingTime / 3600) + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_HOUR.build().key()));
        this.publicContext.arg(ContextKeys.MINUTE, remainingTime < 60 ? "" : (remainingTime % 3600) / 60 + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_MINUTE.build().key()));
        this.publicContext.arg(ContextKeys.SECOND, remainingTime == 0 ? "" : remainingTime % 60 + TranslationManager.miniMessageTranslation(MessageConstants.FORMAT_SECOND.build().key()));
        this.publicContext.arg(ContextKeys.SECONDS, remainingTime);
    }

    @ApiStatus.Internal
    public double getScore(String player) {
        double score = this.rankingProvider.getPlayerScore(player);
        return goal.isReversed() ? -score : score;
    }

    @ApiStatus.Internal
    public double getScore(int rank) {
        return goal.isReversed() ? -this.rankingProvider.getScoreAt(rank) : this.rankingProvider.getScoreAt(rank);
    }

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
        int duration = config.durationInSeconds();
        remainingTime = (int) (duration - (current - startTime));
        progress = (float) remainingTime / duration;
        return remainingTime <= 0;
    }

    @Override
    public void refreshData(Player player, double score) {
        refreshScore(player, score);
    }

    @Override
    public void refreshScore(Player player, double score) {
        // if player join for the first time, trigger join actions
        if (!hasPlayerJoined(player)) {
            ActionManager.trigger(Context.player(player).combine(publicContext), config.joinActions());
        }

        // show competition info
        if (this.bossBarManager != null)
            this.bossBarManager.showBossBarTo(player);
        if (this.actionBarManager != null)
            this.actionBarManager.showActionBarTo(player);

        // refresh data
        this.goal.refreshScore(rankingProvider, player, score);
    }

    @Override
    public boolean hasPlayerJoined(OfflinePlayer player) {
        return rankingProvider.getPlayerRank(player.getName()) != -1;
    }

    @Override
    public float getProgress() {
        return progress;
    }

    @Override
    public long getRemainingTime() {
        return remainingTime;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @NotNull
    @Override
    public CompetitionConfig getConfig() {
        return config;
    }

    @NotNull
    @Override
    public CompetitionGoal getGoal() {
        return goal;
    }

    @NotNull
    @Override
    public RankingProvider getRanking() {
        return rankingProvider;
    }

    @Override
    public Context<Player> getPublicContext() {
        return publicContext;
    }
}
