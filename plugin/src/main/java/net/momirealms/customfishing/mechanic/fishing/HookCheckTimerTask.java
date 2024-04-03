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

package net.momirealms.customfishing.mechanic.fishing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishHookLandEvent;
import net.momirealms.customfishing.api.event.LavaFishingEvent;
import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.setting.CFConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A task responsible for checking the state of a fishing hook and handling lava fishing mechanics.
 */
public class HookCheckTimerTask implements Runnable {

    private final FishingManagerImpl manager;
    private final CancellableTask hookMovementTask;
    private LavaEffectTask lavaFishingTask;
    private final FishHook fishHook;
    private final FishingPreparation fishingPreparation;
    private final FishingEffect initialEffect;
    private Effect tempEffect;
    private final int lureLevel;
    private boolean firstTime;
    private boolean fishHooked;
    private boolean reserve;
    private int jumpTimer;
    private Entity hookedEntity;
    private Loot loot;
    private boolean inWater;

    /**
     * Constructs a new HookCheckTimerTask.
     *
     * @param manager             The FishingManagerImpl instance.
     * @param fishHook            The FishHook entity being checked.
     * @param fishingPreparation  The FishingPreparation instance.
     * @param initialEffect       The initial fishing effect.
     */
    public HookCheckTimerTask(
            FishingManagerImpl manager,
            FishHook fishHook,
            FishingPreparation fishingPreparation,
            FishingEffect initialEffect
    ) {
        this.inWater = false;
        this.manager = manager;
        this.fishHook = fishHook;
        this.initialEffect = initialEffect;
        this.fishingPreparation = fishingPreparation;
        this.hookMovementTask = CustomFishingPlugin.get().getScheduler().runTaskSyncTimer(this, fishHook.getLocation(), 1, 1);
        this.lureLevel = fishingPreparation.getRodItemStack().getEnchantmentLevel(Enchantment.LURE);
        this.firstTime = true;
        this.tempEffect = new FishingEffect();
    }

    @Override
    public void run() {
        if (
            !this.fishHook.isValid()
            //|| (fishHook.getHookedEntity() != null && fishHook.getHookedEntity().getType() != EntityType.ARMOR_STAND)
        ) {
            // This task would be cancelled when hook is removed
            this.destroy();
            return;
        }
        if (this.fishHook.isOnGround()) {
            this.inWater = false;
            return;
        }
        if (this.fishHook.getLocation().getBlock().getType() == Material.LAVA) {
            this.inWater = false;
            // if player can fish in lava
            if (firstTime) {
                this.firstTime = false;

                this.fishingPreparation.setLocation(this.fishHook.getLocation());
                this.fishingPreparation.mergeEffect(this.initialEffect);
                if (!initialEffect.canLavaFishing()) {
                    this.destroy();
                    return;
                }

                FishHookLandEvent event = new FishHookLandEvent(this.fishingPreparation.getPlayer(), FishHookLandEvent.Target.LAVA, this.fishHook, true, this.initialEffect);
                Bukkit.getPluginManager().callEvent(event);

                this.fishingPreparation.insertArg("{lava}", "true");
                this.fishingPreparation.triggerActions(ActionTrigger.LAND);
            }

            // simulate fishing mechanic
            if (this.fishHooked) {
                this.jumpTimer++;
                if (this.jumpTimer < 4)
                    return;
                this.jumpTimer = 0;
                this.fishHook.setVelocity(new Vector(0,0.24,0));
                return;
            }

            if (!this.reserve) {
                // jump
                if (this.jumpTimer < 5) {
                    this.jumpTimer++;
                    this.fishHook.setVelocity(new Vector(0,0.2 - this.jumpTimer * 0.02,0));
                    return;
                }

                this.reserve = true;

                this.setNextLoot();
                if (this.loot != null) {
                    this.tempEffect = this.loot.getBaseEffect().build(fishingPreparation.getPlayer(), fishingPreparation.getArgs());
                    this.tempEffect.merge(this.initialEffect);
                    this.setTempState();
                    this.startLavaFishingMechanic();
                } else {
                    this.tempEffect = new FishingEffect();
                    this.tempEffect.merge(this.initialEffect);
                    this.manager.removeTempFishingState(fishingPreparation.getPlayer());
                    CustomFishingPlugin.get().debug("No loot available for " + fishingPreparation.getPlayer().getName() + " at " + fishingPreparation.getLocation());
                }

                this.makeHookStatic(this.fishHook.getLocation());
            }
            return;
        }
        if (!this.inWater && this.fishHook.isInWater()) {
            this.inWater = true;

            this.fishingPreparation.setLocation(this.fishHook.getLocation());
            this.fishingPreparation.insertArg("{lava}", "false");
            this.fishingPreparation.insertArg("{open-water}", String.valueOf(this.fishHook.isInOpenWater()));

            if (this.firstTime) {
                this.firstTime = false;
                this.fishingPreparation.mergeEffect(this.initialEffect);

                FishHookLandEvent event = new FishHookLandEvent(this.fishingPreparation.getPlayer(), FishHookLandEvent.Target.WATER, this.fishHook, false, this.initialEffect);
                Bukkit.getPluginManager().callEvent(event);

                this.fishingPreparation.triggerActions(ActionTrigger.LAND);

            } else {
                FishHookLandEvent event = new FishHookLandEvent(this.fishingPreparation.getPlayer(), FishHookLandEvent.Target.WATER, this.fishHook, true, this.initialEffect);
                Bukkit.getPluginManager().callEvent(event);
            }

            this.setNextLoot();
            if (this.loot == null) {
                // prevent players from getting vanilla loots
                this.fishHook.setWaitTime(Integer.MAX_VALUE);
                this.tempEffect = new FishingEffect();
                this.tempEffect.merge(this.initialEffect);
                this.manager.removeTempFishingState(fishingPreparation.getPlayer());
                CustomFishingPlugin.get().debug("No loot available for " + fishingPreparation.getPlayer().getName() + " at " + fishingPreparation.getLocation());
            } else {
                this.tempEffect = this.loot.getBaseEffect().build(fishingPreparation.getPlayer(), fishingPreparation.getArgs());
                this.tempEffect.merge(this.initialEffect);
                this.setWaitTime();
                this.setTempState();
            }

            return;
        }
    }

    /**
     * Destroys the task and associated entities.
     */
    public void destroy() {
        this.cancelSubTask();
        this.removeTempEntity();
        this.hookMovementTask.cancel();
        this.manager.removeHookCheckTask(fishingPreparation.getPlayer());
    }

    /**
     * Cancels the lava fishing subtask if it's active.
     */
    public void cancelSubTask() {
        if (lavaFishingTask != null && !lavaFishingTask.isCancelled()) {
            lavaFishingTask.cancel();
            lavaFishingTask = null;
        }
    }

    private void setNextLoot() {
        Loot nextLoot = CustomFishingPlugin.get().getLootManager().getNextLoot(initialEffect, fishingPreparation);
        if (nextLoot == null) {
            this.loot = null;
            return;
        }
        this.loot = nextLoot;
    }

    /**
     * Sets temporary state and prepares for the next loot.
     */
    private void setTempState() {
        fishingPreparation.insertArg("{nick}", loot.getNick());
        fishingPreparation.insertArg("{loot}", loot.getID());
        if (!loot.disableStats()) {
            fishingPreparation.insertArg("{statistics_size}", loot.getStatisticKey().getSizeKey());
            fishingPreparation.insertArg("{statistics_amount}", loot.getStatisticKey().getAmountKey());
        }
        manager.setTempFishingState(fishingPreparation.getPlayer(), new TempFishingState(
                tempEffect,
                fishingPreparation,
                loot
        ));
    }

    /**
     * Removes the temporary hooked entity.
     */
    public void removeTempEntity() {
        if (hookedEntity != null && !hookedEntity.isDead())
            hookedEntity.remove();
    }

    /**
     * Starts the lava fishing mechanic.
     */
    private void startLavaFishingMechanic() {
        // get random time
        int random;
        if (CFConfig.overrideVanilla) {
            random = ThreadLocalRandom.current().nextInt(CFConfig.lavaMinTime, CFConfig.lavaMaxTime);
            random *= tempEffect.getWaitTimeMultiplier();
            random += tempEffect.getWaitTime();
            random = Math.max(1, random);
        } else {
            random = ThreadLocalRandom.current().nextInt(CFConfig.lavaMinTime, CFConfig.lavaMaxTime);
            random -= lureLevel * 100;
            random = Math.max(CFConfig.lavaMinTime, random);
            random *= tempEffect.getWaitTimeMultiplier();
            random += tempEffect.getWaitTime();
            random = Math.max(1, random);
        }

        // lava effect task (Three seconds in advance)
        this.lavaFishingTask = new LavaEffectTask(
                this,
                fishHook.getLocation(),
                random - 3 * 20
        );
    }

    /**
     * Handles the hook state of the fish hook.
     */
    public void getHooked() {
        LavaFishingEvent lavaFishingEvent = new LavaFishingEvent(fishingPreparation.getPlayer(), LavaFishingEvent.State.BITE, fishHook);
        Bukkit.getPluginManager().callEvent(lavaFishingEvent);
        if (lavaFishingEvent.isCancelled()) {
            this.startLavaFishingMechanic();
            return;
        }

        this.loot.triggerActions(ActionTrigger.BITE, fishingPreparation);
        this.fishingPreparation.triggerActions(ActionTrigger.BITE);

        this.fishHooked = true;
        this.removeTempEntity();

        AdventureManagerImpl.getInstance().sendSound(
                fishingPreparation.getPlayer(),
                Sound.Source.NEUTRAL,
                Key.key("minecraft:block.pointed_dripstone.drip_lava_into_cauldron"),
                1,
                1
        );

        CustomFishingPlugin.get().getScheduler().runTaskAsyncLater(() -> {
            fishHooked = false;
            reserve = false;
        }, (2 * 20) * 50L, TimeUnit.MILLISECONDS);
    }

    private void makeHookStatic(Location armorLoc) {
        armorLoc.setY(armorLoc.getBlockY() + 0.2);
        if (hookedEntity != null && !hookedEntity.isDead())
            hookedEntity.remove();
        hookedEntity = armorLoc.getWorld().spawn(armorLoc, ArmorStand.class, a -> {
            a.setInvisible(true);
            a.setCollidable(false);
            a.setInvulnerable(true);
            a.setVisible(false);
            a.setCustomNameVisible(false);
            a.setSmall(true);
            a.setGravity(false);
            a.getPersistentDataContainer().set(
                    Objects.requireNonNull(NamespacedKey.fromString("lavafishing", CustomFishingPlugin.get())),
                    PersistentDataType.STRING,
                    "temp"
            );
        });
        fishHook.setHookedEntity(hookedEntity);
    }

    /**
     * Checks if the fish hook is currently hooked.
     *
     * @return True if the fish hook is hooked, false otherwise.
     */
    public boolean isFishHooked() {
        return fishHooked;
    }

    private void setWaitTime() {
        if (CFConfig.overrideVanilla) {
            double initialTime = ThreadLocalRandom.current().nextInt(CFConfig.waterMaxTime - CFConfig.waterMinTime + 1) + CFConfig.waterMinTime;
            fishHook.setWaitTime(Math.max(1, (int) (initialTime * tempEffect.getWaitTimeMultiplier() + tempEffect.getWaitTime())));
        } else {
            fishHook.setMinWaitTime(Math.max(1, (int) (fishHook.getMinWaitTime() * tempEffect.getWaitTimeMultiplier() + tempEffect.getWaitTime())));
            fishHook.setMaxWaitTime(Math.max(2, (int) (fishHook.getMaxWaitTime() * tempEffect.getWaitTimeMultiplier() + tempEffect.getWaitTime())));
        }
    }
}
