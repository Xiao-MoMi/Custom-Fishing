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

package net.momirealms.customfishing.fishing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.FishingManager;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ArmorStandUtils;
import net.momirealms.customfishing.util.FakeItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BobberCheckTask extends BukkitRunnable {

    private final FishHook fishHook;
    private final CustomFishing plugin;
    private int timer;
    private final Player player;
    private final Effect effect;
    private final FishingManager fishingManager;
    private boolean hooked;
    private boolean reserve;
    private int jump_timer;
    private final int lureLevel;
    private BukkitTask task_1;
    private BukkitTask task_2;
    private BukkitTask task_3;
    private ArmorStand hookedEntity;
    private final int entityID;
    private boolean land;
    private boolean first;

    public BobberCheckTask(CustomFishing plugin, Player player, Effect effect, FishHook fishHook, FishingManager fishingManager, int lureLevel, int entityID) {
        this.fishHook = fishHook;
        this.plugin = plugin;
        this.fishingManager = fishingManager;
        this.player = player;
        this.timer = 0;
        this.effect = effect;
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
            CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtils.getVelocity(entityID, fishHook.getVelocity()));
            CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtils.getTpPacket(entityID, fishHook.getLocation()));
        }
        if (timer > 3600) {
            stop();
            return;
        }
        if (fishHook.getHookedEntity() != null && fishHook.getHookedEntity().getType() != EntityType.ARMOR_STAND) {
            stop();
            return;
        }
        if (!fishHook.isValid()) {
            stop();
            return;
        }
        if (fishHook.getLocation().getBlock().getType() == Material.LAVA) {
            land = true;
            if (!effect.canLavaFishing()) {
                stop();
                return;
            }
            if (first) {
                sendRemovePacket();
                ArmorStandUtils.sendAnimationToPlayer(fishHook.getLocation(), player, ConfigManager.lava_item, ConfigManager.lava_time);
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
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                List<Loot> possibleLoots = new ArrayList<>();
                if (!(ConfigManager.needRodForLoot && !effect.hasSpecialRod())) {
                    possibleLoots = fishingManager.getPossibleLootList(new FishingCondition(fishHook.getLocation(), player), false, plugin.getLootManager().getWaterLoots().values());
                }
                fishingManager.getNextLoot(player, effect, possibleLoots);
                if (ConfigManager.enableWaterAnimation) {
                    ArmorStandUtils.sendAnimationToPlayer(fishHook.getLocation(), player, ConfigManager.water_item, ConfigManager.water_time);
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
        if (hookedEntity != null && !hookedEntity.isDead()) {
            hookedEntity.remove();
            hookedEntity = null;
        }
        sendRemovePacket();
    }

    private void sendRemovePacket() {
        if (entityID == 0) return;
        CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtils.getDestroyPacket(entityID));
    }

    public void cancelTask() {
        if (task_1 != null) {
            task_1.cancel();
            task_1 = null;
        }
        if (task_2 != null) {
            task_2.cancel();
            task_2 = null;
        }
        if (task_3 != null) {
            task_3.cancel();
            task_3 = null;
        }
    }

    private void randomTime() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            List<Loot> possibleLoots = new ArrayList<>();
            if (!(ConfigManager.needRodForLoot && !effect.hasSpecialRod())) {
                possibleLoots = fishingManager.getPossibleLootList(new FishingCondition(fishHook.getLocation(), player), false, plugin.getLootManager().getLavaLoots().values());
            }
            fishingManager.getNextLoot(player, effect, possibleLoots);
        });
        cancelTask();
        int random = new Random().nextInt(ConfigManager.lavaMaxTime) + ConfigManager.lavaMinTime;
        random -= lureLevel * 100;
        random *= effect.getTimeModifier();
        if (random < ConfigManager.lavaMinTime) random = ConfigManager.lavaMinTime;
        task_1 = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hooked = true;
            if (hookedEntity != null && !hookedEntity.isDead()) hookedEntity.remove();
            AdventureUtils.playerSound(player, Sound.Source.NEUTRAL, Key.key("minecraft:block.pointed_dripstone.drip_lava_into_cauldron"), 1, 1);
            if (ConfigManager.instantBar) fishingManager.showBar(player);
        }, random);
        task_2 = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            hooked = false;
            reserve = true;
        }, random + 40);
        task_3 = new LavaEffect(fishHook.getLocation()).runTaskTimerAsynchronously(plugin,random - 60,1);
    }

    private void spawnArmorStand(Location armorLoc) {
        armorLoc.setY(armorLoc.getBlockY() + 0.2);
        if (hookedEntity != null && !hookedEntity.isDead()) hookedEntity.remove();
        hookedEntity = armorLoc.getWorld().spawn(armorLoc, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setCollidable(false);
            a.setInvulnerable(true);
            a.setVisible(false);
            a.setCustomNameVisible(false);
            a.setSmall(true);
            a.setGravity(false);
        });
        fishHook.setHookedEntity(hookedEntity);
    }

    public boolean isHooked() {
        return hooked;
    }
}
