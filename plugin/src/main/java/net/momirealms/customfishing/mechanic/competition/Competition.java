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
import net.momirealms.customfishing.setting.Config;
import net.momirealms.customfishing.setting.Locale;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
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
        if (Config.redisRanking) this.ranking = new RedisRankingImpl();
                            else this.ranking = new LocalRankingImpl();
        this.publicPlaceholders = new ConcurrentHashMap<>();
        this.publicPlaceholders.put("{goal}", CustomFishingPlugin.get().getCompetitionManager().getCompetitionLocale(goal));
    }

    @Override
    public void start() {
        this.progress = 1;
        this.remainingTime = config.getDuration();
        this.startTime = Instant.now().getEpochSecond();
        this.updatePublicPlaceholders();

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
            Condition condition = new Condition(null, null, new HashMap<>());
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    private void arrangeTimerTask() {
        this.competitionTimerTask = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(() -> {
            if (decreaseTime()) {
                end();
                return;
            }
            updatePublicPlaceholders();
        }, 1, 1, TimeUnit.SECONDS);
    }

    private void updatePublicPlaceholders() {
        for (int i = 1; i < Config.placeholderLimit + 1; i++) {
            int finalI = i;
            Optional.ofNullable(ranking.getPlayerAt(i)).ifPresentOrElse(player -> {
                publicPlaceholders.put("{" + finalI + "_player}", player);
                publicPlaceholders.put("{" + finalI + "_score}", String.format("%.2f", ranking.getScoreAt(finalI)));
            }, () -> {
                publicPlaceholders.put("{" + finalI + "_player}", Locale.MSG_No_Player);
                publicPlaceholders.put("{" + finalI + "_score}", Locale.MSG_No_Score);
            });
        }
        publicPlaceholders.put("{hour}", remainingTime < 3600 ? "" : (remainingTime / 3600) + Locale.FORMAT_Hour);
        publicPlaceholders.put("{minute}", remainingTime < 60 ? "" : (remainingTime % 3600) / 60 + Locale.FORMAT_Minute);
        publicPlaceholders.put("{second}", remainingTime == 0 ? "" : remainingTime % 60 + Locale.FORMAT_Second);
        publicPlaceholders.put("{seconds}", remainingTime + Locale.FORMAT_Second);
    }

    @Override
    public void stop() {
        if (!competitionTimerTask.isCancelled()) this.competitionTimerTask.cancel();
        if (this.bossBarManager != null) this.bossBarManager.unload();
        if (this.actionBarManager != null) this.actionBarManager.unload();
        this.ranking.clear();
        this.remainingTime = 0;
    }

    @Override
    public void end() {
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
                            action.trigger(new Condition(player));
                    i++;
                } else {
                    Action[] actions = rewardsMap.get("participation");
                    if (actions != null) {
                        iterator.forEachRemaining(playerName -> {
                            Player player = Bukkit.getPlayer(competitionPlayer.left());
                            if (player != null)
                                for (Action action : actions)
                                    action.trigger(new Condition(player));
                        });
                    } else {
                        break;
                    }
                }
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

        // 1.5 seconds delay for other servers to read the redis data
        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(this.ranking::clear, 1500, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isOnGoing() {
        return remainingTime > 0;
    }

    private boolean decreaseTime() {
        long current = Instant.now().getEpochSecond();
        int duration = config.getDuration();
        remainingTime = duration - (current - startTime);
        progress = (float) remainingTime / duration;
        return remainingTime <= 0;
    }

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

    @Override
    public boolean hasPlayerJoined(OfflinePlayer player) {
        return ranking.getPlayerRank(player.getName()) != -1;
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

    @Override
    public CompetitionConfig getConfig() {
        return config;
    }

    @Override
    public CompetitionGoal getGoal() {
        return goal;
    }

    @Override
    public Ranking getRanking() {
        return ranking;
    }

    @Override
    public ConcurrentHashMap<String, String> getCachedPlaceholders() {
        return publicPlaceholders;
    }

    @Override
    public String getCachedPlaceholder(String papi) {
        return publicPlaceholders.get(papi);
    }
}
