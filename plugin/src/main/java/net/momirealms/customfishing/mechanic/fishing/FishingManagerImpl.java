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
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
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
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.WeightUtils;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.util.ItemUtils;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FishingManagerImpl implements Listener, FishingManager {

    private final CustomFishingPluginImpl plugin;
    private final ConcurrentHashMap<UUID, FishHook> hookCacheMap;
    private final ConcurrentHashMap<UUID, HookCheckTimerTask> hookCheckMap;
    private final ConcurrentHashMap<UUID, TempFishingState> tempFishingStateMap;
    private final ConcurrentHashMap<UUID, GamingPlayer> gamingPlayerMap;
    private final ConcurrentHashMap<UUID, Pair<ItemStack, Integer>> vanillaLootMap;

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
        final Player player = event.getPlayer();
        this.removeHook(event.getPlayer().getUniqueId());
        this.removeTempFishingState(player);
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

    /**
     * Removes a fishing hook entity associated with a given UUID.
     *
     * @param uuid The UUID of the fishing hook entity to be removed.
     * @return {@code true} if the fishing hook was successfully removed, {@code false} otherwise.
     */
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

    /**
     * Retrieves a FishHook object associated with the provided player's UUID
     *
     * @param uuid The UUID of the player
     * @return fishhook entity, null if not exists
     */
    @Override
    @Nullable
    public FishHook getHook(UUID uuid) {
        FishHook fishHook = hookCacheMap.get(uuid);
        if (fishHook != null) {
            if (!fishHook.isValid()) {
                hookCacheMap.remove(uuid);
                return null;
            } else {
                return fishHook;
            }
        }
        return null;
    }

    /**
     * Selects the appropriate fishing state based on the provided PlayerFishEvent and triggers the corresponding action.
     *
     * @param event The PlayerFishEvent that represents the fishing action.
     */
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

    /**
     * Handle the event when the fishing hook lands on the ground.
     *
     * @param event The PlayerFishEvent that occurred.
     */
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
                    ItemUtils.decreaseDurability(player, itemStack, 2, true);
                }
            }
        }
    }

    /**
     * Handle the event when a player casts a fishing rod.
     *
     * @param event The PlayerFishEvent that occurred.
     */
    public void onCastRod(PlayerFishEvent event) {
        var player = event.getPlayer();
        var fishingPreparation = new FishingPreparation(player, plugin);
        if (!fishingPreparation.canFish()) {
            event.setCancelled(true);
            return;
        }
        // Check mechanic requirements
        if (!RequirementManager.isRequirementMet(
                fishingPreparation, RequirementManagerImpl.mechanicRequirements
        )) {
            removeTempFishingState(player);
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
//        fishHook.setMaxWaitTime(Math.max(100, (int) (fishHook.getMaxWaitTime() * initialEffect.getHookTimeModifier())));
//        fishHook.setMinWaitTime(Math.max(100, (int) (fishHook.getMinWaitTime() * initialEffect.getHookTimeModifier())));
        fishHook.setWaitTime(Math.max(1, (int) (fishHook.getWaitTime() * initialEffect.getWaitTimeMultiplier() + initialEffect.getWaitTime())));
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

    /**
     * Handle the event when a player catches an entity.
     *
     * @param event The PlayerFishEvent that occurred.
     */
    private void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        Entity entity = event.getCaught();
        if ((entity instanceof ArmorStand armorStand)
                && armorStand.getPersistentDataContainer().get(
                Objects.requireNonNull(NamespacedKey.fromString("lavafishing", plugin)),
                PersistentDataType.STRING
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
                ItemUtils.decreaseDurability(player, itemStack, 5, true);
            }
        }
    }

    /**
     * Handle the event when a player catches a fish.
     *
     * @param event The PlayerFishEvent that occurred.
     */
    private void onCaughtFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        if (!(event.getCaught() instanceof Item item))
            return;

        // If player is playing the game
        GamingPlayer gamingPlayer = gamingPlayerMap.get(uuid);
        if (gamingPlayer != null) {
            if (gamingPlayer.onRightClick())
                event.setCancelled(true);
            return;
        }

        // If player is not playing the game
        var temp = this.getTempFishingState(uuid);
        if (temp != null) {
            var loot = temp.getLoot();
            if (loot.getID().equals("vanilla")) {
                // put vanilla loot in map
                this.vanillaLootMap.put(uuid, Pair.of(item.getItemStack(), event.getExpToDrop()));
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
    }

    /**
     * Handle the event when a player receives a bite on their fishing hook.
     *
     * @param event The PlayerFishEvent that occurred.
     */
    private void onBite(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // If player is already in game
        // then ignore the event
        GamingPlayer gamingPlayer = getGamingPlayer(uuid);
        if (gamingPlayer != null) {
            return;
        }

        // If the loot's game is instant
        TempFishingState temp = getTempFishingState(uuid);
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

    /**
     * Handle the event when a player reels in their fishing line.
     *
     * @param event The PlayerFishEvent that occurred.
     */
    private void onReelIn(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        // If player is in game
        GamingPlayer gamingPlayer = getGamingPlayer(uuid);
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

            var temp = getTempFishingState(uuid);
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

    /**
     * Removes the temporary fishing state associated with a player.
     *
     * @param player The player whose temporary fishing state should be removed.
     */
    @Override
    public TempFishingState removeTempFishingState(Player player) {
        return this.tempFishingStateMap.remove(player.getUniqueId());
    }

    /**
     * Processes the game result for a gaming player
     *
     * @param gamingPlayer The gaming player whose game result should be processed.
     */
    @Override
    public void processGameResult(GamingPlayer gamingPlayer) {
        final Player player = gamingPlayer.getPlayer();
        final UUID uuid = player.getUniqueId();
        FishHook fishHook = hookCacheMap.remove(uuid);
        if (fishHook == null) {
            LogUtils.warn("Unexpected situation: Can't get player's fish hook when processing game results.");
            return;
        }
        TempFishingState tempFishingState = removeTempFishingState(player);
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
                    ItemUtils.decreaseHookDurability(rod, 1, false);
                    ItemUtils.decreaseDurability(player, rod, 1, true);
                }

            if (gamingPlayer.isSuccessful())
                success(tempFishingState, fishHook);
            else
                fail(tempFishingState, fishHook);

            fishHook.remove();

        }, fishHook.getLocation());
    }

    public void fail(TempFishingState state, FishHook hook) {
        var loot = state.getLoot();
        var fishingPreparation = state.getPreparation();

        if (loot.getID().equals("vanilla")) {
            Pair<ItemStack, Integer> pair = this.vanillaLootMap.remove(fishingPreparation.getPlayer().getUniqueId());
            if (pair != null) {
                fishingPreparation.insertArg("{nick}", "<lang:item.minecraft." + pair.left().getType().toString().toLowerCase() + ">");
                fishingPreparation.insertArg("{loot}", pair.left().getType().toString());
            }
        }

        // call event
        FishingResultEvent fishingResultEvent = new FishingResultEvent(
                fishingPreparation.getPlayer(),
                FishingResultEvent.Result.FAILURE,
                hook,
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

        ItemUtils.decreaseHookDurability(fishingPreparation.getRodItemStack(), 1, true);
    }

    /**
     * Handle the success of a fishing attempt, including spawning loot, calling events, and executing success actions.
     *
     * @param state The temporary fishing state containing information about the loot and effect.
     * @param hook The FishHook entity associated with the fishing attempt.
     */
    public void success(TempFishingState state, FishHook hook) {
        var loot = state.getLoot();
        var effect = state.getEffect();
        var fishingPreparation = state.getPreparation();
        var player = fishingPreparation.getPlayer();
        fishingPreparation.insertArg("{size-multiplier}", String.valueOf(effect.getSizeMultiplier()));
        fishingPreparation.insertArg("{size-fixed}", String.valueOf(effect.getSize()));
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
                hook,
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
                    Pair<ItemStack, Integer> pair = vanillaLootMap.remove(player.getUniqueId());
                    if (pair != null) {
                        fishingPreparation.insertArg("{nick}", "<lang:item.minecraft." + pair.left().getType().toString().toLowerCase() + ">");
                        for (int i = 0; i < amount; i++) {
                            plugin.getItemManager().dropItem(hook.getLocation(), player.getLocation(), pair.left().clone());
                            doSuccessActions(loot, effect, fishingPreparation, player);
                            if (pair.right() > 0) {
                                player.giveExp(pair.right(), true);
                                AdventureManagerImpl.getInstance().sendSound(player, Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < amount; i++) {
                        plugin.getItemManager().dropItem(player, hook.getLocation(), player.getLocation(), loot.getID(), fishingPreparation.getArgs());
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

    /**
     * Execute success-related actions after a successful fishing attempt, including updating competition data, triggering events and actions, and updating player statistics.
     *
     * @param loot The loot that was successfully caught.
     * @param effect The effect applied during fishing.
     * @param fishingPreparation The fishing preparation containing preparation data.
     * @param player The player who successfully caught the loot.
     */
    private void doSuccessActions(Loot loot, Effect effect, FishingPreparation fishingPreparation, Player player) {
        FishingCompetition competition = plugin.getCompetitionManager().getOnGoingCompetition();
        if (competition != null) {
            String scoreStr = fishingPreparation.getArg("{SCORE}");
            if (scoreStr != null) {
                competition.refreshData(player, Double.parseDouble(scoreStr));
            } else {
                switch (competition.getGoal()) {
                    case CATCH_AMOUNT -> {
                        fishingPreparation.insertArg("{score}", "1.00");
                        competition.refreshData(player, 1);
                    }
                    case MAX_SIZE, TOTAL_SIZE -> {
                        String size = fishingPreparation.getArg("{size}");
                        if (size != null) {
                            double score = Double.parseDouble(size);
                            fishingPreparation.insertArg("{score}", String.format("%.2f", score));
                            competition.refreshData(player, score);
                        } else {
                            fishingPreparation.insertArg("{score}", "0.00");
                        }
                    }
                    case TOTAL_SCORE -> {
                        double score = loot.getScore();
                        if (score != 0) {
                            double finalScore = score * effect.getScoreMultiplier() + effect.getScore();
                            fishingPreparation.insertArg("{score}", String.format("%.2f", finalScore));
                            competition.refreshData(player, finalScore);
                        } else {
                            fishingPreparation.insertArg("{score}", "0.00");
                        }
                    }
                }
            }
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

    /**
     * Starts a fishing game for the specified player with the given condition and effect.
     *
     * @param player    The player starting the fishing game.
     * @param condition The condition used to determine the game.
     * @param effect    The effect applied to the game.
     */
    @Override
    public void startFishingGame(Player player, Condition condition, Effect effect) {
        Map<String, Double> gameWithWeight = plugin.getGameManager().getGameWithWeight(condition);
        plugin.debug(gameWithWeight.toString());
        String random = WeightUtils.getRandom(gameWithWeight);
        Pair<BasicGameConfig, GameInstance> gamePair = plugin.getGameManager().getGameInstance(random);
        if (random == null) {
            LogUtils.warn("No game is available for player:" + player.getName() + " location:" + condition.getLocation());
            return;
        }
        if (gamePair == null) {
            LogUtils.warn(String.format("Game %s doesn't exist.", random));
            return;
        }
        plugin.debug("Game: " + random);
        startFishingGame(player, Objects.requireNonNull(gamePair.left().getGameSetting(effect)), gamePair.right());
    }

    /**
     * Starts a fishing game for the specified player with the given settings and game instance.
     *
     * @param player       The player starting the fishing game.
     * @param settings     The game settings for the fishing game.
     * @param gameInstance The instance of the fishing game to start.
     */
    @Override
    public void startFishingGame(Player player, GameSettings settings, GameInstance gameInstance) {
        plugin.debug("Difficulty:" + settings.getDifficulty());
        plugin.debug("Time:" + settings.getTime());
        FishHook hook = getHook(player.getUniqueId());
        if (hook != null) {
            this.gamingPlayerMap.put(player.getUniqueId(), gameInstance.start(player, hook, settings));
        } else {
            LogUtils.warn("It seems that player " + player.getName() + " is not fishing. Fishing game failed to start.");
        }
    }

    /**
     * Checks if a player with the given UUID has cast their fishing hook.
     *
     * @param uuid The UUID of the player to check.
     * @return {@code true} if the player has cast their fishing hook, {@code false} otherwise.
     */
    @Override
    public boolean hasPlayerCastHook(UUID uuid) {
        FishHook fishHook = hookCacheMap.get(uuid);
        if (fishHook == null) return false;
        if (!fishHook.isValid()) {
            hookCacheMap.remove(uuid);
            return false;
        }
        return true;
    }

    /**
     * Sets the temporary fishing state for a player.
     *
     * @param player            The player for whom to set the temporary fishing state.
     * @param tempFishingState  The temporary fishing state to set for the player.
     */
    @Override
    public void setTempFishingState(Player player, TempFishingState tempFishingState) {
        tempFishingStateMap.put(player.getUniqueId(), tempFishingState);
    }

    public void removeHookCheckTask(Player player) {
        hookCheckMap.remove(player.getUniqueId());
    }

    /**
     * Gets the {@link GamingPlayer} object associated with the given UUID.
     *
     * @param uuid The UUID of the player.
     * @return The {@link GamingPlayer} object if found, or {@code null} if not found.
     */
    @Override
    @Nullable
    public GamingPlayer getGamingPlayer(UUID uuid) {
        return gamingPlayerMap.get(uuid);
    }

    /**
     * Gets the {@link TempFishingState} object associated with the given UUID.
     *
     * @param uuid The UUID of the player.
     * @return The {@link TempFishingState} object if found, or {@code null} if not found.
     */
    @Override
    @Nullable
    public TempFishingState getTempFishingState(UUID uuid) {
        return tempFishingStateMap.get(uuid);
    }
}
