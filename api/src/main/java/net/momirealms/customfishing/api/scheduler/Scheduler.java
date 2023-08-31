package net.momirealms.customfishing.api.scheduler;

import org.bukkit.Location;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

    void runTaskSync(Runnable runnable, Location location);

    CancellableTask runTaskSyncTimer(Runnable runnable, Location location, long delay, long period);

    CancellableTask runTaskAsyncLater(Runnable runnable, long delay, TimeUnit timeUnit);

    void runTaskAsync(Runnable runnable);

    CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay, TimeUnit timeUnit);

    CancellableTask runTaskSyncLater(Runnable runnable, Location location, long delay);

    CancellableTask runTaskAsyncTimer(Runnable runnable, long delay, long period, TimeUnit timeUnit);
}
