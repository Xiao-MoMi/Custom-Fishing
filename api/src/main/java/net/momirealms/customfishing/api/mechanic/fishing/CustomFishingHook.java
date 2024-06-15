package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.fishing.hook.HookMechanic;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class CustomFishingHook {

    private static final Consumer<FishHook> hookConsumer = defaultHookLogics();
    private FishHook hook;
    private final SchedulerTask task;
    private HookMechanic hookMechanic;
    private Context<Player> context;

    public static Consumer<FishHook> defaultHookLogics() {
        return fishHook -> {


        };
    }

    public CustomFishingHook(FishHook hook, FishingGears gears, Context<Player> context, List<HookMechanic> enabledMechanics) {
        this.hook = hook;
        this.context = context;

        Effect effect = Effect.newInstance();
        for (EffectModifier modifier : gears.effectModifiers()) {
            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                consumer.accept(effect, context, 0);
            }
        }



        this.task = BukkitCustomFishingPlugin.getInstance().getScheduler().sync().runRepeating(() -> {
            // destroy if hook is invalid
            if (hook.isValid()) {
                BukkitCustomFishingPlugin.getInstance().getFishingManager().destroy(hook.getOwnerUniqueId());
                return;
            }
            for (HookMechanic mechanic : enabledMechanics) {
                if (mechanic.canStart()) {
                    if (hookMechanic != mechanic) {
                        if (hookMechanic != null) hookMechanic.destroy();
                        hookMechanic = mechanic;
                        mechanic.preStart();

                        Effect tempEffect = effect.copy();

                        for (EffectModifier modifier : gears.effectModifiers()) {
                            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                consumer.accept(tempEffect, context, 1);
                            }
                        }

                        Loot loot = BukkitCustomFishingPlugin.getInstance().getLootManager().getNextLoot(effect, context);
                        if (loot == null) {
                            // TODO warn
                            return;
                        }

                        Effect baseEffect = loot.baseEffect().toEffect(context);
                        tempEffect.combine(baseEffect);

                        for (EffectModifier modifier : gears.effectModifiers()) {
                            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                consumer.accept(tempEffect, context, 2);
                            }
                        }

                        mechanic.start(tempEffect);
                    }
                }
            }


        }, 1, 1, hook.getLocation());
    }

    public void cancel() {
        task.cancel();
        if (hookMechanic != null) hookMechanic.destroy();
    }
}
