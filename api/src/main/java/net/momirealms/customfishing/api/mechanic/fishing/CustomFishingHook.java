package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.competition.CompetitionGoal;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.fishing.hook.HookMechanic;
import net.momirealms.customfishing.api.mechanic.fishing.hook.LavaFishingMechanic;
import net.momirealms.customfishing.api.mechanic.fishing.hook.VanillaMechanic;
import net.momirealms.customfishing.api.mechanic.fishing.hook.VoidFishingMechanic;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.TriConsumer;
import net.momirealms.customfishing.common.util.TriFunction;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.inventory.HandSlot;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CustomFishingHook {

    private final BukkitCustomFishingPlugin plugin;
    private final FishHook hook;
    private final SchedulerTask task;
    private final FishingGears gears;
    private final Context<Player> context;
    private Effect tempFinalEffect;
    private HookMechanic hookMechanic;
    private Loot nextLoot;

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

    public CustomFishingHook(BukkitCustomFishingPlugin plugin, FishHook hook, FishingGears gears, Context<Player> context) {
        this.gears = gears;
        this.gears.trigger(ActionTrigger.CAST, context);
        this.plugin = plugin;
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
        this.task = plugin.getScheduler().sync().runRepeating(() -> {
            // destroy if hook is invalid
            if (!hook.isValid()) {
                plugin.getFishingManager().destroy(hook.getOwnerUniqueId());
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

                        context.arg(ContextKeys.HOOK_LOCATION, hook.getLocation());
                        context.arg(ContextKeys.HOOK_X, hook.getLocation().getBlockX());
                        context.arg(ContextKeys.HOOK_Y, hook.getLocation().getBlockY());
                        context.arg(ContextKeys.HOOK_Z, hook.getLocation().getBlockZ());

                        // get the next loot
                        Loot loot = plugin.getLootManager().getNextLoot(effect, context);
                        if (loot == null) {
                            plugin.debug("No loot available for player " + context.getHolder().getName());
                            return;
                        }
                        this.nextLoot = loot;

                        context.arg(ContextKeys.ID, loot.id());
                        context.arg(ContextKeys.NICK, loot.nick());
                        context.arg(ContextKeys.LOOT, loot.type());

                        plugin.debug("Next loot: " + loot.id());
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

                        this.tempFinalEffect = tempEffect;
                    }
                }
            }
        }, 1, 1, hook.getLocation());
    }

    public void destroy() {
        if (task != null) task.cancel();
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
                gears.trigger(ActionTrigger.REEL, context);
                end();
            } else {
                if (nextLoot.disableGame() || RequirementManager.isSatisfied(context, ConfigManager.skipGameRequirements())) {
                    handleSuccessfulFishing();
                    end();
                } else {
                    handleSuccessfulFishing();
                    end();
                }
            }
        } else {
            gears.trigger(ActionTrigger.REEL, context);
            end();
        }
    }

    private void end() {
        plugin.getFishingManager().destroy(context.getHolder().getUniqueId());
    }

    public void onBite() {
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.getTypeByID(nextLoot.id()), ActionTrigger.BITE);
        gears.trigger(ActionTrigger.BITE, context);
        if (RequirementManager.isSatisfied(context, ConfigManager.autoFishingRequirements())) {
            handleSuccessfulFishing();
            SparrowHeart.getInstance().swingHand(context.getHolder(), gears.getRodSlot());
            end();
            scheduleNextFishing();
            return;
        }
        if (nextLoot.instantGame()) {

        }
    }

    private void scheduleNextFishing() {
        final Player player = context.getHolder();
        plugin.getScheduler().sync().runLater(() -> {
            if (player.isOnline()) {
                ItemStack item = player.getInventory().getItem(gears.getRodSlot() == HandSlot.MAIN ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
                if (item.getType() == Material.FISHING_ROD) {
                    SparrowHeart.getInstance().useItem(player, gears.getRodSlot(), item);
                    SparrowHeart.getInstance().swingHand(context.getHolder(), gears.getRodSlot());
                }
            }
        }, 20, player.getLocation());
    }

    public void onLand() {
        gears.trigger(ActionTrigger.LAND, context);
    }

    public void onEscape() {
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.getTypeByID(nextLoot.id()), ActionTrigger.ESCAPE);
        gears.trigger(ActionTrigger.ESCAPE, context);
    }

    public void onLure() {
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.getTypeByID(nextLoot.id()), ActionTrigger.LURE);
        gears.trigger(ActionTrigger.LURE, context);
    }

    private void handleFailedFishing() {

        // update the hook location
        context.arg(ContextKeys.HOOK_LOCATION, hook.getLocation());
        context.arg(ContextKeys.HOOK_X, hook.getLocation().getBlockX());
        context.arg(ContextKeys.HOOK_Y, hook.getLocation().getBlockY());
        context.arg(ContextKeys.HOOK_Z, hook.getLocation().getBlockZ());

        gears.trigger(ActionTrigger.FAILURE, context);
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.getTypeByID(nextLoot.id()), ActionTrigger.FAILURE);
    }

    private void handleSuccessfulFishing() {

        // update the hook location
        context.arg(ContextKeys.HOOK_LOCATION, hook.getLocation());
        context.arg(ContextKeys.HOOK_X, hook.getLocation().getBlockX());
        context.arg(ContextKeys.HOOK_Y, hook.getLocation().getBlockY());
        context.arg(ContextKeys.HOOK_Z, hook.getLocation().getBlockZ());

        LootType lootType = context.arg(ContextKeys.LOOT);
        Objects.requireNonNull(lootType, "Missing loot type");
        Objects.requireNonNull(tempFinalEffect, "Missing final effects");

        int amount;
        if (lootType == LootType.ITEM) {
            amount = (int) tempFinalEffect.multipleLootChance();
            amount += Math.random() < (tempFinalEffect.multipleLootChance() - amount) ? 2 : 1;
        } else {
            amount = 1;
        }
        // set the amount of loot
        context.arg(ContextKeys.AMOUNT, amount);

        FishingResultEvent event = new FishingResultEvent(context, FishingResultEvent.Result.SUCCESS, hook, nextLoot);
        if (EventUtils.fireAndCheckCancel(event)) {
            return;
        }

        switch (lootType) {
            case ITEM -> {
                for (int i = 0; i < amount; i++) {
                    plugin.getScheduler().sync().runLater(() -> {
                        Item item = plugin.getItemManager().dropItemLoot(context, gears.getItem(FishingGears.GearType.ROD).stream().findAny().orElseThrow().right(), hook);
                        if (item != null && Objects.equals(context.arg(ContextKeys.NICK), "UNDEFINED")) {
                            ItemStack stack = item.getItemStack();
                            Optional<String> displayName = plugin.getItemManager().wrap(stack).displayName();
                            if (displayName.isPresent()) {
                                context.arg(ContextKeys.NICK, AdventureHelper.jsonToMiniMessage(displayName.get()));
                            } else {
                                context.arg(ContextKeys.NICK, "<lang:" + stack.getType().translationKey() + ">");
                            }
                        }
                        doSuccessActions();
                    }, (long) ConfigManager.multipleLootSpawnDelay() * i, hook.getLocation());
                }
            }
            case BLOCK -> {
                plugin.getBlockManager().summonBlockLoot(context);
                doSuccessActions();
            }
            case ENTITY -> {
                plugin.getEntityManager().summonEntityLoot(context);
                doSuccessActions();
            }
        }

        gears.trigger(ActionTrigger.SUCCESS, context);
    }

    private void doSuccessActions() {
        FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
        if (competition != null && RequirementManager.isSatisfied(context, competition.getConfig().joinRequirements())) {
            Double customScore = context.arg(ContextKeys.CUSTOM_SCORE);
            if (customScore != null) {
                competition.refreshData(context.getHolder(), customScore);
                context.arg(ContextKeys.SCORE_FORMATTED, String.format("%.2f", customScore));
                context.arg(ContextKeys.SCORE, customScore);
            } else {
                double score = 0;
                if (competition.getGoal() == CompetitionGoal.CATCH_AMOUNT) {
                    score = 1;
                    competition.refreshData(context.getHolder(), score);
                } else if (competition.getGoal() == CompetitionGoal.MAX_SIZE || competition.getGoal() == CompetitionGoal.MIN_SIZE) {
                    Float size = context.arg(ContextKeys.SIZE);
                    if (size != null) {
                        competition.refreshData(context.getHolder(), size);
                    }
                } else if (competition.getGoal() == CompetitionGoal.TOTAL_SCORE) {
                    score = nextLoot.score().evaluate(context);
                    if (score != 0) {
                        competition.refreshData(context.getHolder(), score);
                    }
                }
                context.arg(ContextKeys.SCORE_FORMATTED, String.format("%.2f", score));
                context.arg(ContextKeys.SCORE, score);
            }
        } else {
            context.arg(ContextKeys.SCORE_FORMATTED, "0.0");
            context.arg(ContextKeys.SCORE, 0d);
        }

        String id = context.arg(ContextKeys.ID);
        Player player = context.getHolder();
        MechanicType type = MechanicType.getTypeByID(id);
        plugin.getEventManager().trigger(context, id, type, ActionTrigger.SUCCESS);
        player.setStatistic(Statistic.FISH_CAUGHT, player.getStatistic(Statistic.FISH_CAUGHT) + 1);
        if (!nextLoot.disableStats()) {
            plugin.getStorageManager().getOnlineUser(player.getUniqueId()).ifPresent(
                    userData -> {
                        userData.statistics().addAmount(id, 1);
                        Optional.ofNullable(context.arg(ContextKeys.SIZE)).ifPresent(size -> {
                            if (userData.statistics().updateSize(id, size)) {
                                plugin.getEventManager().trigger(context, id, type, ActionTrigger.NEW_SIZE_RECORD);
                            }
                        });
                    }
            );
        }
    }
}
