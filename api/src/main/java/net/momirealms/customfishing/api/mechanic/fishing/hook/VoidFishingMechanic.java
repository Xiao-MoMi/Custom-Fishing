package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

public class VoidFishingMechanic implements HookMechanic {

    private final FishHook hook;
    private final Effect gearsEffect;
    private final Context<Player> context;

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
    public void preStart() {
        this.context.arg(ContextKeys.SURROUNDING, EffectProperties.VOID_FISHING.key());
    }

    @Override
    public void start(Effect finalEffect) {

    }

    @Override
    public boolean isHooked() {
        return false;
    }

    @Override
    public void destroy() {

    }
}
