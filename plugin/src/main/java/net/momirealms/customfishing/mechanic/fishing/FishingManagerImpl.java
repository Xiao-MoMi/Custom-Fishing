package net.momirealms.customfishing.mechanic.fishing;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.event.LavaFishingEvent;
import net.momirealms.customfishing.api.event.RodCastEvent;
import net.momirealms.customfishing.api.manager.FishingManager;
import net.momirealms.customfishing.api.manager.RequirementManager;
import net.momirealms.customfishing.api.mechanic.TempFishingState;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.game.Game;
import net.momirealms.customfishing.api.mechanic.game.GameConfig;
import net.momirealms.customfishing.api.mechanic.game.GameSettings;
import net.momirealms.customfishing.api.mechanic.game.GamingPlayer;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.WeightUtils;
import net.momirealms.customfishing.mechanic.loot.LootManagerImpl;
import net.momirealms.customfishing.mechanic.requirement.RequirementManagerImpl;
import net.momirealms.customfishing.setting.Config;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Statistic;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
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
        if (Config.eventPriority != EventPriority.MONITOR) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishHIGHEST(PlayerFishEvent event) {
        if (Config.eventPriority != EventPriority.HIGHEST) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFishHIGH(PlayerFishEvent event) {
        if (Config.eventPriority != EventPriority.HIGH) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFishNORMAL(PlayerFishEvent event) {
        if (Config.eventPriority != EventPriority.NORMAL) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFishLOW(PlayerFishEvent event) {
        if (Config.eventPriority != EventPriority.LOW) return;
        this.selectState(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFishLOWEST(PlayerFishEvent event) {
        if (Config.eventPriority != EventPriority.LOWEST) return;
        this.selectState(event);
    }

    @EventHandler
    public void onPickUp(PlayerAttemptPickupItemEvent event) {
        if (event.isCancelled()) return;
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("owner")) return;
        if (!Objects.equals(nbtItem.getString("owner"), event.getPlayer().getName())) {
            event.setCancelled(true);
        } else {
            nbtItem.removeKey("owner");
            itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        }
    }

    @EventHandler
    public void onMove(InventoryPickupItemEvent event) {
        if (event.isCancelled()) return;
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("owner")) return;
        nbtItem.removeKey("owner");
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
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
        Effect initialEffect = plugin.getEffectManager().getInitialEffect();
        initialEffect
                .merge(fishingPreparation.getRodEffect())
                .merge(fishingPreparation.getBaitEffect());

        for (Effect utilEffect : fishingPreparation.getUtilEffects()) {
            initialEffect.merge(utilEffect);
        }

        // Apply enchants
        for (String enchant : plugin.getIntegrationManager().getEnchantments(fishingPreparation.getRodItemStack())) {
            Effect enchantEffect = plugin.getEffectManager().getEffect("enchant", enchant);
            if (enchantEffect != null && enchantEffect.canMerge(fishingPreparation)) {
                initialEffect.merge(enchantEffect);
            }
        }
        //TODO Apply totem effects

        // Call custom event
        RodCastEvent rodCastEvent = new RodCastEvent(event, initialEffect);
        Bukkit.getPluginManager().callEvent(rodCastEvent);
        if (rodCastEvent.isCancelled()) {
            return;
        }

        // Store fishhook entity and apply the effects
        final FishHook fishHook = event.getHook();
        this.hookCacheMap.put(player.getUniqueId(), fishHook);
        fishHook.setMaxWaitTime((int) (fishHook.getMaxWaitTime() * initialEffect.getTimeModifier()));
        fishHook.setMinWaitTime((int) (fishHook.getMinWaitTime() * initialEffect.getTimeModifier()));
        // Reduce amount & Send animation
        var baitItem = fishingPreparation.getBaitItemStack();
        if (baitItem != null) {
            if (Config.enableBaitAnimation) {
                ItemStack cloned = baitItem.clone();
                cloned.setAmount(1);
                new BaitAnimationTask(plugin, player, fishHook, cloned);
            }
            baitItem.setAmount(baitItem.getAmount() - 1);
        }
        // Arrange hook check task
        this.hookCheckMap.put(player.getUniqueId(), new HookCheckTimerTask(this, fishHook, fishingPreparation, initialEffect));
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
        }

        // TODO It's unsure if the hook would hook into other entities when playing a game
        // TODO But it should not affect the game result
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
            if (!loot.disableGame()) {
                // start the game if the loot has a game
                event.setCancelled(true);
                startFishingGame(player, temp.getLoot(), temp.getEffect());
            } else {
                // If the game is disabled, then do success actions
                success(temp, event.getHook());
                // Cancel the event because loots can be multiple and unique
                event.setCancelled(true);
                event.getHook().remove();
            }
            return;
        }

        if (!Config.vanillaMechanicIfNoLoot) {
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

            Action[] actions = loot.getActions(ActionTrigger.HOOK);
            if (actions != null)
                for (Action action : actions)
                    action.trigger(temp.getPreparation());

            if (loot.instanceGame() && !loot.disableGame()) {
                startFishingGame(player, loot, temp.getEffect());
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
                if (!temp.getLoot().disableGame()) {
                    event.setCancelled(true);
                    startFishingGame(player, temp.getLoot(), temp.getEffect());
                } else {
                    success(temp, event.getHook());
                }
            }
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

        if (gamingPlayer.isSuccessful())
            success(tempFishingState, fishHook);
        else
            fail(tempFishingState);

        gamingPlayer.cancel();
        gamingPlayerMap.remove(uuid);
        plugin.getScheduler().runTaskSync(fishHook::remove, fishHook.getLocation());
    }

    public void fail(TempFishingState state) {
        var loot = state.getLoot();
        var fishingPreparation = state.getPreparation();

        if (loot.getID().equals("vanilla")) {
            ItemStack itemStack = this.vanillaLootMap.remove(fishingPreparation.getPlayer().getUniqueId());
            if (itemStack != null) {
                fishingPreparation.insertArg("loot", "<lang:item.minecraft." + itemStack.getType().toString().toLowerCase() + ">");
            }
        }

        Action[] globalActions = LootManagerImpl.globalLootProperties.getActions(ActionTrigger.FAILURE);
        if (globalActions != null)
            for (Action action : globalActions)
                action.trigger(fishingPreparation);

        Action[] actions = loot.getActions(ActionTrigger.FAILURE);
        if (actions != null)
            for (Action action : actions)
                action.trigger(fishingPreparation);
    }

    public void success(TempFishingState state, FishHook hook) {
        var loot = state.getLoot();
        var effect = state.getEffect();
        var fishingPreparation = state.getPreparation();
        var player = fishingPreparation.getPlayer();

        fishingPreparation.insertArg("{score}", String.format("%.2f", loot.getScore() * effect.getScoreMultiplier()));
        fishingPreparation.insertArg("{size-multiplier}", String.format("%.2f", effect.getSizeMultiplier()));
        fishingPreparation.insertArg("{x}", String.valueOf(hook.getLocation().getBlockX()));
        fishingPreparation.insertArg("{y}", String.valueOf(hook.getLocation().getBlockY()));
        fishingPreparation.insertArg("{z}", String.valueOf(hook.getLocation().getBlockZ()));
        fishingPreparation.insertArg("{loot}", loot.getID());
        fishingPreparation.insertArg("{nick}", loot.getNick());

        plugin.getScheduler().runTaskSync(() -> {
            switch (loot.getType()) {
                case LOOT -> {
                    int amount = (int) effect.getMultipleLootChance();
                    amount += Math.random() < (effect.getMultipleLootChance() - amount) ? 2 : 1;
                    fishingPreparation.insertArg("{amount}", String.valueOf(amount));
                    // build the items for multiple times instead of using setAmount() to make sure that each item is unique
                    if (loot.getID().equals("vanilla")) {
                        ItemStack itemStack = vanillaLootMap.remove(player.getUniqueId());
                        if (itemStack != null) {
                            fishingPreparation.insertArg("{loot}", "<lang:item.minecraft." + itemStack.getType().toString().toLowerCase() + ">");
                            for (int i = 0; i < amount; i++) {
                                plugin.getItemManager().dropItem(hook.getLocation(), player.getLocation(), itemStack.clone());
                                doSuccessActions(loot, fishingPreparation, player);
                            }
                        }
                    } else {
                        for (int i = 0; i < amount; i++) {
                            plugin.getItemManager().dropItem(player, hook.getLocation(), player.getLocation(), loot, fishingPreparation.getArgs());
                            doSuccessActions(loot, fishingPreparation, player);
                        }
                    }
                    return;
                }
                case MOB -> plugin.getMobManager().summonMob(hook.getLocation(), player.getLocation(), loot);
                case BLOCK -> plugin.getBlockManager().summonBlock(player, hook.getLocation(), player.getLocation(), loot);
            }
            doSuccessActions(loot, fishingPreparation, player);
        }, hook.getLocation());
    }

    private void doSuccessActions(Loot loot, FishingPreparation fishingPreparation, Player player) {
        Action[] globalActions = LootManagerImpl.globalLootProperties.getActions(ActionTrigger.SUCCESS);
        if (globalActions != null)
            for (Action action : globalActions)
                action.trigger(fishingPreparation);

        Action[] actions = loot.getActions(ActionTrigger.SUCCESS);
        if (actions != null)
            for (Action action : actions)
                action.trigger(fishingPreparation);

        player.setStatistic(
                Statistic.FISH_CAUGHT,
                player.getStatistic(Statistic.FISH_CAUGHT) + 1
        );
    }

    @Nullable
    public Loot getNextLoot(Effect initialEffect, FishingPreparation fishingPreparation) {
        HashMap<String, Double> lootWithWeight = plugin.getRequirementManager().getLootWithWeight(fishingPreparation);
        if (lootWithWeight.size() == 0) {
            LogUtils.warn(String.format("No Loot found at %s for Player %s!", fishingPreparation.getPlayer().getLocation(), fishingPreparation.getPlayer().getName()));
            return null;
        }

        for (Pair<String, Modifier> pair : initialEffect.getLootWeightModifier()) {
            double previous = lootWithWeight.getOrDefault(pair.left(), 0d);
            lootWithWeight.put(pair.left(), pair.right().modify(previous));
        }

        String key = WeightUtils.getRandom(lootWithWeight);
        Loot loot = plugin.getLootManager().getLoot(key);
        if (loot == null) {
            LogUtils.warn(String.format("Loot %s doesn't exist!", key));
            return null;
        }
        return loot;
    }

    @Override
    public void startFishingGame(Player player, Loot loot, Effect effect) {
        GameConfig gameConfig = loot.getGameConfig();
        if (gameConfig == null) {
            gameConfig = plugin.getGameManager().getRandomGameConfig();
        }
        var gamePair = gameConfig.getRandomGame(effect);
        if (gamePair == null) {
            return;
        }
        startFishingGame(player, gamePair.right(), gamePair.left());
    }

    @Override
    public void startFishingGame(Player player, GameSettings settings, Game game) {
        Optional<FishHook> hook = getHook(player.getUniqueId());
        if (hook.isPresent()) {
            this.gamingPlayerMap.put(player.getUniqueId(), game.start(player, hook.get(), settings));
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
