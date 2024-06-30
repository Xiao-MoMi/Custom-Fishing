package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectProperties;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

public class LavaFishingMechanic implements HookMechanic {

    private final FishHook hook;
    private final Effect gearsEffect;
    private final Context<Player> context;

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
        return hook.getLocation().getY() <= hook.getWorld().getMinHeight();
    }

    @Override
    public void preStart() {
        this.context.arg(ContextKeys.SURROUNDING, EffectProperties.LAVA_FISHING.key());
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
