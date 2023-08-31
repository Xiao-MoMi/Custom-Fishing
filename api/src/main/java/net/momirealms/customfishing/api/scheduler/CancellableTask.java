package net.momirealms.customfishing.api.scheduler;

public interface CancellableTask {

    void cancel();

    boolean isCancelled();
}
