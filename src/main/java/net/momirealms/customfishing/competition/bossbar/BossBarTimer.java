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

import net.momirealms.customfishing.CustomFishing;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class BossBarTimer {

    private HashMap<Integer, BossBarSender> bossbarCache = new HashMap<>();

    public BossBarTimer(Player player, BossBarConfig bossBarConfig){

        BossBarSender bossbar = new BossBarSender(player, bossBarConfig);
        bossbar.showBossbar();
        BukkitTask task = bossbar.runTaskTimerAsynchronously(CustomFishing.instance, 0,1);
        bossbarCache.put(task.getTaskId(), bossbar);

    }

    public void stopTimer(){
        bossbarCache.forEach((key,value)-> {
            value.hideBossbar();
            Bukkit.getScheduler().cancelTask(key);
        });
        bossbarCache = null;
    }
}