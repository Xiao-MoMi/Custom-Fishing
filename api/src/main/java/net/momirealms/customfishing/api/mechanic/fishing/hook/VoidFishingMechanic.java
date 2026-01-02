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
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.RandomUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class VoidFishingMechanic implements HookMechanic {
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
    private int timer;
    private boolean freeze;

    public VoidFishingMechanic(FishHook hook, Effect gearsEffect, Context<Player> context) {
        this.hook = hook;
        this.gearsEffect = gearsEffect;
        this.context = context;
    }

    @Override
    public boolean canStart() {
        if (!(boolean) gearsEffect.properties().getOrDefault(EffectProperties.VOID_FISHING, false)) {
            return false;
        }
        return hook.getLocation().getY() <= hook.getWorld().getMinHeight();
    }

    @Override
    public boolean shouldStop() {
        return hook.getLocation().getY() > hook.getWorld().getMinHeight();
    }

    @Override
    public void preStart() {
        this.context.arg(ContextKeys.SURROUNDING, EffectProperties.VOID_FISHING.key());
    }

    @Override
    public void start(Effect finalEffect) {
        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.LAND));
        this.setWaitTime(finalEffect);
        this.tempEntity = hook.getWorld().spawn(hook.getLocation().clone().subtract(0,1,0), ArmorStand.class);
        this.setTempEntityProperties(this.tempEntity);
        this.hook.setHookedEntity(this.tempEntity);
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runRepeating(() -> {
            Location location = hook.getLocation();
            timer++;
            if (timer % 2 == 0) {
                if (timer >= 16) timer = 0;
                hook.getWorld().spawnParticle(Particle.END_ROD, location.getX() + 0.5 * Math.cos(timer * 22.5D * 0.017453292F), location.getY() - 0.15, location.getZ() + 0.5 * Math.sin(timer * 22.5D * 0.017453292F), 0,0,0,0);
            }
            if (this.nibble > 0) {
                --this.nibble;
                if (this.nibble % 4 == 0) {
                    if (RandomUtils.generateRandomDouble(0, 1) < 0.5) {
                        hook.getWorld().spawnParticle(Particle.END_ROD, location.getX(), location.getY(), location.getZ(), (int) (1.0F + 0.3 * 20.0F), 0.3, 0.0D, 0.3, 0.10000000298023224D);
                    } else {
                        hook.getWorld().spawnParticle(Particle.DRAGON_BREATH, location.getX(), location.getY(), location.getZ(), (int) (1.0F + 0.3 * 20.0F), 0.3, 0.0D, 0.3, 0.10000000298023224D);
                    }
                }
                if (this.nibble <= 0) {
                    this.timeUntilLured = 0;
                    this.timeUntilHooked = 0;
                    this.hooked = false;
                    EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.ESCAPE));
                }
            } else {
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
                            hook.getWorld().spawnParticle(Particle.END_ROD, d0, d1 - 0.10000000149011612D, d2, 1, f1, 0.1D, f2, 0.0D);
                        }
                        float f3 = f1 * 0.04F;
                        float f4 = f2 * 0.04F;
                        hook.getWorld().spawnParticle(Particle.END_ROD, d0, d1, d2, 0, f4, 0.01D, -f3, 1.0D);
                    } else {
                        double d3 = location.getY() + 0.5D;
                        hook.getWorld().spawnParticle(Particle.END_ROD, location.getX(), d3, location.getZ(), (int) (1.0F + 0.3 * 20.0F), 0.3, 0.0D, 0.3, 0.20000000298023224D);
                        this.nibble = RandomUtils.generateRandomInt(20, 40);
                        this.hooked = true;
                        hook.getWorld().playSound(location, Sound.ITEM_TRIDENT_THUNDER, 0.25F, 1.0F + (RandomUtils.generateRandomFloat(0,1)-RandomUtils.generateRandomFloat(0,1)) * 0.4F);
                        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.BITE));
                    }
                } else if (timeUntilLured > 0) {
                    if (!freeze) {
                        timeUntilLured--;
                    }
                    f = 0.1F;
                    if (this.timeUntilLured < 20) {
                        f += (float) (20 - this.timeUntilLured) * 0.05F;
                    } else if (this.timeUntilLured < 40) {
                        f += (float) (40 - this.timeUntilLured) * 0.02F;
                    } else if (this.timeUntilLured < 60) {
                        f += (float) (60 - this.timeUntilLured) * 0.01F;
                    }
                    if (RandomUtils.generateRandomFloat(0, 1) < f) {
                        f1 = RandomUtils.generateRandomFloat(0.0F, 360.0F) * 0.017453292F;
                        f2 = RandomUtils.generateRandomFloat(25.0F, 60.0F);
                        d0 = location.getX() + Math.sin(f1) * f2 * 0.1D;
                        d1 = location.getY();
                        d2 = location.getZ() + Math.cos(f1) * f2 * 0.1D;
                        if (VersionHelper.isVersionNewerThan1_21_9()) {
                            this.hook.getWorld().spawnParticle(Particle.DRAGON_BREATH, d0, d1, d2, 2 + RandomUtils.generateRandomInt(0,1), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D, 0f);
                        } else {
                            this.hook.getWorld().spawnParticle(Particle.DRAGON_BREATH, d0, d1, d2, 2 + RandomUtils.generateRandomInt(0,1), 0.10000000149011612D, 0.0D, 0.10000000149011612D, 0.0D);
                        }
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
    public void unfreeze(Effect finalEffect) {
        freeze = false;
    }

    private void setWaitTime(Effect effect) {
        int before = ThreadLocalRandom.current().nextInt(ConfigManager.voidMaxTime() - ConfigManager.voidMinTime() + 1) + ConfigManager.voidMinTime();
        int after = Math.min(Math.max(ConfigManager.finalVoidMinTime(), (int) (before * effect.waitTimeMultiplier() + effect.waitTimeAdder())), ConfigManager.finalVoidMaxTime());
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
                "void"
        );
    }
}
