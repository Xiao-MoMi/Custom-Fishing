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
import net.momirealms.customfishing.api.scheduler.Scheduler;
import net.momirealms.customfishing.setting.Config;
import org.bukkit.Location;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SchedulerImpl implements Scheduler {

    private final SyncScheduler syncScheduler;
    private final ScheduledThreadPoolExecutor schedule;
    private final CustomFishingPlugin plugin;

    public SchedulerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.syncScheduler = plugin.getVersionManager().isFolia() ?
                new FoliaSchedulerImpl(plugin) : new BukkitSchedulerImpl(plugin);
        this.schedule = new ScheduledThreadPoolExecutor(4);
        this.schedule.setMaximumPoolSize(4);
        this.schedule.setKeepAliveTime(10, TimeUnit.SECONDS);
        this.schedule.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void reload() {
        this.schedule.setCorePoolSize(Config.corePoolSize);
        this.schedule.setKeepAliveTime(Config.keepAliveTime, TimeUnit.SECONDS);
        this.schedule.setMaximumPoolSize(Config.maximumPoolSize);
    }

    public void shutdown() {
        if (this.schedule != null && !this.schedule.isShutdown())
            this.schedule.shutdown();
    }

    @Override
    public void runTaskSync(Runnable runnable, Location location) {
        this.syncScheduler.runSyncTask(runnable, location);
    }

    @Override
    public void runTaskAsync(Runnable runnable) {
        this.schedule.execute(runnable);
    }

    @Override
    public CancellableTask runTaskSyncTimer(Runnable runnable, Location location, long delay, long period) {
        return this.syncScheduler.runTaskSyncTimer(runnable, location, delay, period);
    }

    @Override
    public CancellableTask runTaskAsyncLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        return new ScheduledTask(schedule.schedule(runnable, delay, timeUnit));
    }

    @Override
    public CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay, TimeUnit timeUnit) {
        return new ScheduledTask(schedule.schedule(() -> {
            runTaskSync(runnable, location);
        }, delay, timeUnit));
    }

    @Override
    public CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay) {
        return this.syncScheduler.runTaskSyncLater(runnable, location, delay);
    }

    @Override
    public CancellableTask runTaskAsyncTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return new ScheduledTask(schedule.scheduleAtFixedRate(runnable, delay, period, timeUnit));
    }

    public static class ScheduledTask implements CancellableTask {

        private final ScheduledFuture<?> scheduledFuture;

        public ScheduledTask(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public void cancel() {
            this.scheduledFuture.cancel(false);
        }

        @Override
        public boolean isCancelled() {
            return this.scheduledFuture.isCancelled();
        }
    }
}
