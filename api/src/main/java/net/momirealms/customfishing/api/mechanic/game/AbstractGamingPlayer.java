package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.manager.FishingManager;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.TimeUnit;

public abstract class AbstractGamingPlayer implements GamingPlayer, Runnable {

    protected boolean succeeded;
    protected CancellableTask task;
    protected Player player;
    protected GameSettings settings;
    protected FishingManager manager;
    private final long deadline;

    public AbstractGamingPlayer(Player player, GameSettings settings, FishingManager manager) {
        this.player = player;
        this.settings = settings;
        this.manager = manager;
        this.deadline = System.currentTimeMillis() + settings.getTime() * 1000L;
        this.arrangeTask();
    }

    public void arrangeTask() {
        this.task = CustomFishingPlugin.get().getScheduler().runTaskAsyncTimer(this, 50, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cancel() {
        if (task != null && !task.isCancelled())
            task.cancel();
    }

    @Override
    public boolean isSucceeded() {
        return succeeded;
    }

    @Override
    public boolean onRightClick() {
        manager.processGameResult(this);
        return true;
    }

    @Override
    public boolean onLeftClick() {
        return false;
    }

    @Override
    public boolean onSwapHand() {
        return false;
    }

    @Override
    public boolean onJump() {
        return false;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Effect getEffectReward() {
        return null;
    }

    @Override
    public void run() {
        timeOutCheck();
        switchItemCheck();
    }

    protected void timeOutCheck() {
        if (System.currentTimeMillis() > deadline) {
            cancel();
            if (manager.removeHook(player.getUniqueId())) {
                manager.processGameResult(this);
            }
        }
    }

    protected void switchItemCheck() {
        PlayerInventory playerInventory = player.getInventory();
        if (playerInventory.getItemInMainHand().getType() != Material.FISHING_ROD
                && playerInventory.getItemInOffHand().getType() != Material.FISHING_ROD) {
            cancel();
            manager.processGameResult(this);
            player.removePotionEffect(PotionEffectType.SLOW);
        }
    }
}
