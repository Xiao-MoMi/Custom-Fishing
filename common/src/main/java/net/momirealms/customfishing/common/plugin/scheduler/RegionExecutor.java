package net.momirealms.customfishing.common.plugin.scheduler;

public interface RegionExecutor<T> {

    void execute(Runnable r, T l);
}
