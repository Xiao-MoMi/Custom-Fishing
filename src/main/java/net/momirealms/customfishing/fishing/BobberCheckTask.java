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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BobberCheckTask implements Runnable {

    private final CustomFishing plugin;

    private final ScheduledFuture<?> bobberTask;
    private ScheduledFuture<?> getHookedTask;
    private ScheduledFuture<?> resetTask;
    private final FishHook fishHook;
    private final Player player;
    private final Effect effect;
    private final String rod;
    private final String bait;
    private final FishingManager fishingManager;
    private boolean hooked;
    private boolean reserve;
    private boolean land;
    private boolean first;
    private int timer;
    private int jump_timer;
    private final int entityID;
    private final int lureLevel;
    private LavaEffect lavaEffect;
    private ArmorStand hookedEntity;

    public BobberCheckTask(CustomFishing plugin, Player player, Effect effect, FishHook fishHook, FishingManager fishingManager, int lureLevel, int entityID, String rod, String bait) {
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
        this.rod = rod;
        this.bait = bait;
        this.bobberTask = plugin.getScheduler().runTaskTimer(this, 50, 50, TimeUnit.MILLISECONDS);
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
                if (plugin.getVersionHelper().isFolia()) {
                    plugin.getScheduler().runTask(() -> spawnArmorStand(fishHook.getLocation()), fishHook.getLocation());
                } else {
                    spawnArmorStand(fishHook.getLocation());
                }
            }
            return;
        }
        if (fishHook.isInWater()) {
            stop();
            plugin.getScheduler().runTaskAsync(() -> {
                List<Loot> possibleLoots = new ArrayList<>();
                if (!(ConfigManager.needRodForLoot && !effect.hasSpecialRod())) {
                    possibleLoots = fishingManager.getPossibleLootList(new FishingCondition(fishHook.getLocation(), player, rod, bait), false, plugin.getLootManager().getWaterLoots().values());
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
        bobberTask.cancel(false);
        cancelSubTask();
        fishingManager.removePlayerFromLavaFishing(player);
        if (hookedEntity != null && !hookedEntity.isDead()) {
            if (plugin.getVersionHelper().isFolia()) {
                plugin.getScheduler().runTask(() -> {
                    hookedEntity.remove(); hookedEntity = null;
                }, hookedEntity.getLocation());
            } else {
                hookedEntity.remove();
                hookedEntity = null;
            }
        }
        sendRemovePacket();
    }

    private void sendRemovePacket() {
        if (entityID == 0) return;
        CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtils.getDestroyPacket(entityID));
    }

    public void cancelSubTask() {
        if (getHookedTask != null && !lavaEffect.isCancelled()) {
            getHookedTask.cancel(false);
            getHookedTask = null;
        }
        if (resetTask != null && !lavaEffect.isCancelled()) {
            resetTask.cancel(false);
            resetTask = null;
        }
        if (lavaEffect != null && !lavaEffect.isCancelled()) {
            lavaEffect.cancel();
            lavaEffect = null;
        }
    }

    private void randomTime() {
        plugin.getScheduler().runTaskAsync(() -> {
            List<Loot> possibleLoots = new ArrayList<>();
            if (!(ConfigManager.needRodForLoot && !effect.hasSpecialRod())) {
                possibleLoots = fishingManager.getPossibleLootList(new FishingCondition(fishHook.getLocation(), player, rod, bait), false, plugin.getLootManager().getLavaLoots().values());
            }
            fishingManager.getNextLoot(player, effect, possibleLoots);
        });
        cancelSubTask();
        int random = new Random().nextInt(ConfigManager.lavaMaxTime) + ConfigManager.lavaMinTime;
        random -= lureLevel * 100;
        random *= effect.getTimeModifier();
        if (random < ConfigManager.lavaMinTime) random = ConfigManager.lavaMinTime;
        getHookedTask = plugin.getScheduler().runTaskLater(() -> {
            hooked = true;
            if (hookedEntity != null && !hookedEntity.isDead()) {
                if (plugin.getVersionHelper().isFolia()) {
                    plugin.getScheduler().runTask(() -> hookedEntity.remove(), hookedEntity.getLocation());
                } else {
                    hookedEntity.remove();
                }
            }
            AdventureUtils.playerSound(player, Sound.Source.NEUTRAL, Key.key("minecraft:block.pointed_dripstone.drip_lava_into_cauldron"), 1, 1);
            if (ConfigManager.instantBar) fishingManager.showBar(player);
        }, random * 50L, TimeUnit.MILLISECONDS);
        resetTask = plugin.getScheduler().runTaskAsyncLater(() -> {
            hooked = false;
            reserve = true;
        }, (random + 40) * 50L, TimeUnit.MILLISECONDS);
        lavaEffect = new LavaEffect(fishHook.getLocation(), random - 60);
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
