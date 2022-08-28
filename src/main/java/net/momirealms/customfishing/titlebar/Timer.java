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

package net.momirealms.customfishing.titlebar;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.object.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Timer {

    private final TimerTask timerTask;
    private final BukkitTask task;
    private final String layout;

    public Timer(Player player, Difficulty difficulty, String layout) {
        this.layout = layout;
        this.timerTask = new TimerTask(player, difficulty, layout);
        this.task = timerTask.runTaskTimerAsynchronously(CustomFishing.instance, 0,1);
        timerTask.setTaskID(task.getTaskId());
    }

    public TimerTask getTimerTask(){ return this.timerTask; }
    public int getTaskID (){ return this.task.getTaskId(); }
    public String getLayout(){return this.layout;}
}