package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.fishing.hook.HookMechanic;
import net.momirealms.customfishing.api.mechanic.fishing.hook.LavaFishingMechanic;
import net.momirealms.customfishing.api.mechanic.fishing.hook.VanillaMechanic;
import net.momirealms.customfishing.api.mechanic.fishing.hook.VoidFishingMechanic;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.TriConsumer;
import net.momirealms.customfishing.common.util.TriFunction;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomFishingHook {

    private final FishHook hook;
    private final SchedulerTask task;
    private final FishingGears gears;
    private HookMechanic hookMechanic;
    private Loot nextLoot;
    private Context<Player> context;

    private static TriFunction<FishHook, Context<Player>, Effect, List<HookMechanic>> mechanicProviders = defaultMechanicProviders();

    public static TriFunction<FishHook, Context<Player>, Effect, List<HookMechanic>> defaultMechanicProviders() {
        return (h, c, e) -> {
            ArrayList<HookMechanic> mechanics = new ArrayList<>();
            mechanics.add(new VanillaMechanic(h, c));
            if (ConfigManager.enableLavaFishing()) mechanics.add(new LavaFishingMechanic(h, e, c));
            if (ConfigManager.enableVoidFishing()) mechanics.add(new VoidFishingMechanic(h, e, c));
            return mechanics;
        };
    }

    public static void mechanicProviders(TriFunction<FishHook, Context<Player>, Effect, List<HookMechanic>> mechanicProviders) {
        CustomFishingHook.mechanicProviders = mechanicProviders;
    }

    public CustomFishingHook(FishHook hook, FishingGears gears, Context<Player> context) {
        this.gears = gears;
        this.hook = hook;
        // once it becomes a custom hook, the wait time is controlled by plugin
        this.context = context;
        Effect effect = Effect.newInstance();
        // The effects impact mechanism at this stage
        for (EffectModifier modifier : gears.effectModifiers()) {
            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                consumer.accept(effect, context, 0);
            }
        }
        List<HookMechanic> enabledMechanics = mechanicProviders.apply(hook, context, effect);
        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runRepeating(() -> {
            // destroy if hook is invalid
            if (!hook.isValid()) {
                BukkitCustomFishingPlugin.getInstance().getFishingManager().destroy(hook.getOwnerUniqueId());
                return;
            }
            if (this.hookMechanic != null) {
                if (this.hookMechanic.shouldStop()) {
                    this.hookMechanic.destroy();
                    this.hookMechanic = null;
                }
            }
            for (HookMechanic mechanic : enabledMechanics) {
                // find the first available mechanic
                if (mechanic.canStart()) {
                    if (this.hookMechanic != mechanic) {
                        if (this.hookMechanic != null) this.hookMechanic.destroy();
                        this.hookMechanic = mechanic;
                        // to update some properties
                        mechanic.preStart();
                        Effect tempEffect = effect.copy();
                        for (EffectModifier modifier : gears.effectModifiers()) {
                            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                consumer.accept(tempEffect, context, 1);
                            }
                        }
                        // get the next loot
                        Loot loot = BukkitCustomFishingPlugin.getInstance().getLootManager().getNextLoot(effect, context);
                        if (loot == null) {
                            BukkitCustomFishingPlugin.getInstance().debug("No loot available for player " + context.getHolder().getName());
                            return;
                        }
                        this.nextLoot = loot;
                        BukkitCustomFishingPlugin.getInstance().debug("Next loot: " + loot.id());
                        // get its basic properties
                        Effect baseEffect = loot.baseEffect().toEffect(context);
                        tempEffect.combine(baseEffect);
                        // apply the gears' effects
                        for (EffectModifier modifier : gears.effectModifiers()) {
                            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                consumer.accept(tempEffect, context, 2);
                            }
                        }
                        // start the mechanic
                        mechanic.start(tempEffect);
                    }
                }
            }
        }, 1, 1, hook.getLocation());
    }

    public void destroy() {
        task.cancel();
        if (hook.isValid()) hook.remove();
        if (hookMechanic != null) hookMechanic.destroy();
    }

    @NotNull
    public FishHook getHookEntity() {
        return hook;
    }

    @Nullable
    public HookMechanic getCurrentHookMechanic() {
        return hookMechanic;
    }

    @Nullable
    public Loot getNextLoot() {
        return nextLoot;
    }

    public void onReelIn() {
        if (hookMechanic != null) {
            if (!hookMechanic.isHooked()) {
                destroy();
                System.out.println("fail");
            } else {
                System.out.println("succeed");
            }
        }
    }

    public void onBite() {
        gears.bite();
    }
}
