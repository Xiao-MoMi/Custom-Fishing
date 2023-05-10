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

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.object.Function;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;

public class Scheduler extends Function {

    private final ScheduledThreadPoolExecutor schedule;
    private final SchedulerPlatform schedulerPlatform;

    public Scheduler(CustomFishing plugin) {
        if (plugin.getVersionHelper().isFolia()) {
            this.schedulerPlatform = new FoliaSchedulerImpl(plugin);
        } else {
            this.schedulerPlatform = new BukkitSchedulerImpl(plugin);
        }
        this.schedule = new ScheduledThreadPoolExecutor(1);
        this.schedule.setMaximumPoolSize(1);
        this.schedule.setKeepAliveTime(10, TimeUnit.SECONDS);
        this.schedule.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
    }

    public void reload() {
        this.schedule.getQueue().clear();
        this.schedule.setCorePoolSize(ConfigManager.corePoolSize);
        this.schedule.setMaximumPoolSize(ConfigManager.maximumPoolSize);
        this.schedule.setKeepAliveTime(ConfigManager.keepAliveTime, TimeUnit.SECONDS);
    }

    @Override
    public void disable() {
        this.schedule.shutdown();
    }

    public ScheduledFuture<?> runTaskAsyncLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.schedule.schedule(runnable, delay, timeUnit);
    }

    public void runTaskAsync(Runnable runnable) {
        this.schedule.execute(runnable);
    }

    public void runTask(Runnable runnable) {
        this.schedulerPlatform.runTask(runnable);
    }

    public void runTask(Runnable runnable, Location location) {
        this.schedulerPlatform.runTask(runnable, location);
    }

    public ScheduledFuture<?> runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit) {
        return this.schedule.schedule(() -> runTask(runnable), delay, timeUnit);
    }

    public ScheduledFuture<?> runTaskLater(Runnable runnable, long delay, TimeUnit timeUnit, Location location) {
        return this.schedule.schedule(() -> runTask(runnable, location), delay, timeUnit);
    }

    public <T> Future<T> callSyncMethod(@NotNull Callable<T> task) {
        return this.schedulerPlatform.callSyncMethod(task);
    }

    public ScheduledFuture<?> runTaskTimerAsync(Runnable runnable, long delay, long interval, TimeUnit timeUnit) {
        return this.schedule.scheduleAtFixedRate(runnable, delay, interval, timeUnit);
    }

    public ScheduledFuture<?> runTaskTimer(Runnable runnable, long delay, long interval, TimeUnit timeUnit) {
        return this.schedule.scheduleAtFixedRate(() -> runTask(runnable), delay, interval, timeUnit);
    }

    public ScheduledFuture<?> runTaskTimer(Runnable runnable, long delay, long interval, TimeUnit timeUnit, Location location) {
        return this.schedule.scheduleAtFixedRate(() -> runTask(runnable, location), delay, interval, timeUnit);
    }
}
