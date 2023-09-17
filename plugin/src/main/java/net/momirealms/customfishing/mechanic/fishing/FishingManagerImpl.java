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

package net.momirealms.customfishing.mechanic.fishing;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.event.FishingResultEvent;
import net.momirealms.customfishing.api.event.LavaFishingEvent;
import net.momirealms.customfishing.api.event.RodCastEvent;
import net.momirealms.customfishing.api.manager.FishingManager;
import net.momirealms.customfishing.api.manager.RequirementManager;
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.competition.FishingCompetition;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import net.momirealms.customfishing.api.mechanic.game.BasicGameConfig;
import net.momirealms.customfishing.api.mechanic.game.GameInstance;
import net.momirealms.customfishing.api.mechanic.game.GameSettings;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.api.mechanic.loot.WeightModifier;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.WeightUtils;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.util.ItemUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingManagerImpl implements Listener, FishingManager {

    private final CustomFishingPluginImpl plugin;
    private final ConcurrentHashMap<UUID, FishHook> hookCacheMap;
    private final ConcurrentHashMap<UUID, HookCheckTimerTask> hookCheckMap;
    private final ConcurrentHashMap<UUID, TempFishingState> tempFishingStateMap;
    private final ConcurrentHashMap<UUID, GamingPlayer> gamingPlayerMap;
    private final ConcurrentHashMap<UUID, ItemStack> vanillaLootMap;

    public FishingManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.hookCacheMap = new ConcurrentHashMap<>();
        this.tempFishingStateMap = new ConcurrentHashMap<>();
        this.gamingPlayerMap = new ConcurrentHashMap<>();
        this.hookCheckMap = new ConcurrentHashMap<>();
        this.vanillaLootMap = new ConcurrentHashMap<>();
    }

    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        for (FishHook hook : hookCacheMap.values()) {
            hook.remove();
        }
        for (HookCheckTimerTask task : hookCheckMap.values()) {
            task.destroy();
        }
        for (GamingPlayer gamingPlayer : gamingPlayerMap.values()) {
            gamingPlayer.cancel();
        }
        this.hookCacheMap.clear();
        this.tempFishingStateMap.clear();
        this.gamingPlayerMap.clear();
        this.hookCheckMap.clear();
    }

    public void disable() {
        unload();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishMONITOR(PlayerFishEvent event) {
        if (CFConfig.eventPriority != EventPriority.MONITOR) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishHIGHEST(PlayerFishEvent event) {
        if (CFConfig.eventPriority != EventPriority.HIGHEST) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFishHIGH(PlayerFishEvent event) {
        if (CFConfig.eventPriority != EventPriority.HIGH) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFishNORMAL(PlayerFishEvent event) {
        if (CFConfig.eventPriority != EventPriority.NORMAL) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFishLOW(PlayerFishEvent event) {
        if (CFConfig.eventPriority != EventPriority.LOW) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFishLOWEST(PlayerFishEvent event) {
        if (CFConfig.eventPriority != EventPriority.LOWEST) return;
        this.selectState(event);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.removeHook(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onSwapHand())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onJump())
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onChat(event.getMessage()))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY)
            return;
        if (event.getAction() != org.bukkit.event.block.Action.LEFT_CLICK_AIR)
            return;
        GamingPlayer gamingPlayer = gamingPlayerMap.get(event.getPlayer().getUniqueId());
        if (gamingPlayer != null) {
            if (gamingPlayer.onLeftClick()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public boolean removeHook(UUID uuid) {
        FishHook hook = hookCacheMap.remove(uuid);
        if (hook != null && hook.isValid()) {
            plugin.getScheduler().runTaskSync(hook::remove, hook.getLocation());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<FishHook> getHook(UUID uuid) {
        return Optional.ofNullable(hookCacheMap.get(uuid));
    }

    public void selectState(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        switch (event.getState()) {
            case FISHING -> onCastRod(event);
            case REEL_IN -> onReelIn(event);
            case CAUGHT_ENTITY -> onCaughtEntity(event);
            case CAUGHT_FISH -> onCaughtFish(event);
            case BITE -> onBite(event);
            case IN_GROUND -> onInGround(event);
        }
    }

    private void onInGround(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        FishHook hook = event.getHook();
        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.FISHING_ROD) itemStack = player.getInventory().getItemInOffHand();
            if (itemStack.getType() == Material.FISHING_ROD) {
                NBTItem nbtItem = new NBTItem(itemStack);
                NBTCompound compound = nbtItem.getCompound("CustomFishing");
                if (compound != null && compound.hasTag("max_dur")) {
                    event.setCancelled(true);
                    hook.remove();
                    ItemUtils.loseDurability(itemStack, 2);
                }
            }
        }
    }

    public void onCastRod(PlayerFishEvent event) {
        var player = event.getPlayer();
        var fishingPreparation = new FishingPreparation(player, plugin);
        if (!fishingPreparation.canFish()) {
            event.setCancelled(true);
            return;
        }
        // Check mechanic requirements
        if (!RequirementManager.isRequirementsMet(
                RequirementManagerImpl.mechanicRequirements,
                fishingPreparation
        )) {
            return;
        }
        // Merge rod/bait/util effects
        FishingEffect initialEffect = plugin.getEffectManager().getInitialEffect();
        fishingPreparation.mergeEffect(initialEffect);

        // Merge totem effects
        EffectCarrier totemEffect = plugin.getTotemManager().getTotemEffect(player.getLocation());
        if (totemEffect != null)
            for (EffectModifier modifier : totemEffect.getEffectModifiers()) {
                modifier.modify(initialEffect, fishingPreparation);
            }

        // Call custom event
        RodCastEvent rodCastEvent = new RodCastEvent(event, fishingPreparation, initialEffect);
        Bukkit.getPluginManager().callEvent(rodCastEvent);
        if (rodCastEvent.isCancelled()) {
            return;
        }

        // Store fishhook entity and apply the effects
        final FishHook fishHook = event.getHook();
        this.hookCacheMap.put(player.getUniqueId(), fishHook);
        fishHook.setMaxWaitTime((int) (fishHook.getMaxWaitTime() * initialEffect.getHookTimeModifier()));
        fishHook.setMinWaitTime((int) (fishHook.getMinWaitTime() * initialEffect.getHookTimeModifier()));
        // Reduce amount & Send animation
        var baitItem = fishingPreparation.getBaitItemStack();
        if (baitItem != null) {
            ItemStack cloned = baitItem.clone();
            cloned.setAmount(1);
            new BaitAnimationTask(plugin, player, fishHook, cloned);
            baitItem.setAmount(baitItem.getAmount() - 1);
        }
        // Arrange hook check task
        this.hookCheckMap.put(player.getUniqueId(), new HookCheckTimerTask(this, fishHook, fishingPreparation, initialEffect));
        // trigger actions
        fishingPreparation.triggerActions(ActionTrigger.CAST);
    }

    private void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        Entity entity = event.getCaught();
        if ((entity instanceof ArmorStand armorStand)
                && armorStand.getPersistentDataContainer().get(
                Objects.requireNonNull(NamespacedKey.fromString("lavafishing", plugin)),
                PersistentDataType.BOOLEAN
        ) != null) {
            // The hook is hooked into the temp entity
            // This might be called both not in game and in game
            LavaFishingEvent lavaFishingEvent = new LavaFishingEvent(player, LavaFishingEvent.State.REEL_IN, event.getHook());
            Bukkit.getPluginManager().callEvent(lavaFishingEvent);
            if (lavaFishingEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            GamingPlayer gamingPlayer = gamingPlayerMap.get(uuid);
            if (gamingPlayer != null) {
                // in game
                if (gamingPlayer.onRightClick())
                    event.setCancelled(true);
            } else {
                // not in game
                HookCheckTimerTask task = hookCheckMap.get(uuid);
                if (task != null)
                    task.destroy();
                else
                    // should not reach this but in case
                    entity.remove();
            }
            return;
        }

        if (player.getGameMode() != GameMode.CREATIVE) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (itemStack.getType() != Material.FISHING_ROD) itemStack = player.getInventory().getItemInOffHand();
            NBTItem nbtItem = new NBTItem(itemStack);
            NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
            if (nbtCompound != null && nbtCompound.hasTag("max_dur")) {
                event.getHook().remove();
                event.setCancelled(true);
                ItemUtils.loseDurability(itemStack, 5);
            }
        }
    }

    private void onCaughtFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!(event.getCaught() instanceof Item item)) return;

        // If player is playing the game
        GamingPlayer gamingPlayer = gamingPlayerMap.get(uuid);
        if (gamingPlayer != null) {
            if (gamingPlayer.onRightClick()) {
                event.setCancelled(true);
            }
            return;
        }

        // If player is not playing the game
        var temp = this.tempFishingStateMap.get(uuid);
        if (temp != null ) {
            var loot = temp.getLoot();
            if (loot.getID().equals("vanilla")) {
                // put vanilla loot in map
                this.vanillaLootMap.put(uuid, item.getItemStack());
            }
            loot.triggerActions(ActionTrigger.HOOK, temp.getPreparation());
            temp.getPreparation().triggerActions(ActionTrigger.HOOK);
            if (!loot.disableGame()) {
                // start the game if the loot has a game
                event.setCancelled(true);
                startFishingGame(player, temp.getPreparation(), temp.getEffect());
            } else {
                // If the game is disabled, then do success actions
                success(temp, event.getHook());
                // Cancel the event because loots can be multiple and unique
                event.setCancelled(true);
                event.getHook().remove();
            }
            return;
        }

        if (!CFConfig.vanillaMechanicIfNoLoot) {
            event.setCancelled(true);
            event.getHook().remove();
        }
    }

    private void onBite(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // If player is already in game
        // then ignore the event
        GamingPlayer gamingPlayer = gamingPlayerMap.get(uuid);
        if (gamingPlayer != null) {
            return;
        }

        // If the loot's game is instant
        TempFishingState temp = tempFishingStateMap.get(uuid);
        if (temp != null) {
            var loot = temp.getLoot();

            loot.triggerActions(ActionTrigger.BITE, temp.getPreparation());
            temp.getPreparation().triggerActions(ActionTrigger.BITE);

            if (loot.instanceGame() && !loot.disableGame()) {
                loot.triggerActions(ActionTrigger.HOOK, temp.getPreparation());
                temp.getPreparation().triggerActions(ActionTrigger.HOOK);
                startFishingGame(player, temp.getPreparation(), temp.getEffect());
            }
        }
    }

    private void onReelIn(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // If player is in game
        GamingPlayer gamingPlayer = gamingPlayerMap.get(uuid);
        if (gamingPlayer != null) {
            if (gamingPlayer.onRightClick())
                event.setCancelled(true);
            return;
        }

        // If player is lava fishing
        HookCheckTimerTask hookTask = hookCheckMap.get(uuid);
        if (hookTask != null && hookTask.isFishHooked()) {
            LavaFishingEvent lavaFishingEvent = new LavaFishingEvent(player, LavaFishingEvent.State.CAUGHT_FISH, event.getHook());
            Bukkit.getPluginManager().callEvent(lavaFishingEvent);
            if (lavaFishingEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            var temp = this.tempFishingStateMap.get(uuid);
            if (temp != null ) {
                Loot loot = temp.getLoot();
                loot.triggerActions(ActionTrigger.HOOK, temp.getPreparation());
                temp.getPreparation().triggerActions(ActionTrigger.HOOK);
                if (!loot.disableGame()) {
                    event.setCancelled(true);
                    startFishingGame(player, temp.getPreparation(), temp.getEffect());
                } else {
                    success(temp, event.getHook());
                }
            }
            return;
        }
    }

    @Override
    public void removeTempFishingState(Player player) {
        this.tempFishingStateMap.remove(player.getUniqueId());
    }

    @Override
    public void processGameResult(GamingPlayer gamingPlayer) {
        final Player player = gamingPlayer.getPlayer();
        final UUID uuid = player.getUniqueId();
        FishHook fishHook = hookCacheMap.remove(uuid);
        if (fishHook == null) {
            LogUtils.warn("Unexpected situation: Can't get player's fish hook when processing game results.");
            return;
        }
        TempFishingState tempFishingState = tempFishingStateMap.remove(uuid);
        if (tempFishingState == null) {
            LogUtils.warn("Unexpected situation: Can't get player's fishing state when processing game results.");
            return;
        }
        Effect bonus = gamingPlayer.getEffectReward();
        if (bonus != null)
            tempFishingState.getEffect().merge(bonus);

        gamingPlayer.cancel();
        gamingPlayerMap.remove(uuid);
        plugin.getScheduler().runTaskSync(() -> {

            if (player.getGameMode() != GameMode.CREATIVE)
                outer: {
                    ItemStack rod = tempFishingState.getPreparation().getRodItemStack();
                    PlayerItemDamageEvent damageEvent = new PlayerItemDamageEvent(player, rod, 1, 1);
                    Bukkit.getPluginManager().callEvent(damageEvent);
                    if (damageEvent.isCancelled()) {
                        break outer;
                    }
                    ItemUtils.loseDurability(rod, 1);
                }

            fishHook.remove();

            if (gamingPlayer.isSuccessful())
                success(tempFishingState, fishHook);
            else
                fail(tempFishingState, fishHook);
        }, fishHook.getLocation());
    }

    public void fail(TempFishingState state, FishHook hook) {
        var loot = state.getLoot();
        var fishingPreparation = state.getPreparation();

        if (loot.getID().equals("vanilla")) {
            ItemStack itemStack = this.vanillaLootMap.remove(fishingPreparation.getPlayer().getUniqueId());
            if (itemStack != null) {
                fishingPreparation.insertArg("{nick}", "<lang:item.minecraft." + itemStack.getType().toString().toLowerCase() + ">");
                fishingPreparation.insertArg("{loot}", itemStack.getType().toString());
            }
        }

        // call event
        FishingResultEvent fishingResultEvent = new FishingResultEvent(
                fishingPreparation.getPlayer(),
                FishingResultEvent.Result.FAILURE,
                loot,
                fishingPreparation.getArgs()
        );
        Bukkit.getPluginManager().callEvent(fishingResultEvent);
        if (fishingResultEvent.isCancelled()) {
            return;
        }

        GlobalSettings.triggerLootActions(ActionTrigger.FAILURE, fishingPreparation);
        loot.triggerActions(ActionTrigger.FAILURE, fishingPreparation);
        fishingPreparation.triggerActions(ActionTrigger.FAILURE);
    }

    public void success(TempFishingState state, FishHook hook) {
        var loot = state.getLoot();
        var effect = state.getEffect();
        var fishingPreparation = state.getPreparation();
        var player = fishingPreparation.getPlayer();
        fishingPreparation.insertArg("{size-multiplier}", String.format("%.2f", effect.getSizeMultiplier()));
        int amount;
        if (loot.getType() == LootType.ITEM) {
            amount = (int) effect.getMultipleLootChance();
            amount += Math.random() < (effect.getMultipleLootChance() - amount) ? 2 : 1;
        } else {
            amount = 1;
        }
        fishingPreparation.insertArg("{amount}", String.valueOf(amount));

        // call event
        FishingResultEvent fishingResultEvent = new FishingResultEvent(
                player,
                FishingResultEvent.Result.SUCCESS,
                loot,
                fishingPreparation.getArgs()
        );
        Bukkit.getPluginManager().callEvent(fishingResultEvent);
        if (fishingResultEvent.isCancelled()) {
            return;
        }

        switch (loot.getType()) {
            case ITEM -> {

                // build the items for multiple times instead of using setAmount() to make sure that each item is unique
                if (loot.getID().equals("vanilla")) {
                    ItemStack itemStack = vanillaLootMap.remove(player.getUniqueId());
                    if (itemStack != null) {
                        fishingPreparation.insertArg("{loot}", "<lang:item.minecraft." + itemStack.getType().toString().toLowerCase() + ">");
                        for (int i = 0; i < amount; i++) {
                            plugin.getItemManager().dropItem(hook.getLocation(), player.getLocation(), itemStack.clone());
                            doSuccessActions(loot, effect, fishingPreparation, player);
                        }
                    }
                } else {
                    for (int i = 0; i < amount; i++) {
                        plugin.getItemManager().dropItem(player, hook.getLocation(), player.getLocation(), loot, fishingPreparation.getArgs());
                        doSuccessActions(loot, effect, fishingPreparation, player);
                    }
                }
                return;
            }
            case ENTITY -> {
                plugin.getEntityManager().summonEntity(hook.getLocation(), player.getLocation(), loot);
            }
            case BLOCK -> {
                plugin.getBlockManager().summonBlock(player, hook.getLocation(), player.getLocation(), loot);
            }
        }
        doSuccessActions(loot, effect, fishingPreparation, player);
    }

    private void doSuccessActions(Loot loot, Effect effect, FishingPreparation fishingPreparation, Player player) {
        FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
        if (competition != null) {
            switch (competition.getGoal()) {
                case CATCH_AMOUNT -> {
                    fishingPreparation.insertArg("{score}", "1");
                    competition.refreshData(player, 1);
                }
                case MAX_SIZE, TOTAL_SIZE -> {
                    String size = fishingPreparation.getArg("{size}");
                    if (size != null) {
                        fishingPreparation.insertArg("{score}", size);
                        competition.refreshData(player, Double.parseDouble(size));
                    } else {
                        fishingPreparation.insertArg("{score}", "0");
                    }
                }
                case TOTAL_SCORE -> {
                    double score = loot.getScore();
                    if (score != 0) {
                        fishingPreparation.insertArg("{score}", String.format("%.2f", score * effect.getScoreMultiplier()));
                        competition.refreshData(player, score * effect.getScoreMultiplier());
                    } else {
                        fishingPreparation.insertArg("{score}", "0");
                    }
                }
            }
        } else {
            fishingPreparation.insertArg("{score}","-1");
        }

        // events and actions
        GlobalSettings.triggerLootActions(ActionTrigger.SUCCESS, fishingPreparation);
        loot.triggerActions(ActionTrigger.SUCCESS, fishingPreparation);
        fishingPreparation.triggerActions(ActionTrigger.SUCCESS);

        player.setStatistic(
                Statistic.FISH_CAUGHT,
                player.getStatistic(Statistic.FISH_CAUGHT) + 1
        );

        if (!loot.disableStats())
            Optional.ofNullable(
                    plugin.getStatisticsManager()
                          .getStatistics(player.getUniqueId())
            ).ifPresent(it -> it.addLootAmount(loot, fishingPreparation, 1));
    }

    @Override
    public Collection<String> getPossibleLootKeys (Condition condition) {
        return plugin.getRequirementManager().getLootWithWeight(condition).keySet();
    }

    @NotNull
    @Override
    public Map<String, Double> getPossibleLootKeysWithWeight(Effect initialEffect, Condition condition) {
        Map<String, Double> lootWithWeight = plugin.getRequirementManager().getLootWithWeight(condition);

        Player player = condition.getPlayer();
        for (Pair<String, WeightModifier> pair : initialEffect.getWeightModifier()) {
            Double previous = lootWithWeight.get(pair.left());
            if (previous != null)
                lootWithWeight.put(pair.left(), pair.right().modify(player, previous));
        }
        for (Pair<String, WeightModifier> pair : initialEffect.getWeightModifierIgnored()) {
            double previous = lootWithWeight.getOrDefault(pair.left(), 0d);
            lootWithWeight.put(pair.left(), pair.right().modify(player, previous));
        }
        return lootWithWeight;
    }

    @Override
    @Nullable
    public Loot getNextLoot(Effect initialEffect, Condition condition) {
        String key = WeightUtils.getRandom(getPossibleLootKeysWithWeight(initialEffect, condition));
        Loot loot = plugin.getLootManager().getLoot(key);
        if (loot == null) {
            LogUtils.warn(String.format("Loot %s doesn't exist!", key));
            return null;
        }
        return loot;
    }

    @Override
    public void startFishingGame(Player player, Condition condition, Effect effect) {
        Map<String, Double> gameWithWeight = plugin.getRequirementManager().getGameWithWeight(condition);
        plugin.debug(gameWithWeight.toString());
        String random = WeightUtils.getRandom(gameWithWeight);
        Optional<Pair<BasicGameConfig, GameInstance>> gamePair = plugin.getGameManager().getGame(random);
        if (gamePair.isEmpty()) {
            LogUtils.warn(String.format("Game %s doesn't exist!", random));
            return;
        }
        plugin.debug("Game: " + random);
        startFishingGame(player, Objects.requireNonNull(gamePair.get().left().getGameSetting(effect)), gamePair.get().right());
    }

    @Override
    public void startFishingGame(Player player, GameSettings settings, GameInstance gameInstance) {
        plugin.debug("Difficulty:" + settings.getDifficulty());
        plugin.debug("Time:" + settings.getTime());
        Optional<FishHook> hook = getHook(player.getUniqueId());
        if (hook.isPresent()) {
            this.gamingPlayerMap.put(player.getUniqueId(), gameInstance.start(player, hook.get(), settings));
        } else {
            LogUtils.warn("It seems that player " + player.getName() + " is not fishing. Fishing game failed to start.");
        }
    }

    @Override
    public void setTempFishingState(Player player, TempFishingState tempFishingState) {
        tempFishingStateMap.put(player.getUniqueId(), tempFishingState);
    }

    @Override
    public void removeHookCheckTask(Player player) {
        hookCheckMap.remove(player.getUniqueId());
    }
}
