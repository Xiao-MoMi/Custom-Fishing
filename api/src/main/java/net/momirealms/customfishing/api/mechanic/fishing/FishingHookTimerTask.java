package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.entity.FishHook;

import java.util.function.Consumer;

public class FishingHookTimerTask {

    private static Consumer<FishHook> hookConsumer = defaultHookLogics();
    private FishingManager manager;
    private FishHook hook;
    private final SchedulerTask task;

    public static Consumer<FishHook> defaultHookLogics() {
        return fishHook -> {
            if (fishHook.isValid()) {
                BukkitCustomFishingPlugin.getInstance().getFishingManager().destroy(fishHook.getOwnerUniqueId());
                return;
            }

        };
    }

    public FishingHookTimerTask(FishingManager manager, FishHook hook) {
        this.manager = manager;
        this.hook = hook;
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runRepeating(() -> {
            hookConsumer.accept(hook);
        }, 1, 1, hook.getLocation());
    }

    public void cancel() {
        task.cancel();
    }
}
