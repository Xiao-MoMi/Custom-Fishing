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

package net.momirealms.customfishing.scheduler;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

public class BukkitSchedulerImpl implements SyncScheduler {

    private final CustomFishingPlugin plugin;

    public BukkitSchedulerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void runSyncTask(Runnable runnable, Location location) {
        if (Bukkit.isPrimaryThread())
            runnable.run();
        else
            Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @Override
    public CancellableTask runTaskSyncTimer(Runnable runnable, Location location, long delay, long period) {
        return new BukkitCancellableTask(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, period));
    }

    @Override
    public CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay) {
        return new BukkitCancellableTask(Bukkit.getScheduler().runTaskLater(plugin, runnable, delay));
    }

    public static class BukkitCancellableTask implements CancellableTask {

        private final BukkitTask bukkitTask;

        public BukkitCancellableTask(BukkitTask bukkitTask) {
            this.bukkitTask = bukkitTask;
        }

        @Override
        public void cancel() {
            this.bukkitTask.cancel();
        }

        @Override
        public boolean isCancelled() {
            return this.bukkitTask.isCancelled();
        }
    }
}
