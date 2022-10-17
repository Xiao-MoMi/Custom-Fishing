package net.momirealms.customfishing.competition;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.competition.bossbar.BossBarManager;
import net.momirealms.customfishing.competition.ranking.LocalRankingImpl;
import net.momirealms.customfishing.competition.ranking.RankingInterface;
import net.momirealms.customfishing.competition.ranking.RedisRankingImpl;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.MessageManager;
import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.*;

public class Competition {

    public static Competition currentCompetition;

    private final CompetitionConfig competitionConfig;
    private CompetitionGoal goal;
    private BukkitTask timerTask;
    private RankingInterface ranking;
    private long startTime;
    private long remainingTime;
    private float progress;
    private BossBarManager bossBarManager;

    public Competition(CompetitionConfig competitionConfig) {
        this.competitionConfig = competitionConfig;
    }

    public void begin(boolean forceStart) {
        this.goal = competitionConfig.getGoal();
        if (this.goal == CompetitionGoal.RANDOM) {
            this.goal = getRandomGoal();
        }
        this.remainingTime = this.competitionConfig.getDuration();
        this.startTime = Instant.now().getEpochSecond();

        Collection<? extends Player> playerCollections = Bukkit.getOnlinePlayers();
        if (playerCollections.size() >= competitionConfig.getMinPlayers() || forceStart) {

            currentCompetition = this;

            if (ConfigManager.useRedis){
                ranking = new RedisRankingImpl();
            } else {
                ranking = new LocalRankingImpl();
            }
            startTimer();
            for (String startMsg : competitionConfig.getStartMessage()) {
                for (Player player : playerCollections) {
                    AdventureUtil.playerMessage(player, startMsg);
                }
            }
            for (String startCmd : competitionConfig.getStartCommand()) {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), startCmd);
            }
            if (competitionConfig.isEnableBossBar()) {
                bossBarManager = new BossBarManager();
                bossBarManager.load();
            }
        }
        else {
            for (Player player : playerCollections) {
                AdventureUtil.playerMessage(player, MessageManager.notEnoughPlayers);
            }
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
        }.runTaskTimer(CustomFishing.plugin, 0, 20);
    }

    public void cancel() {
        if (this.bossBarManager != null) {
            bossBarManager.unload();
        }
        ranking.clear();
        this.timerTask.cancel();
        currentCompetition = null;
    }

    public void end() {
        if (this.bossBarManager != null) {
            bossBarManager.unload();
        }
        this.timerTask.cancel();
        givePrize();

        List<String> newMessage = new ArrayList<>();
        for (String endMsg : competitionConfig.getEndMessage()) {
            CompetitionPlayer[] competitionPlayers = ranking.getTop3Player();
            float first = Optional.ofNullable(competitionPlayers[0]).orElse(CompetitionPlayer.emptyPlayer).getScore();
            float second = Optional.ofNullable(competitionPlayers[1]).orElse(CompetitionPlayer.emptyPlayer).getScore();
            float third = Optional.ofNullable(competitionPlayers[2]).orElse(CompetitionPlayer.emptyPlayer).getScore();
            newMessage.add(endMsg
                    .replace("{1st}", Optional.ofNullable(Optional.ofNullable(competitionPlayers[0]).orElse(CompetitionPlayer.emptyPlayer).getPlayer()).orElse(MessageManager.noPlayer))
                    .replace("{2nd}", Optional.ofNullable(Optional.ofNullable(competitionPlayers[1]).orElse(CompetitionPlayer.emptyPlayer).getPlayer()).orElse(MessageManager.noPlayer))
                    .replace("{3rd}", Optional.ofNullable(Optional.ofNullable(competitionPlayers[2]).orElse(CompetitionPlayer.emptyPlayer).getPlayer()).orElse(MessageManager.noPlayer))
                    .replace("{1st_points}", first < 0 ? MessageManager.noScore : String.format("%.1f",(first)))
                    .replace("{2nd_points}", second < 0 ? MessageManager.noScore : String.format("%.1f",(second)))
                    .replace("{3rd_points}", third < 0 ? MessageManager.noScore : String.format("%.1f",(third))));
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            for (String msg : newMessage) {
                AdventureUtil.playerMessage(player, msg);
            }
        }

        for (String endCmd : competitionConfig.getEndCommand()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), endCmd);
        }

        currentCompetition = null;

        Bukkit.getScheduler().runTaskLaterAsynchronously(CustomFishing.plugin, ()-> {
            ranking.clear();
        }, 100);
    }

    public void givePrize(){
        HashMap<String, ActionInterface[]> rewardsMap = competitionConfig.getRewards();
        if (ranking.getSize() != 0 && rewardsMap != null) {
            Iterator<String> iterator = ranking.getIterator();
            int i = 1;
            while (iterator.hasNext()) {
                if (i < rewardsMap.size()) {
                    String playerName = iterator.next();
                    Player player = Bukkit.getPlayer(playerName);
                    if (player != null){
                        for (ActionInterface action : rewardsMap.get(String.valueOf(i))) {
                            action.doOn(player);
                        }
                    }
                    i++;
                }
                else {
                    ActionInterface[] actions = rewardsMap.get("participation");
                    if (actions != null) {
                        iterator.forEachRemaining(playerName -> {
                            Player player = Bukkit.getPlayer(playerName);
                            if (player != null){
                                for (ActionInterface action : actions) {
                                    action.doOn(player);
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

    public String getPlayerRank(Player player) {
        return Optional.ofNullable(ranking.getPlayerRank(player.getName())).orElse(MessageManager.noRank);
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public double getScore(Player player) {
        return Optional.ofNullable(ranking.getCompetitionPlayer(player.getName())).orElse(CompetitionPlayer.emptyPlayer).getScore();
    }

    public float getFirstScore() {
        return ranking.getFirstScore();
    }

    public String getFirstPlayer() {
        return ranking.getFirstPlayer();
    }

    public boolean isJoined(Player player) {
        return ranking.getCompetitionPlayer(player.getName()) != null;
    }

    public BossBarManager getBossBarManager() {
        return bossBarManager;
    }


    public void refreshData(Player player, float score, boolean doubleScore) {
        if (this.goal == CompetitionGoal.CATCH_AMOUNT) {
            score = 1f;
        }
        if (doubleScore) {
            score *= 2;
        }
        ranking.refreshData(player.getName(), score);
    }
}
