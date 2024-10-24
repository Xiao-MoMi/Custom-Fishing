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

package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingEffectApplyEvent;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import net.momirealms.customfishing.api.mechanic.MechanicType;
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
import net.momirealms.customfishing.api.mechanic.game.Game;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.api.util.PlayerUtils;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.TriConsumer;
import net.momirealms.customfishing.common.util.TriFunction;
import net.momirealms.sparrow.heart.SparrowHeart;
import net.momirealms.sparrow.heart.feature.inventory.HandSlot;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents a custom fishing hook.
 */
public class CustomFishingHook {

    private final BukkitCustomFishingPlugin plugin;
    private final FishHook hook;
    private final SchedulerTask task;
    private final FishingGears gears;
    private final Context<Player> context;
    private Effect tempFinalEffect;
    private HookMechanic hookMechanic;
    private Loot nextLoot;
    private GamingPlayer gamingPlayer;
    private BaitAnimationTask baitAnimationTask;
    private boolean valid = true;

    private static TriFunction<FishHook, Context<Player>, Effect, List<HookMechanic>> mechanicProviders = defaultMechanicProviders();

    /**
     * Provides the default mechanic providers for the custom fishing hook.
     *
     * @return a TriFunction that provides a list of HookMechanic instances.
     */
    public static TriFunction<FishHook, Context<Player>, Effect, List<HookMechanic>> defaultMechanicProviders() {
        return (h, c, e) -> {
            ArrayList<HookMechanic> mechanics = new ArrayList<>();
            mechanics.add(new VanillaMechanic(h, c));
            if (ConfigManager.enableLavaFishing()) mechanics.add(new LavaFishingMechanic(h, e, c));
            if (ConfigManager.enableVoidFishing()) mechanics.add(new VoidFishingMechanic(h, e, c));
            return mechanics;
        };
    }

    /**
     * Sets the mechanic providers for the custom fishing hook.
     *
     * @param mechanicProviders the TriFunction to set.
     */
    public static void mechanicProviders(TriFunction<FishHook, Context<Player>, Effect, List<HookMechanic>> mechanicProviders) {
        CustomFishingHook.mechanicProviders = mechanicProviders;
    }

    /**
     * Constructs a new CustomFishingHook.
     *
     * @param plugin  the BukkitCustomFishingPlugin instance.
     * @param hook    the FishHook entity.
     * @param gears   the FishingGears instance.
     * @param context the context of the player.
     */
    public CustomFishingHook(BukkitCustomFishingPlugin plugin, FishHook hook, FishingGears gears, Context<Player> context) {
        this.gears = gears;
        // enable bait animation
        if (ConfigManager.baitAnimation() && !gears.getItem(FishingGears.GearType.BAIT).isEmpty()) {
            this.baitAnimationTask = new BaitAnimationTask(plugin, context.holder(), hook, gears.getItem(FishingGears.GearType.BAIT).get(0).right());
        }
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

        // trigger event
        EventUtils.fireAndForget(new FishingEffectApplyEvent(this, effect, FishingEffectApplyEvent.Stage.CAST));

        List<HookMechanic> enabledMechanics = mechanicProviders.apply(hook, context, effect);
        this.task = plugin.getScheduler().sync().runRepeating(() -> {
            // destroy if hook is invalid
            if (!hook.isValid()) {
                plugin.getFishingManager().destroyHook(context.holder().getUniqueId());
                return;
            }
            if (isPlayingGame()) {
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

                        // remove bait animation if there exists
                        if (this.baitAnimationTask != null) {
                            this.baitAnimationTask.cancel();
                            this.baitAnimationTask = null;
                        }

                        // to update some properties
                        mechanic.preStart();
                        Effect tempEffect = effect.copy();

                        for (EffectModifier modifier : gears.effectModifiers()) {
                            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                consumer.accept(tempEffect, context, 1);
                            }
                        }

                        // trigger event
                        EventUtils.fireAndForget(new FishingEffectApplyEvent(this, tempEffect, FishingEffectApplyEvent.Stage.LOOT));

                        context.arg(ContextKeys.OTHER_LOCATION, hook.getLocation());
                        context.arg(ContextKeys.OTHER_X, hook.getLocation().getBlockX());
                        context.arg(ContextKeys.OTHER_Y, hook.getLocation().getBlockY());
                        context.arg(ContextKeys.OTHER_Z, hook.getLocation().getBlockZ());

                        // get the next loot


                        Loot loot;
                        try {
                            loot = plugin.getLootManager().getNextLoot(tempEffect, context);
                        } catch (Exception e) {
                            loot = null;
                            plugin.getPluginLogger().warn("Error occurred when getting next loot.", e);
                        }
                        if (loot != null) {
                            this.nextLoot = loot;

                            context.arg(ContextKeys.ID, loot.id());
                            context.arg(ContextKeys.NICK, loot.nick());
                            context.arg(ContextKeys.LOOT, loot.type());

                            context.clearCustomData();
                            for (Map.Entry<String, TextValue<Player>> entry : loot.customData().entrySet()) {
                                context.arg(ContextKeys.of("data_" + entry.getKey(), String.class), entry.getValue().render(context));
                            }

                            plugin.debug("Next loot: " + loot.id());
                            plugin.debug(context);
                            // get its basic properties
                            Effect baseEffect = loot.baseEffect().toEffect(context);
                            plugin.debug(baseEffect);
                            tempEffect.combine(baseEffect);
                            // apply the gears' effects
                            for (EffectModifier modifier : gears.effectModifiers()) {
                                for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                    consumer.accept(tempEffect, context, 2);
                                }
                            }

                            // trigger event
                            EventUtils.fireAndForget(new FishingEffectApplyEvent(this, tempEffect, FishingEffectApplyEvent.Stage.FISHING));

                            // start the mechanic
                            mechanic.start(tempEffect);

                            this.tempFinalEffect = tempEffect;
                        } else {
                            mechanic.start(tempEffect);
                            this.tempFinalEffect = tempEffect;
                            // to prevent players from getting any loot
                            mechanic.freeze();
                        }
                    }
                }
            }
        }, 1, 1, hook.getLocation());
    }

    /**
     * stops the custom fishing hook. In most cases, you should use {@link CustomFishingHook#destroy()} instead
     */
    @ApiStatus.Internal
    public void stop() {
        if (!this.valid) return;
        this.valid = false;
        if (this.task != null) this.task.cancel();
        if (this.hook.isValid()) this.hook.remove();
        if (this.hookMechanic != null) hookMechanic.destroy();
        if (this.gamingPlayer != null) gamingPlayer.destroy();
        if (this.baitAnimationTask != null) {
            this.baitAnimationTask.cancel();
            this.baitAnimationTask = null;
        }
    }

    /**
     * Ends the life of the custom fishing hook.
     */
    public void destroy() {
        // if the hook exists in cache
        this.plugin.getFishingManager().destroyHook(this.context.holder().getUniqueId());
        // if not, then destroy the tasks. This should never happen
        if (this.valid) {
            stop();
        }
    }

    /**
     * Gets the context of the player.
     *
     * @return the context.
     */
    public Context<Player> getContext() {
        return this.context;
    }

    /**
     * Gets the FishHook entity.
     *
     * @return the FishHook entity.
     */
    @NotNull
    public FishHook getHookEntity() {
        return this.hook;
    }

    /**
     * Gets the current hook mechanic.
     *
     * @return the current HookMechanic, or null if none.
     */
    @Nullable
    public HookMechanic getCurrentHookMechanic() {
        return hookMechanic;
    }

    /**
     * Gets the next loot.
     *
     * @return the next Loot, or null if none.
     */
    @Nullable
    public Loot getNextLoot() {
        return nextLoot;
    }

    /**
     * Checks if the player is currently playing a game.
     *
     * @return true if the player is playing a game, false otherwise.
     */
    public boolean isPlayingGame() {
        return gamingPlayer != null && gamingPlayer.isValid();
    }

    public boolean isHookValid() {
        if (hook == null) return false;
        return hook.isValid() && valid;
    }

    /**
     * Cancels the current game.
     */
    public void cancelCurrentGame() {
        if (gamingPlayer == null || !gamingPlayer.isValid()) {
            return;
        }
        gamingPlayer.cancel();
        gamingPlayer = null;
        if (hookMechanic != null) {
            hookMechanic.unfreeze(tempFinalEffect);
        }
    }

    /**
     * Starts a game.
     */
    public void gameStart() {
        if (isPlayingGame() || !hook.isValid())
            return;
        Game nextGame = plugin.getGameManager().getNextGame(tempFinalEffect, context);
        if (nextGame != null) {
            plugin.debug("Next game: " + nextGame.id());
            gamingPlayer = nextGame.start(this, tempFinalEffect);
            if (this.hookMechanic != null) {
                BukkitCustomFishingPlugin.getInstance().debug("Freezing current mechanic");
                this.hookMechanic.freeze();
            }
        } else {
            plugin.debug("Next game: " + "`null`");
            handleSuccessfulFishing();
            destroy();
        }
    }

    /**
     * Gets the gaming player.
     *
     * @return an Optional containing the GamingPlayer if present, otherwise empty.
     */
    public Optional<GamingPlayer> getGamingPlayer() {
        return Optional.ofNullable(gamingPlayer);
    }

    // auto fishing
    private void scheduleNextFishing() {
        final Player player = context.holder();
        plugin.getScheduler().sync().runLater(() -> {
            if (player.isOnline()) {
                ItemStack item = player.getInventory().getItem(gears.getRodSlot() == HandSlot.MAIN ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
                if (item.getType() == Material.FISHING_ROD) {
                    SparrowHeart.getInstance().useItem(player, gears.getRodSlot(), item);
                    SparrowHeart.getInstance().swingHand(context.holder(), gears.getRodSlot());
                }
            }
        }, 20, player.getLocation());
    }

    /**
     * Handles the reel-in action.
     */
    public void onReelIn() {
        if (isPlayingGame() || !hook.isValid()) return;
        if (hookMechanic != null) {
            if (!hookMechanic.isHooked()) {
                gears.trigger(ActionTrigger.REEL, context);
                destroy();
            } else {
                if (nextLoot.disableGame() || RequirementManager.isSatisfied(context, ConfigManager.skipGameRequirements())) {
                    handleSuccessfulFishing();
                    destroy();
                } else {
                    plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.HOOK);
                    gears.trigger(ActionTrigger.HOOK, context);
                    gameStart();
                }
            }
        } else {
            gears.trigger(ActionTrigger.REEL, context);
            destroy();
        }
    }


    /**
     * Handles the bite action.
     */
    public void onBite() {
        if (isPlayingGame() || !hook.isValid()) return;
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.BITE);
        gears.trigger(ActionTrigger.BITE, context);
        if (RequirementManager.isSatisfied(context, ConfigManager.autoFishingRequirements())) {
            handleSuccessfulFishing();
            SparrowHeart.getInstance().swingHand(context.holder(), gears.getRodSlot());
            destroy();
            scheduleNextFishing();
            return;
        }
        if (nextLoot.instantGame()) {
            gameStart();
        }
    }

    /**
     * Handles the landing action.
     */
    public void onLand() {
        if (!hook.isValid()) return;
        gears.trigger(ActionTrigger.LAND, context);
    }

    /**
     * Handles the escape action.
     */
    public void onEscape() {
        if (isPlayingGame() || !hook.isValid()) return;
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.ESCAPE);
        gears.trigger(ActionTrigger.ESCAPE, context);
    }

    /**
     * Handles the lure action.
     */
    public void onLure() {
        if (isPlayingGame() || !hook.isValid()) return;
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.LURE);
        gears.trigger(ActionTrigger.LURE, context);
    }

    /**
     * Handles a failed fishing attempt.
     */
    public void handleFailedFishing() {

        if (!valid) return;

        // update the hook location
        context.arg(ContextKeys.OTHER_LOCATION, hook.getLocation());
        context.arg(ContextKeys.OTHER_X, hook.getLocation().getBlockX());
        context.arg(ContextKeys.OTHER_Y, hook.getLocation().getBlockY());
        context.arg(ContextKeys.OTHER_Z, hook.getLocation().getBlockZ());

        gears.trigger(ActionTrigger.FAILURE, context);
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.FAILURE);
    }

    /**
     * Handles a successful fishing attempt.
     */
    public void handleSuccessfulFishing() {

        if (!valid) return;

        // update the hook location
        Location hookLocation = hook.getLocation();
        context.arg(ContextKeys.OTHER_LOCATION, hookLocation);
        context.arg(ContextKeys.OTHER_X, hookLocation.getBlockX());
        context.arg(ContextKeys.OTHER_Y, hookLocation.getBlockY());
        context.arg(ContextKeys.OTHER_Z, hookLocation.getBlockZ());

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

        gears.trigger(ActionTrigger.SUCCESS, context);

        switch (lootType) {
            case ITEM -> {
                context.arg(ContextKeys.SIZE_MULTIPLIER, tempFinalEffect.sizeMultiplier());
                context.arg(ContextKeys.SIZE_ADDER, tempFinalEffect.sizeAdder());
                boolean directlyToInventory = nextLoot.toInventory().evaluate(context) != 0;
                for (int i = 0; i < amount; i++) {
                    plugin.getScheduler().sync().runLater(() -> {
                        if (directlyToInventory) {
                            ItemStack stack = plugin.getItemManager().getItemLoot(context, gears.getItem(FishingGears.GearType.ROD).stream().findAny().orElseThrow().right(), hook);
                            if (stack.getType() != Material.AIR) {
                                if (Objects.equals(context.arg(ContextKeys.NICK), "UNDEFINED")) {
                                    Optional<String> displayName = plugin.getItemManager().wrap(stack).displayName();
                                    if (displayName.isPresent()) {
                                        context.arg(ContextKeys.NICK, AdventureHelper.jsonToMiniMessage(displayName.get()));
                                    } else {
                                        context.arg(ContextKeys.NICK, "<lang:" + stack.getType().translationKey() + ">");
                                    }
                                }
                                PlayerUtils.giveItem(context.holder(), stack, stack.getAmount());
                            }
                        } else {
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
                            if (item != null) {
                                FishingLootSpawnEvent spawnEvent = new FishingLootSpawnEvent(context, hookLocation, nextLoot, item);
                                Bukkit.getPluginManager().callEvent(spawnEvent);
                                if (!spawnEvent.summonEntity())
                                    item.remove();
                                if (spawnEvent.skipActions())
                                    return;
                                if (item.isValid() && nextLoot.preventGrabbing()) {
                                    item.getPersistentDataContainer().set(Objects.requireNonNull(NamespacedKey.fromString("owner", plugin.getBootstrap())), PersistentDataType.STRING, context.holder().getName());
                                }
                            }
                        }
                        doSuccessActions();
                    }, (long) ConfigManager.multipleLootSpawnDelay() * i, hookLocation);
                }
            }
            case BLOCK -> {
                FallingBlock fallingBlock = plugin.getBlockManager().summonBlockLoot(context);
                FishingLootSpawnEvent spawnEvent = new FishingLootSpawnEvent(context, hook.getLocation(), nextLoot, fallingBlock);
                Bukkit.getPluginManager().callEvent(spawnEvent);
                if (!spawnEvent.summonEntity())
                    fallingBlock.remove();
                if (spawnEvent.skipActions())
                    return;
                doSuccessActions();
            }
            case ENTITY -> {
                Entity entity = plugin.getEntityManager().summonEntityLoot(context);
                FishingLootSpawnEvent spawnEvent = new FishingLootSpawnEvent(context, hook.getLocation(), nextLoot, entity);
                Bukkit.getPluginManager().callEvent(spawnEvent);
                if (!spawnEvent.summonEntity())
                    entity.remove();
                if (spawnEvent.skipActions())
                    return;
                doSuccessActions();
            }
        }
    }

    private void doSuccessActions() {
        FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
        if (competition != null && RequirementManager.isSatisfied(context, competition.getConfig().joinRequirements())) {
            Double customScore = context.arg(ContextKeys.CUSTOM_SCORE);
            if (customScore != null) {
                competition.refreshData(context.holder(), customScore);
                context.arg(ContextKeys.SCORE_FORMATTED, String.format("%.2f", customScore));
                context.arg(ContextKeys.SCORE, customScore);
            } else {
                double score = 0;
                if (competition.getGoal() == CompetitionGoal.CATCH_AMOUNT) {
                    score = 1;
                    competition.refreshData(context.holder(), score);
                } else if (competition.getGoal() == CompetitionGoal.MAX_SIZE || competition.getGoal() == CompetitionGoal.MIN_SIZE || competition.getGoal() == CompetitionGoal.TOTAL_SIZE) {
                    Float size = context.arg(ContextKeys.SIZE);
                    if (size != null && size > 0) {
                        competition.refreshData(context.holder(), size);
                    }
                } else if (competition.getGoal() == CompetitionGoal.TOTAL_SCORE) {
                    score = nextLoot.score().evaluate(context);
                    score = score * tempFinalEffect.scoreMultiplier() + tempFinalEffect.scoreAdder();
                    if (score != 0) {
                        competition.refreshData(context.holder(), score);
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
        Player player = context.holder();

        if (!nextLoot.disableStats()) {
            plugin.getStorageManager().getOnlineUser(player.getUniqueId()).ifPresent(
                    userData -> {
                        userData.statistics().addAmount(nextLoot.statisticKey().amountKey(), 1);
                        context.arg(ContextKeys.TOTAL_AMOUNT, userData.statistics().getAmount(nextLoot.statisticKey().amountKey()));
                        Optional.ofNullable(context.arg(ContextKeys.SIZE)).ifPresent(size -> {
                            float max = Math.max(0, userData.statistics().getMaxSize(nextLoot.statisticKey().sizeKey()));
                            context.arg(ContextKeys.RECORD, max);
                            context.arg(ContextKeys.RECORD_FORMATTED, String.format("%.2f", max));
                            if (userData.statistics().updateSize(nextLoot.statisticKey().sizeKey(), size)) {
                                plugin.getEventManager().trigger(context, id, MechanicType.LOOT, ActionTrigger.NEW_SIZE_RECORD);
                            }
                        });
                    }
            );
        }

        plugin.getEventManager().trigger(context, id, MechanicType.LOOT, ActionTrigger.SUCCESS);
        player.setStatistic(Statistic.FISH_CAUGHT, player.getStatistic(Statistic.FISH_CAUGHT) + 1);
    }
}
