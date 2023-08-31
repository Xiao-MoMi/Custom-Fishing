package net.momirealms.customfishing.mechanic.fishing;

import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.util.FakeItemUtils;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BaitAnimationTask implements Runnable {

    private final CancellableTask cancellableTask;
    private final int entityID;
    private final Player player;
    private final FishHook fishHook;

    public BaitAnimationTask(CustomFishingPlugin plugin, Player player, FishHook fishHook, ItemStack baitItem) {
        this.player = player;
        this.fishHook = fishHook;
        entityID = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, FakeItemUtils.getSpawnPacket(entityID, fishHook.getLocation()));
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, FakeItemUtils.getMetaPacket(entityID, baitItem));
        this.cancellableTask = plugin.getScheduler().runTaskAsyncTimer(this, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        if (    fishHook == null
                || fishHook.isOnGround()
                || fishHook.isInLava()
                || fishHook.isInWater()
                || !fishHook.isValid()
        ) {
            cancelAnimation();
        } else {
            CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, FakeItemUtils.getVelocity(entityID, fishHook.getVelocity()));
            CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, FakeItemUtils.getTpPacket(entityID, fishHook.getLocation()));
        }
    }

    private void cancelAnimation() {
        cancellableTask.cancel();
        CustomFishingPluginImpl.getProtocolManager().sendServerPacket(player, FakeItemUtils.getDestroyPacket(entityID));
    }
}
