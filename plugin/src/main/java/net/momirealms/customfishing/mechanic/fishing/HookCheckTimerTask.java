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
import org.bukkit.entity.EntityType;
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
    private final int lureLevel;
    private boolean firstTime;
    private boolean fishHooked;
    private boolean reserve;
    private int jumpTimer;
    private Entity hookedEntity;
    private Loot loot;

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
        this.manager = manager;
        this.fishHook = fishHook;
        this.initialEffect = initialEffect;
        this.fishingPreparation = fishingPreparation;
        this.hookMovementTask = CustomFishingPlugin.get().getScheduler().runTaskSyncTimer(this, fishHook.getLocation(), 1, 1);
        this.lureLevel = fishingPreparation.getRodItemStack().getEnchantmentLevel(Enchantment.LURE);
        this.firstTime = true;
    }

    @Override
    public void run() {
        if (
            !fishHook.isValid()
            || fishHook.isOnGround()
            || (fishHook.getHookedEntity() != null && fishHook.getHookedEntity().getType() != EntityType.ARMOR_STAND)
        ) {
            // This task would be cancelled when hook is not at a proper place
            // or player reels in before it goes into water or lava
            this.destroy();
            return;
        }
        if (fishHook.getLocation().getBlock().getType() == Material.LAVA) {
            // if player can fish in lava
            if (firstTime) {
                this.fishingPreparation.setLocation(fishHook.getLocation());
                this.fishingPreparation.mergeEffect(initialEffect);
                if (!initialEffect.canLavaFishing()) {
                    this.destroy();
                    return;
                }
                this.fishingPreparation.insertArg("{lava}", "true");
                this.fishingPreparation.triggerActions(ActionTrigger.LAND);
                FishHookLandEvent event = new FishHookLandEvent(fishingPreparation.getPlayer(), FishHookLandEvent.Target.LAVA, fishHook, initialEffect);
                Bukkit.getPluginManager().callEvent(event);
                this.setWaitTime();
                this.firstTime = false;
                this.setTempState();
            }
            // simulate fishing mechanic
            if (fishHooked) {
                jumpTimer++;
                if (jumpTimer < 4)
                    return;
                jumpTimer = 0;
                fishHook.setVelocity(new Vector(0,0.24,0));
                return;
            }
            if (!reserve) {
                if (jumpTimer < 5) {
                    jumpTimer++;
                    fishHook.setVelocity(new Vector(0,0.2 - jumpTimer * 0.02,0));
                    return;
                }
                reserve = true;
                this.startLavaFishingMechanic();
                this.makeHookStatic(fishHook.getLocation());
            }
            return;
        }
        if (fishHook.isInWater()) {
            this.fishingPreparation.setLocation(fishHook.getLocation());
            this.fishingPreparation.mergeEffect(initialEffect);
            this.fishingPreparation.insertArg("{lava}", "false");
            this.fishingPreparation.insertArg("{open-water}", String.valueOf(fishHook.isInOpenWater()));
            this.fishingPreparation.triggerActions(ActionTrigger.LAND);
            FishHookLandEvent event = new FishHookLandEvent(fishingPreparation.getPlayer(), FishHookLandEvent.Target.WATER, fishHook, initialEffect);
            Bukkit.getPluginManager().callEvent(event);
            // if the hook is in water
            // then cancel the task
            this.destroy();
            this.setWaitTime();
            this.setTempState();
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

    /**
     * Sets temporary state and prepares for the next loot.
     */
    private void setTempState() {
        Loot nextLoot = CustomFishingPlugin.get().getLootManager().getNextLoot(initialEffect, fishingPreparation);
        if (nextLoot == null)
            return;
        this.loot = nextLoot;
        fishingPreparation.insertArg("{nick}", nextLoot.getNick());
        fishingPreparation.insertArg("{loot}", nextLoot.getID());
        if (!nextLoot.disableStats()) {
            fishingPreparation.insertArg("{statistics_size}", nextLoot.getStatisticKey().getSizeKey());
            fishingPreparation.insertArg("{statistics_amount}", nextLoot.getStatisticKey().getAmountKey());
        }
        CustomFishingPlugin.get().getScheduler().runTaskAsync(() -> manager.setTempFishingState(fishingPreparation.getPlayer(), new TempFishingState(
                initialEffect,
                fishingPreparation,
                nextLoot
        )));
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
            random *= initialEffect.getWaitTimeMultiplier();
            random += initialEffect.getWaitTime();
            random = Math.max(1, random);
        } else {
            random = ThreadLocalRandom.current().nextInt(CFConfig.lavaMinTime, CFConfig.lavaMaxTime);
            random -= lureLevel * 100;
            random = Math.max(CFConfig.lavaMinTime, random);
            random *= initialEffect.getWaitTimeMultiplier();
            random += initialEffect.getWaitTime();
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
            fishHook.setWaitTime(Math.max(1, (int) (initialTime * initialEffect.getWaitTimeMultiplier() + initialEffect.getWaitTime())));
        } else {
            fishHook.setMinWaitTime(Math.max(1, (int) (fishHook.getMinWaitTime() * initialEffect.getWaitTimeMultiplier() + initialEffect.getWaitTime())));
            fishHook.setMaxWaitTime(Math.max(2, (int) (fishHook.getMaxWaitTime() * initialEffect.getWaitTimeMultiplier() + initialEffect.getWaitTime())));
        }
    }
}
