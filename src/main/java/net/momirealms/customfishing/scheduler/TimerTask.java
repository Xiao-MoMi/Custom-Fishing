package net.momirealms.customfishing.scheduler;

public interface TimerTask {

    void cancel();

    boolean isCancelled();
}
