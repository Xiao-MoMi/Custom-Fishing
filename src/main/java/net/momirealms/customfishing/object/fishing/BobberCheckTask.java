package net.momirealms.customfishing.object.fishing;

import com.plotsquared.core.plot.PlotId;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.object.loot.Loot;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Random;

public class BobberCheckTask extends BukkitRunnable {

    private final FishHook fishHook;
    private int timer;
    private final Player player;
    private final Bonus bonus;
    private final FishingManager fishingManager;
    private boolean hooked;
    private boolean first_time;
    private int jump_timer;
    private final int lureLevel;
    private BukkitTask cache_1;
    private BukkitTask cache_2;
    private BukkitTask cache_3;

    public BobberCheckTask(Player player, Bonus bonus, FishHook fishHook, FishingManager fishingManager, int lureLevel) {
        this.fishHook = fishHook;
        this.fishingManager = fishingManager;
        this.player = player;
        this.timer = 0;
        this.bonus = bonus;
        this.first_time = true;
        this.jump_timer = 0;
        this.lureLevel = lureLevel;
    }

    @Override
    public void run() {
        timer ++;
        if (!fishHook.isValid()) {
            stop();
            return;
        }
        if (fishHook.getLocation().getBlock().getType() == Material.LAVA) {
            if (!bonus.canLavaFishing()) {
                stop();
                return;
            }
            if (hooked) {
                jump_timer++;
                if (jump_timer < 5) {
                    return;
                }
                jump_timer = 0;
                fishHook.setVelocity(new Vector(0,0.24,0));
                return;
            }
            if (first_time) {
                first_time = false;
                randomTime();
            }
            fishHook.setVelocity(new Vector(0, 0.12,0));
            return;
        }
        if (fishHook.isInWater()) {
            List<Loot> possibleLoots = fishingManager.getPossibleWaterLootList(new FishingCondition(fishHook.getLocation(), player), false);
            fishingManager.getNextLoot(player, bonus, possibleLoots);
            stop();
            return;
        }
        if (fishHook.isOnGround()) {
            stop();
            return;
        }
        if (timer > 2400) {
            stop();
        }
    }

    public void stop() {
        cancel();
        cancelTask();
    }

    public void cancelTask() {
        if (cache_1 != null) {
            cache_1.cancel();
            cache_1 = null;
        }
        if (cache_2 != null) {
            cache_2.cancel();
            cache_2 = null;
        }
        if (cache_3 != null) {
            cache_3.cancel();
            cache_3 = null;
        }
    }

    private void randomTime() {
        List<Loot> possibleLoots = fishingManager.getPossibleLavaLootList(new FishingCondition(fishHook.getLocation(), player), false);
        fishingManager.getNextLoot(player, bonus, possibleLoots);
        cancelTask();
        int random = new Random().nextInt(ConfigManager.lavaMaxTime) + ConfigManager.lavaMinTime;
        random -= lureLevel * 100;
        random *= bonus.getTime();
        if (random < ConfigManager.lavaMinTime) random = ConfigManager.lavaMinTime;
        cache_1 = Bukkit.getScheduler().runTaskLater(CustomFishing.plugin, () -> {
            hooked = true;
            fishingManager.addPlayerToLavaFishing(player, this);
        }, random);
        cache_2 = Bukkit.getScheduler().runTaskLater(CustomFishing.plugin, () -> {
            hooked = false;
            first_time = true;
            fishingManager.removePlayerFromLavaFishing(player);
        }, random + 40);
        cache_3 = new LavaEffect(fishHook.getLocation()).runTaskTimerAsynchronously(CustomFishing.plugin,random - 60,1);
    }
}
