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

package net.momirealms.customfishing.competition.bossbar;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.competition.CompetitionSchedule;
import net.momirealms.customfishing.utils.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;

public class BossBarManager implements Listener {

    public static HashMap<Player, BossBarTimer> cache = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if (CompetitionSchedule.competition != null && CompetitionSchedule.competition.isGoingOn()){
            if (CompetitionSchedule.competition.getRanking().getCompetitionPlayer(player.getName()) != null && cache.get(player) == null){
                BossBarTimer timerTask = new BossBarTimer(player, CompetitionSchedule.competition.getBossBarConfig());
                cache.put(player, timerTask);
            }else {
                AdventureUtil.playerMessage(player, ConfigReader.Message.competitionOn);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        BossBarTimer timerTask = cache.get(player);
        if (timerTask != null){
            timerTask.stopTimer();
            cache.remove(player);
        }
    }

    public static void stopAllTimer(){
        cache.forEach(((player, timerTask) -> {
            timerTask.stopTimer();
        }));
        cache.clear();
    }

    public static void joinCompetition(Player player){
        if (cache.get(player) == null) {
            BossBarTimer timerTask = new BossBarTimer(player, CompetitionSchedule.competition.getBossBarConfig());
            cache.put(player, timerTask);
            if (CompetitionSchedule.competition.getJoinCommand() != null){
                CompetitionSchedule.competition.getJoinCommand().forEach(command -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command.replace("{player}", player.getName()));
                });
            }
        }
    }
}