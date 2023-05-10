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

package net.momirealms.customfishing.fishing.competition;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.fishing.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.fishing.competition.ranking.LocalRankingImpl;
import net.momirealms.customfishing.fishing.competition.ranking.RankingInterface;
import net.momirealms.customfishing.fishing.competition.ranking.RedisRankingImpl;
import net.momirealms.customfishing.integration.papi.PlaceholderManager;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Competition {

    public static Competition currentCompetition;
    private final CompetitionConfig competitionConfig;
    private CompetitionGoal goal;
    private ScheduledFuture<?> timerTask;
    private RankingInterface ranking;
    private long startTime;
    private long remainingTime;
    private float progress;
    private BossBarManager bossBarManager;

    public Competition(CompetitionConfig competitionConfig) {
        this.competitionConfig = competitionConfig;
    }

    public void begin(boolean forceStart) {
        Collection<? extends Player> playerCollections = Bukkit.getOnlinePlayers();
        if (playerCollections.size() >= competitionConfig.getMinPlayers() || forceStart) {
            this.goal = competitionConfig.getGoal() == CompetitionGoal.RANDOM ? getRandomGoal() : competitionConfig.getGoal();
            this.remainingTime = this.competitionConfig.getDuration();
            this.startTime = Instant.now().getEpochSecond();

            if (ConfigManager.useRedis) this.ranking = new RedisRankingImpl();
            else this.ranking = new LocalRankingImpl();
            this.ranking.clear();

            startTimer();
            PlaceholderManager placeholderManager = CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager();
            for (String startMsg : competitionConfig.getStartMessage())
                for (Player player : playerCollections)
                    AdventureUtils.playerMessage(player, placeholderManager.parse(player, startMsg));

            for (String startCmd : competitionConfig.getStartCommand())
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), startCmd);

            if (competitionConfig.isBossBarEnabled()) {
                BossBarConfig bossBarConfig = competitionConfig.getBossBarConfig();
                if (bossBarConfig != null) {
                    this.bossBarManager = new BossBarManager(bossBarConfig);
                    this.bossBarManager.load();
                    if (bossBarConfig.isShowToAll()) {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            this.bossBarManager.tryJoin(player, false);
                        }
                    }
                }
            }
        } else {
            for (Player player : playerCollections) {
                AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.notEnoughPlayers);
            }
            currentCompetition = null;
        }
    }

    private void startTimer() {
        this.timerTask = CustomFishing.getInstance().getScheduler().runTaskTimer(() -> {
            if (decreaseTime()) {
                end();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void cancel() {
        if (this.bossBarManager != null) {
            bossBarManager.unload();
        }
        if (this.timerTask != null && !this.timerTask.isCancelled()) {
            this.timerTask.cancel(false);
        }
        this.ranking.clear();
        currentCompetition = null;
    }

    public void end() {
        if (this.bossBarManager != null) {
            bossBarManager.unload();
        }
        if (this.timerTask != null && !this.timerTask.isCancelled()) {
            this.timerTask.cancel(false);
        }

        this.givePrize();

        List<String> newMessage = new ArrayList<>();
        PlaceholderManager placeholderManager = CustomFishing.getInstance().getIntegrationManager().getPlaceholderManager();
        for (String endMsg : competitionConfig.getEndMessage()) {
            List<String> placeholders = new ArrayList<>(placeholderManager.detectBetterPlaceholders(endMsg));
            for (String placeholder : placeholders) {
                if (placeholder.endsWith("_player}")) {
                    int rank = Integer.parseInt(placeholder.substring(1, placeholder.length() - 8));
                    endMsg = endMsg.replace(placeholder, Optional.ofNullable(ranking.getPlayerAt(rank)).orElse(MessageManager.noPlayer));
                } else if (placeholder.endsWith("_score}")) {
                    int rank = Integer.parseInt(placeholder.substring(1, placeholder.length() - 7));
                    float score = ranking.getScoreAt(rank);
                    endMsg = endMsg.replace(placeholder, score == 0 ? MessageManager.noScore : String.format("%.1f", score));
                }
            }
            newMessage.add(endMsg);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String msg : newMessage) {
                AdventureUtils.playerMessage(player, placeholderManager.parse(player, msg));
            }
        }
        for (String endCmd : competitionConfig.getEndCommand()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), endCmd);
        }

        currentCompetition = null;
        CustomFishing.getInstance().getScheduler().runTaskAsyncLater(() -> ranking.clear(), 3, TimeUnit.SECONDS);
    }

    public void givePrize(){
        HashMap<String, Action[]> rewardsMap = competitionConfig.getRewards();
        if (ranking.getSize() != 0 && rewardsMap != null) {
            Iterator<String> iterator = ranking.getIterator();
            int i = 1;
            while (iterator.hasNext()) {
                if (i < rewardsMap.size()) {
                    String playerName = iterator.next();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null){
                        for (Action action : rewardsMap.get(String.valueOf(i))) {
                            action.doOn(player, null);
                        }
                    }
                    i++;
                } else {
                    Action[] actions = rewardsMap.get("participation");
                    if (actions != null) {
                        iterator.forEachRemaining(playerName -> {
                            Player player = Bukkit.getPlayer(playerName);
                            if (player != null){
                                for (Action action : actions) {
                                    action.doOn(player, null);
                                }
                            }
                        });
                    } else {
                        break;
                    }
                }
            }
        }
    }

    private boolean decreaseTime() {
        long tVac;
        long current = Instant.now().getEpochSecond();
        int duration = competitionConfig.getDuration();
        progress = (float) remainingTime / duration;
        remainingTime = duration - (current - startTime);
        if ((tVac = (current - startTime) + 1) != duration - remainingTime) {
            for (long i = duration - remainingTime; i < tVac; i++) {
                if (remainingTime <= 0) return true;
                remainingTime--;
            }
        }
        return false;
    }

    private CompetitionGoal getRandomGoal() {
        return CompetitionGoal.values()[new Random().nextInt(CompetitionGoal.values().length - 1)];
    }

    public static boolean hasCompetitionOn() {
        return currentCompetition != null;
    }

    public float getProgress() {
        return progress;
    }

    public CompetitionConfig getCompetitionConfig() {
        return competitionConfig;
    }

    public String getPlayerRank(OfflinePlayer player) {
        return Optional.ofNullable(ranking.getPlayerRank(player.getName())).orElse(MessageManager.noRank);
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public double getScore(OfflinePlayer player) {
        return Optional.ofNullable(ranking.getCompetitionPlayer(player.getName())).orElse(CompetitionPlayer.emptyPlayer).getScore();
    }

    public boolean isJoined(Player player) {
        return ranking.getCompetitionPlayer(player.getName()) != null;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }

    public void tryJoinCompetition(Player player) {
        if (bossBarManager != null) {
            bossBarManager.tryJoin(player, true);
        }
    }

    public void refreshData(Player player, float score, boolean doubleScore) {
        if (this.goal == CompetitionGoal.CATCH_AMOUNT) {
            score = 1f;
        }
        if (this.goal == CompetitionGoal.MAX_SIZE) {
            if (score > ranking.getPlayerScore(player.getName())) {
                ranking.setData(player.getName(), score);
            }
            return;
        }
        ranking.refreshData(player.getName(), doubleScore ? 2 * score : score);
    }

    public CompetitionGoal getGoal() {
        return goal;
    }

    public RankingInterface getRanking() {
        return ranking;
    }

    public static Competition getCurrentCompetition() {
        return currentCompetition;
    }

    public long getStartTime() {
        return startTime;
    }
}
