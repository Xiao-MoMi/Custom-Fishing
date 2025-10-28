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
import net.momirealms.customfishing.api.mechanic.fishing.AntiAutoFishing;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.common.helper.VersionHelper;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.RandomUtils;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.concurrent.ThreadLocalRandom;

public class VanillaMechanic implements HookMechanic {

    private final FishHook hook;
    private final Context<Player> context;
    private SchedulerTask task;
    private boolean isHooked = false;
    private int tempWaitTime;
    private boolean freeze = false;

    public VanillaMechanic(FishHook hook, Context<Player> context) {
        this.hook = hook;
        this.context = context;
    }

    @Override
    public boolean canStart() {
        return hook.isInWater();
    }

    @Override
    public boolean shouldStop() {
        return hook.getState() != FishHook.HookState.BOBBING;
    }

    @Override
    public void preStart() {
        this.context.arg(ContextKeys.SURROUNDING, EffectProperties.WATER_FISHING.key());
    }

    @Override
    public void start(Effect finalEffect) {
        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.LAND));
        setWaitTime(hook, finalEffect);
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runRepeating(() -> {
            if (isHooked) {
                if (!isHooked()) {
                    isHooked = false;
                    setWaitTime(hook, finalEffect);
                }
            } else {
                if (isHooked()) {
                    isHooked = true;
                }
            }
        }, 1, 1, hook.getLocation());
    }

    private void setWaitTime(FishHook hook, Effect effect) {
        BukkitCustomFishingPlugin.getInstance().getScheduler().sync().run(() -> {
            if (freeze) {
                SparrowHeart.getInstance().setWaitTime(hook, Integer.MAX_VALUE);
                return;
            }
            if (!ConfigManager.overrideVanillaWaitTime()) {
                int rawBefore = SparrowHeart.getInstance().getWaitTime(hook);
                int before = Math.max(rawBefore, 0);
                int after = (int) Math.max(100, before * effect.waitTimeMultiplier() + effect.waitTimeAdder());
                BukkitCustomFishingPlugin.getInstance().debug("Wait time: " + rawBefore + " -> " + after + " ticks");
                SparrowHeart.getInstance().setWaitTime(hook, after);
            } else {
                int before = ThreadLocalRandom.current().nextInt(ConfigManager.waterMaxTime() - ConfigManager.waterMinTime() + 1) + ConfigManager.waterMinTime();
                int after = Math.min(Math.max(ConfigManager.finalWaterMinTime(), (int) (before * effect.waitTimeMultiplier() + effect.waitTimeAdder())), ConfigManager.finalWaterMaxTime());
                SparrowHeart.getInstance().setWaitTime(hook, after);
                BukkitCustomFishingPlugin.getInstance().debug("Wait time: " + before + " -> " + after + " ticks");
            }
            int lureTime = RandomUtils.generateRandomInt(20, 80);
            if (VersionHelper.isVersionNewerThan1_19_4()) {
                hook.setLureTime(lureTime, lureTime);
            } else {
                // the lowest value
                lureTime = 20;
            }
            if (ConfigManager.antiAutoFishingMod()) {
                BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runLater(() -> {
                    Player player = context.holder();
                    if (player.isOnline() && hook.isValid()) {
                        AntiAutoFishing.prevent(player, hook);
                    }
                },  RandomUtils.generateRandomInt(20, Math.max(20, SparrowHeart.getInstance().getWaitTime(hook) + lureTime - 5)), hook.getLocation());
            }
        }, hook.getLocation());
    }

    @Override
    public boolean isHooked() {
        return SparrowHeart.getInstance().isFishingHookBit(hook);
    }

    @Override
    public void destroy() {
        if (this.task != null) this.task.cancel();
        this.tempWaitTime = 0;
    }

    @Override
    public void freeze() {
        freeze = true;
        int waitTime = SparrowHeart.getInstance().getWaitTime(hook);
        if (waitTime > 0) {
            this.tempWaitTime = waitTime;
        }
        SparrowHeart.getInstance().setWaitTime(hook, Integer.MAX_VALUE);
    }

    @Override
    public void unfreeze(Effect effect) {
        freeze = false;
        if (this.tempWaitTime != 0) {
            SparrowHeart.getInstance().setWaitTime(hook, tempWaitTime);
            this.tempWaitTime = 0;
        } else {
            setWaitTime(hook, effect);
        }
    }

    public void onBite() {
        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.BITE));
    }

    public void onFailedAttempt() {
        EventUtils.fireAndForget(new FishingHookStateEvent(context.holder(), hook, FishingHookStateEvent.State.ESCAPE));
    }
}
