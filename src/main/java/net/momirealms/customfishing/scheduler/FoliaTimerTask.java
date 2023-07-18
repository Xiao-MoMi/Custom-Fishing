package net.momirealms.customfishing.scheduler;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class FoliaTimerTask implements TimerTask {

    private final ScheduledTask timerTask;

    public FoliaTimerTask(ScheduledTask timerTask) {
        this.timerTask = timerTask;
    }

    @Override
    public void cancel() {
        this.timerTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return timerTask.isCancelled();
    }
}
