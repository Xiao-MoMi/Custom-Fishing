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

package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
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
import net.momirealms.customfishing.api.mechanic.game.Game;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
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
    private GamingPlayer gamingPlayer;

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
                        // to update some properties
                        mechanic.preStart();
                        Effect tempEffect = effect.copy();
                        for (EffectModifier modifier : gears.effectModifiers()) {
                            for (TriConsumer<Effect, Context<Player>, Integer> consumer : modifier.modifiers()) {
                                consumer.accept(tempEffect, context, 1);
                            }
                        }

                        context.arg(ContextKeys.OTHER_LOCATION, hook.getLocation());
                        context.arg(ContextKeys.OTHER_X, hook.getLocation().getBlockX());
                        context.arg(ContextKeys.OTHER_Y, hook.getLocation().getBlockY());
                        context.arg(ContextKeys.OTHER_Z, hook.getLocation().getBlockZ());

                        // get the next loot
                        Loot loot = plugin.getLootManager().getNextLoot(effect, context);
                        if (loot != null) {
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

    public void destroy() {
        if (task != null) task.cancel();
        if (hook.isValid()) hook.remove();
        if (hookMechanic != null) hookMechanic.destroy();
        if (gamingPlayer != null) gamingPlayer.destroy();
    }

    public Context<Player> getContext() {
        return context;
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
        if (isPlayingGame()) return;
        if (hookMechanic != null) {
            if (!hookMechanic.isHooked()) {
                gears.trigger(ActionTrigger.REEL, context);
                end();
            } else {
                if (nextLoot.disableGame() || RequirementManager.isSatisfied(context, ConfigManager.skipGameRequirements())) {
                    handleSuccessfulFishing();
                    end();
                } else {
                    gameStart();
                }
            }
        } else {
            gears.trigger(ActionTrigger.REEL, context);
            end();
        }
    }

    public void end() {
        plugin.getFishingManager().destroy(context.getHolder().getUniqueId());
    }

    public void onBite() {
        if (isPlayingGame()) return;
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.BITE);
        gears.trigger(ActionTrigger.BITE, context);
        if (RequirementManager.isSatisfied(context, ConfigManager.autoFishingRequirements())) {
            handleSuccessfulFishing();
            SparrowHeart.getInstance().swingHand(context.getHolder(), gears.getRodSlot());
            end();
            scheduleNextFishing();
            return;
        }
        if (nextLoot.instantGame()) {
            gameStart();
        }
    }

    public boolean isPlayingGame() {
        return gamingPlayer != null && gamingPlayer.isValid();
    }

    public void cancelCurrentGame() {
        if (gamingPlayer == null || !gamingPlayer.isValid()) {
            throw new RuntimeException("You can't call this method if the player is not playing the game");
        }
        gamingPlayer.cancel();
        gamingPlayer = null;
        if (hookMechanic != null) {
            hookMechanic.unfreeze(tempFinalEffect);
        }
    }

    public void gameStart() {
        if (isPlayingGame())
            return;
        Game nextGame = plugin.getGameManager().getNextGame(tempFinalEffect, context);
        if (nextGame != null) {
            plugin.debug("Next game: " + nextGame.id());
            gamingPlayer = nextGame.start(this, tempFinalEffect);
            if (this.hookMechanic != null) {
                this.hookMechanic.freeze();
            }
        } else {
            plugin.debug("Next game: " + "`null`");
            handleSuccessfulFishing();
            end();
        }
    }

    public Optional<GamingPlayer> getGamingPlayer() {
        return Optional.ofNullable(gamingPlayer);
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
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.ESCAPE);
        gears.trigger(ActionTrigger.ESCAPE, context);
    }

    public void onLure() {
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.LURE);
        gears.trigger(ActionTrigger.LURE, context);
    }

    public void handleFailedFishing() {

        // update the hook location
        context.arg(ContextKeys.OTHER_LOCATION, hook.getLocation());
        context.arg(ContextKeys.OTHER_X, hook.getLocation().getBlockX());
        context.arg(ContextKeys.OTHER_Y, hook.getLocation().getBlockY());
        context.arg(ContextKeys.OTHER_Z, hook.getLocation().getBlockZ());

        gears.trigger(ActionTrigger.FAILURE, context);
        plugin.getEventManager().trigger(context, nextLoot.id(), MechanicType.LOOT, ActionTrigger.FAILURE);
    }

    public void handleSuccessfulFishing() {

        // update the hook location
        context.arg(ContextKeys.OTHER_LOCATION, hook.getLocation());
        context.arg(ContextKeys.OTHER_X, hook.getLocation().getBlockX());
        context.arg(ContextKeys.OTHER_Y, hook.getLocation().getBlockY());
        context.arg(ContextKeys.OTHER_Z, hook.getLocation().getBlockZ());

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

                        FishingLootSpawnEvent spawnEvent = new FishingLootSpawnEvent(context, hook.getLocation(), nextLoot, item);
                        Bukkit.getPluginManager().callEvent(spawnEvent);
                        if (item != null && !spawnEvent.summonEntity())
                            item.remove();
                        if (spawnEvent.skipActions())
                            return;
                        if (item != null && item.isValid() && nextLoot.preventGrabbing()) {
                            item.getPersistentDataContainer().set(Objects.requireNonNull(NamespacedKey.fromString("owner", plugin.getBoostrap())), PersistentDataType.STRING, context.getHolder().getName());
                        }
                        doSuccessActions();
                    }, (long) ConfigManager.multipleLootSpawnDelay() * i, hook.getLocation());
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
