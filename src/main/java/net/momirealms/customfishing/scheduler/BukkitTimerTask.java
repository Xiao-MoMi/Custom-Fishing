package net.momirealms.customfishing.scheduler;

import org.bukkit.scheduler.BukkitTask;

public class BukkitTimerTask implements TimerTask {

    private final BukkitTask bukkitTask;

    public BukkitTimerTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        this.bukkitTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }
}
