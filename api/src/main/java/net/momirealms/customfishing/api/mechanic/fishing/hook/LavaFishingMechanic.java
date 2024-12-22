/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingHookStateEvent;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.RandomUtils;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.fluid.FluidData;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class LavaFishingMechanic implements HookMechanic {

    private final FishHook hook;
    private final Effect gearsEffect;
    private final Context<Player> context;
    private ArmorStand tempEntity;
    private SchedulerTask task;
    private int timeUntilLured;
    private int timeUntilHooked;
    private int nibble;
    private boolean hooked;
    private float fishAngle;
    private int currentState;
    private int jumpTimer;
    private boolean firstTime = true;
    private boolean freeze = false;

    public LavaFishingMechanic(FishHook hook, Effect gearsEffect, Context<Player> context) {
        this.hook = hook;
        this.gearsEffect = gearsEffect;
        this.context = context;
    }

    @Override
    public boolean canStart() {
        if (!(boolean) gearsEffect.properties().getOrDefault(EffectProperties.LAVA_FISHING, false)) {
            return false;
        }
        if (hook.isInLava()) {
            return true;
        }
        float lavaHeight = 0F;
        Location location = this.hook.getLocation();
        FluidData fluidData = SparrowHeart.getInstance().getFluidData(location);
        if (fluidData.getFluidType() == Fluid.LAVA || fluidData.getFluidType() == Fluid.FLOWING_LAVA) {
            lavaHeight = (float) (fluidData.getLevel() * 0.125);
        }
        return lavaHeight > 0 && location.getY() % 1 <= lavaHeight;
    }

    @Override
    public boolean shouldStop() {
        if (hook.isInLava()) {
            return false;
        }
        return hook.isOnGround() || (hook.getLocation().getBlock().getType() != Material.LAVA && hook.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.LAVA);
    }

    @Override
    public void preStart() {
        this.context.arg(ContextKeys.SURROUNDING, EffectProperties.LAVA_FISHING.key());
    }

    @Override
    public void start(Effect finalEffect) {
        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.LAND));
        this.setWaitTime(finalEffect);
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runRepeating(() -> {
            Location location = this.hook.getLocation();
            float lavaHeight = 0F;
            FluidData fluidData = SparrowHeart.getInstance().getFluidData(location);
            if (fluidData.getFluidType() == Fluid.LAVA || fluidData.getFluidType() == Fluid.FLOWING_LAVA) {
                lavaHeight = (float) (fluidData.getLevel() * 0.125);
            }
            if (this.nibble > 0) {
                --this.nibble;
                if (location.getY() % 1 <= lavaHeight) {
                    this.jumpTimer++;
                    if (this.jumpTimer >= 4) {
                        this.jumpTimer = 0;
                        this.hook.setVelocity(new Vector(0,0.24,0));
                    }
                }
                if (this.nibble <= 0) {
                    this.timeUntilLured = 0;
                    this.timeUntilHooked = 0;
                    this.hooked = false;
                    this.jumpTimer = 0;
                    this.currentState = 0;
                }
            } else {
                double hookY = location.getY();
                if (hookY < 0) {
                    hookY += Math.abs(Math.floor(hookY));
                }
                if (hookY % 1 <= lavaHeight || this.hook.isInLava()) {
                    Vector previousVector = this.hook.getVelocity();
                    this.hook.setVelocity(new Vector(previousVector.getX() * 0.6, Math.min(0.1, Math.max(-0.1, previousVector.getY() + 0.07)), previousVector.getZ() * 0.6));
                    this.currentState = 1;
                } else {
                    if (currentState == 1) {
                        this.currentState = 0;
                        // set temp entity
                        this.tempEntity = this.hook.getWorld().spawn(location.clone().subtract(0,1,0), ArmorStand.class);
                        this.setTempEntityProperties(this.tempEntity);
                        this.hook.setHookedEntity(this.tempEntity);
                        if (!firstTime) {
                            EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.ESCAPE));
                        }
                        firstTime = false;
                    }
                }
                float f;
                float f1;
                float f2;
                double d0;
                double d1;
                double d2;
                if (this.timeUntilHooked > 0) {
                    this.timeUntilHooked -= 1;
                    if (this.timeUntilHooked > 0) {
                        this.fishAngle += (float) RandomUtils.triangle(0.0D, 9.188D);
                        f = this.fishAngle * 0.017453292F;
                        f1 = (float) Math.sin(f);
                        f2 = (float) Math.cos(f);
                        d0 = location.getX() + (double) (f1 * (float) this.timeUntilHooked * 0.1F);
                        d1 = location.getY();
                        d2 = location.getZ() + (double) (f2 * (float) this.timeUntilHooked * 0.1F);
                        if (RandomUtils.generateRandomFloat(0,1) < 0.15F) {
                            hook.getWorld().spawnParticle(Particle.FLAME, d0, d1 - 0.10000000149011612D, d2, 1, f1, 0.1D, f2, 0.0D);
                        }
                        float f3 = f1 * 0.04F;
                        float f4 = f2 * 0.04F;
                        hook.getWorld().spawnParticle(Particle.FLAME, d0, d1, d2, 0, f4, 0.01D, -f3, 1.0D);
                    } else {
                        double d3 = location.getY() + 0.5D;
                        hook.getWorld().spawnParticle(Particle.FLAME, location.getX(), d3, location.getZ(), (int) (1.0F + 0.3 * 20.0F), 0.3, 0.0D, 0.3, 0.20000000298023224D);
                        this.nibble = RandomUtils.generateRandomInt(20, 40);
                        this.hooked = true;
                        hook.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25F, 1.0F + (RandomUtils.generateRandomFloat(0,1)-RandomUtils.generateRandomFloat(0,1)) * 0.4F);
                        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.BITE));
                        if (this.tempEntity != null && this.tempEntity.isValid()) {
                            this.tempEntity.remove();
                        }
                    }
                } else if (timeUntilLured > 0) {
                    if (!freeze) {
                        timeUntilLured--;
                    }
                    if (this.timeUntilLured <= 0) {
                        this.fishAngle = RandomUtils.generateRandomFloat(0F, 360F);
                        this.timeUntilHooked = RandomUtils.generateRandomInt(20, 80);
                        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.LURE));
                    }
                } else {
                    setWaitTime(finalEffect);
                }
            }
        }, 1, 1, hook.getLocation());
    }

    @Override
    public boolean isHooked() {
        return hooked;
    }

    @Override
    public void destroy() {
        if (this.tempEntity != null && this.tempEntity.isValid()) {
            this.tempEntity.remove();
        }
        if (this.task != null) {
            this.task.cancel();
        }
        freeze = false;
    }

    @Override
    public void freeze() {
        freeze = true;
    }

    @Override
    public void unfreeze(Effect effect) {
        freeze = false;
    }

    private void setWaitTime(Effect effect) {
        int before = ThreadLocalRandom.current().nextInt(ConfigManager.lavaMaxTime() - ConfigManager.lavaMinTime() + 1) + ConfigManager.lavaMinTime();
        int after = Math.min(Math.max(ConfigManager.finalLavaMinTime(), (int) (before * effect.waitTimeMultiplier() + effect.waitTimeAdder())), ConfigManager.finalLavaMaxTime());
        BukkitCustomFishingPlugin.getInstance().debug("Wait time: " + before + " -> " + after + " ticks");
        this.timeUntilLured = after;
    }

    private void setTempEntityProperties(ArmorStand entity) {
        entity.setInvisible(true);
        entity.setCollidable(false);
        entity.setInvulnerable(true);
        entity.setVisible(false);
        entity.setCustomNameVisible(false);
        entity.setSmall(true);
        entity.setGravity(false);
        entity.getPersistentDataContainer().set(
                Objects.requireNonNull(NamespacedKey.fromString("temp-entity", BukkitCustomFishingPlugin.getInstance().getBootstrap())),
                PersistentDataType.STRING,
                "lava"
        );
    }
}
