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

package net.momirealms.customfishing.competition;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.competition.bossbar.BossBarConfig;
import net.momirealms.customfishing.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.competition.ranking.Ranking;
import net.momirealms.customfishing.competition.ranking.RankingImpl;
import net.momirealms.customfishing.competition.ranking.RedisRankingImpl;
import net.momirealms.customfishing.competition.reward.Reward;
import net.momirealms.customfishing.utils.AdventureManager;
import net.momirealms.customfishing.utils.JedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.*;

public class Competition {

    private Goal goal;
    private final long duration;
    private long startTime;
    private final int minPlayers;
    private boolean status;
    private BukkitTask timerTask;
    private Ranking ranking;
    private final BossBarConfig bossBarConfig;
    private final List<String> startMessage;
    private final List<String> endMessage;
    private final HashMap<String, List<Reward>> rewardsMap;

    public static long remainingTime;
    public static float progress;
    public static CompetitionPlayer emptyPlayer = new CompetitionPlayer("XiaoMoMi",0);

    public Competition(CompetitionConfig competitionConfig) {
        this.duration = competitionConfig.getDuration();
        this.goal = competitionConfig.getGoal();
        this.minPlayers = competitionConfig.getMinPlayers();
        this.bossBarConfig = competitionConfig.getBossBarConfig();
        this.startMessage = competitionConfig.getStartMessage();
        this.endMessage = competitionConfig.getEndMessage();
        this.rewardsMap = competitionConfig.getRewards();
    }

    public void begin(boolean forceStart) {
        if (goal == Goal.RANDOM) {
            goal = getRandomGoal();
        }
        remainingTime = this.duration;
        this.startTime = Instant.now().getEpochSecond();

        Collection<? extends Player> playerCollections = Bukkit.getOnlinePlayers();
        if (playerCollections.size() >= minPlayers || forceStart) {
            status = true;
            if (JedisUtil.useRedis){
                ranking = new RedisRankingImpl();
            }else {
                ranking = new RankingImpl();
            }
            startTimer();
            if (startMessage != null){
                playerCollections.forEach(player -> {
                    startMessage.forEach(message -> {
                        AdventureManager.playerMessage(player, message);
                    });
                });
            }
        }
        else {
            playerCollections.forEach(player -> {
                AdventureManager.playerMessage(player, ConfigReader.Message.notEnoughPlayers);
            });
        }
    }

    private void startTimer() {
        this.timerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (decreaseTime()){
                    end();
                }
            }
        }.runTaskTimer(CustomFishing.instance, 0, 20);
    }

    private boolean decreaseTime() {

        long tVac;
        long current = Instant.now().getEpochSecond();

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

    public void end() {
        BossBarManager.stopAllTimer();
        this.timerTask.cancel();
        status = false;
        givePrize();
        if (endMessage != null){
            List<String> newMessage = new ArrayList<>();
            endMessage.forEach(message -> {
                CompetitionPlayer[] competitionPlayers = ranking.getTop3Player();
                float first = Optional.ofNullable(competitionPlayers[0]).orElse(CompetitionPlayer.emptyPlayer).getScore();
                float second = Optional.ofNullable(competitionPlayers[1]).orElse(CompetitionPlayer.emptyPlayer).getScore();
                float third = Optional.ofNullable(competitionPlayers[2]).orElse(CompetitionPlayer.emptyPlayer).getScore();
                newMessage.add(message
                        .replace("{1st}", Optional.ofNullable(Optional.ofNullable(competitionPlayers[0]).orElse(CompetitionPlayer.emptyPlayer).getPlayer()).orElse(ConfigReader.Message.noPlayer))
                        .replace("{2nd}", Optional.ofNullable(Optional.ofNullable(competitionPlayers[1]).orElse(CompetitionPlayer.emptyPlayer).getPlayer()).orElse(ConfigReader.Message.noPlayer))
                        .replace("{3rd}", Optional.ofNullable(Optional.ofNullable(competitionPlayers[2]).orElse(CompetitionPlayer.emptyPlayer).getPlayer()).orElse(ConfigReader.Message.noPlayer))
                        .replace("{1st_points}", first < 0 ? ConfigReader.Message.noScore : String.format("%.1f",(first)))
                        .replace("{2nd_points}", second < 0 ? ConfigReader.Message.noScore : String.format("%.1f",(second)))
                        .replace("{3rd_points}", third < 0 ? ConfigReader.Message.noScore : String.format("%.1f",(third))));
            });
            Bukkit.getOnlinePlayers().forEach(player -> {
                newMessage.forEach(message -> {
                    AdventureManager.playerMessage(player, message);
                });
            });
        }
        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomFishing.instance, ()-> {
            ranking.clear();
        }, 300);
    }

    public void givePrize(){
        if (ranking.getSize() != 0 && rewardsMap != null) {
            Iterator<String> iterator = ranking.getIterator();
            int i = 1;
            while (iterator.hasNext()) {
                if (i < rewardsMap.size()) {
                    String playerName = iterator.next();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null){
                        for (Reward reward : rewardsMap.get(String.valueOf(i))) {
                            reward.giveReward(player);
                        }
                    }
                    i++;
                }
                else {
                    List<Reward> rewards = rewardsMap.get("participation");
                    if (rewards != null) {
                        iterator.forEachRemaining(playerName -> {
                            Player player = Bukkit.getPlayer(playerName);
                            if (player != null){
                                for (Reward reward : rewards) {
                                    reward.giveReward(player);
                                }
                            }
                        });
                    }
                    else {
                        break;
                    }
                }
            }
        }
    }

    public void cancel() {
        BossBarManager.stopAllTimer();
        ranking.clear();
        this.timerTask.cancel();
        status = false;
    }

    public void refreshRanking(String player, float score) {
        if (this.goal != Goal.TOTAL_SCORE) score = 1.0f;
        if (score == 0) return;
        ranking.refreshData(player, score);
    }

    private Goal getRandomGoal() {
        int goal = new Random().nextInt(Goal.values().length-1);
        return Goal.values()[goal];
    }

    public long getDuration() {return duration;}
    public long getRemainingTime() {return remainingTime;}
    public boolean isGoingOn() {return status;}
    public BossBarConfig getBossBarConfig() {return bossBarConfig;}
    public Ranking getRanking() {return ranking;}
}
