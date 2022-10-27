/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.object.fishing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.manager.LootManager;
import net.momirealms.customfishing.object.loot.Loot;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ArmorStandUtil;
import net.momirealms.customfishing.util.FakeItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

public class BobberCheckTask extends BukkitRunnable {

    private final FishHook fishHook;
    private int timer;
    private final Player player;
    private final Bonus bonus;
    private final FishingManager fishingManager;
    private boolean hooked;
    private boolean reserve;
    private int jump_timer;
    private final int lureLevel;
    private BukkitTask cache_1;
    private BukkitTask cache_2;
    private BukkitTask cache_3;
    private ArmorStand entityCache;
    private final int entityID;
    private boolean land;
    private boolean first;

    public BobberCheckTask(Player player, Bonus bonus, FishHook fishHook, FishingManager fishingManager, int lureLevel, int entityID) {
        this.fishHook = fishHook;
        this.fishingManager = fishingManager;
        this.player = player;
        this.timer = 0;
        this.bonus = bonus;
        this.reserve = true;
        this.jump_timer = 0;
        this.lureLevel = lureLevel;
        this.entityID = entityID;
        this.land = false;
        this.first = true;
    }

    @Override
    public void run() {
        timer ++;
        if (!land && entityID != 0) {
            try {
                CustomFishing.protocolManager.sendServerPacket(player, FakeItemUtil.getVelocity(entityID, fishHook.getVelocity()));
                CustomFishing.protocolManager.sendServerPacket(player, FakeItemUtil.getTpPacket(entityID, fishHook.getLocation()));
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (timer > 3600) {
            stop();
        }
        if (!fishHook.isValid()) {
            stop();
            return;
        }
        if (fishHook.getLocation().getBlock().getType() == Material.LAVA) {
            land = true;
            if (!bonus.canLavaFishing()) {
                stop();
                return;
            }
            if (first) {
                ArmorStandUtil.sendAnimationToPlayer(fishHook.getLocation(), player, ConfigManager.lava_item, ConfigManager.lava_time);
                first = false;
            }
            if (hooked) {
                jump_timer++;
                if (jump_timer < 4) {
                    return;
                }
                jump_timer = 0;
                fishHook.setVelocity(new Vector(0,0.24,0));
                return;
            }
            if (reserve) {
                if (jump_timer < 5) {
                    jump_timer++;
                    fishHook.setVelocity(new Vector(0,0.2 - jump_timer * 0.02,0));
                    return;
                }
                reserve = false;
                randomTime();
                spawnArmorStand(fishHook.getLocation());
            }
            return;
        }
        if (fishHook.isInWater()) {
            stop();
            Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.plugin, () -> {
                List<Loot> possibleLoots = fishingManager.getPossibleLootList(new FishingCondition(fishHook.getLocation(), player), false, LootManager.WATERLOOTS.values());
                fishingManager.getNextLoot(player, bonus, possibleLoots);
                if (ConfigManager.enableWaterAnimation) {
                    ArmorStandUtil.sendAnimationToPlayer(fishHook.getLocation(), player, ConfigManager.water_item, ConfigManager.water_time);
                }
            });
            return;
        }
        if (fishHook.isOnGround()) {
            stop();
        }
    }

    public void stop() {
        cancel();
        cancelTask();
        fishingManager.removePlayerFromLavaFishing(player);
        if (entityCache != null && !entityCache.isDead()) {
            entityCache.remove();
            entityCache = null;
        }
        if (entityID != 0) {
            try {
                CustomFishing.protocolManager.sendServerPacket(player, FakeItemUtil.getDestroyPacket(entityID));
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
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
        Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.plugin, () -> {
            List<Loot> possibleLoots = fishingManager.getPossibleLootList(new FishingCondition(fishHook.getLocation(), player), false, LootManager.LAVALOOTS.values());
            fishingManager.getNextLoot(player, bonus, possibleLoots);
        });
        cancelTask();
        int random = new Random().nextInt(ConfigManager.lavaMaxTime) + ConfigManager.lavaMinTime;
        random -= lureLevel * 100;
        random *= bonus.getTime();
        if (random < ConfigManager.lavaMinTime) random = ConfigManager.lavaMinTime;
        cache_1 = Bukkit.getScheduler().runTaskLater(CustomFishing.plugin, () -> {
            hooked = true;
            if (entityCache != null && !entityCache.isDead()) entityCache.remove();
            AdventureUtil.playerSound(player, Sound.Source.NEUTRAL, Key.key("minecraft:block.pointed_dripstone.drip_lava_into_cauldron"), 1, 1);
        }, random);
        cache_2 = Bukkit.getScheduler().runTaskLater(CustomFishing.plugin, () -> {
            hooked = false;
            reserve = true;
        }, random + 40);
        cache_3 = new LavaEffect(fishHook.getLocation()).runTaskTimerAsynchronously(CustomFishing.plugin,random - 60,1);
    }

    private void spawnArmorStand(Location armorLoc) {
        armorLoc.setY(armorLoc.getBlockY() + 0.2);
        if (entityCache != null && !entityCache.isDead()) entityCache.remove();
        entityCache = armorLoc.getWorld().spawn(armorLoc, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setCollidable(false);
            a.setInvulnerable(true);
            a.setVisible(false);
            a.setCustomNameVisible(false);
            a.setSmall(true);
            a.setGravity(false);
        });
        fishHook.setHookedEntity(entityCache);
    }

    public boolean isHooked() {
        return hooked;
    }
}
