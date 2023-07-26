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

package net.momirealms.customfishing.manager;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.CustomFishingAPI;
import net.momirealms.customfishing.api.event.*;
import net.momirealms.customfishing.fishing.Effect;
import net.momirealms.customfishing.fishing.*;
import net.momirealms.customfishing.fishing.action.Action;
import net.momirealms.customfishing.fishing.action.VanillaXPImpl;
import net.momirealms.customfishing.fishing.bar.FishingBar;
import net.momirealms.customfishing.fishing.bar.ModeOneBar;
import net.momirealms.customfishing.fishing.bar.ModeThreeBar;
import net.momirealms.customfishing.fishing.bar.ModeTwoBar;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.fishing.competition.CompetitionGoal;
import net.momirealms.customfishing.fishing.loot.DroppedItem;
import net.momirealms.customfishing.fishing.loot.LootImpl;
import net.momirealms.customfishing.fishing.loot.Mob;
import net.momirealms.customfishing.fishing.mode.FishingGame;
import net.momirealms.customfishing.fishing.mode.ModeOneGame;
import net.momirealms.customfishing.fishing.mode.ModeThreeGame;
import net.momirealms.customfishing.fishing.mode.ModeTwoGame;
import net.momirealms.customfishing.fishing.requirements.RequirementInterface;
import net.momirealms.customfishing.fishing.totem.ActivatedTotem;
import net.momirealms.customfishing.fishing.totem.TotemConfig;
import net.momirealms.customfishing.integration.MobInterface;
import net.momirealms.customfishing.integration.item.McMMOTreasure;
import net.momirealms.customfishing.listener.*;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.Pair;
import net.momirealms.customfishing.object.SimpleLocation;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.FakeItemUtils;
import net.momirealms.customfishing.util.ItemStackUtils;
import net.momirealms.customfishing.util.LocationUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FishingManager extends Function {

    private final CustomFishing plugin;
    private final PlayerFishListener playerFishListener;
    private final InteractListener interactListener;
    private final ConsumeItemListener consumeItemListener;
    private PickUpListener pickUpListener;
    private JobsRebornXPListener jobsRebornXPListener;
    private final JoinQuitListener joinQuitListener;
    private final BreakBlockListener breakBlockListener;
    private final HashMap<UUID, Long> coolDown;
    private final HashMap<UUID, FishHook> hooks;
    private final HashMap<UUID, LootImpl> nextLoot;
    private final HashMap<UUID, Effect> nextEffect;
    private final HashMap<UUID, VanillaLoot> vanillaLoot;
    private final ConcurrentHashMap<UUID, FishingGame> fishingPlayerMap;
    private final ConcurrentHashMap<SimpleLocation, ActivatedTotem> activeTotemMap;
    private final ConcurrentHashMap<SimpleLocation, SimpleLocation> breakDetectionMap;
    private final ConcurrentHashMap<UUID, BobberCheckTask> hookCheckTaskMap;

    public FishingManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.playerFishListener = new PlayerFishListener(this);
        this.interactListener = new InteractListener(this);
        this.breakBlockListener = new BreakBlockListener(this);
        this.consumeItemListener = new ConsumeItemListener(this);
        this.joinQuitListener = new JoinQuitListener(this);
        this.coolDown = new HashMap<>();
        this.hooks = new HashMap<>();
        this.nextLoot = new HashMap<>();
        this.nextEffect = new HashMap<>();
        this.vanillaLoot = new HashMap<>();
        this.fishingPlayerMap = new ConcurrentHashMap<>();
        this.activeTotemMap = new ConcurrentHashMap<>();
        this.hookCheckTaskMap = new ConcurrentHashMap<>();
        this.breakDetectionMap = new ConcurrentHashMap<>();
        load();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.playerFishListener, plugin);
        Bukkit.getPluginManager().registerEvents(this.interactListener, plugin);
        Bukkit.getPluginManager().registerEvents(this.breakBlockListener, plugin);
        Bukkit.getPluginManager().registerEvents(this.consumeItemListener, plugin);
        Bukkit.getPluginManager().registerEvents(this.joinQuitListener, plugin);
        if (ConfigManager.preventPickUp) {
            this.pickUpListener = new PickUpListener();
            Bukkit.getPluginManager().registerEvents(this.pickUpListener, plugin);
        }
        if (ConfigManager.disableJobsXp) {
            this.jobsRebornXPListener = new JobsRebornXPListener();
            Bukkit.getPluginManager().registerEvents(this.jobsRebornXPListener, plugin);
        }
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.playerFishListener);
        HandlerList.unregisterAll(this.interactListener);
        HandlerList.unregisterAll(this.breakBlockListener);
        HandlerList.unregisterAll(this.consumeItemListener);
        HandlerList.unregisterAll(this.joinQuitListener);
        if (this.pickUpListener != null) HandlerList.unregisterAll(this.pickUpListener);
        if (this.jobsRebornXPListener != null) HandlerList.unregisterAll(this.jobsRebornXPListener);
        for (BobberCheckTask bobberCheckTask : hookCheckTaskMap.values()) {
            bobberCheckTask.stop();
        }
    }

    public void onFishing(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final FishHook fishHook = event.getHook();
        hooks.put(player.getUniqueId(), fishHook);

        boolean noBait = true;
        boolean rodOnMainHand = false;
        ItemStack baitAnimationItem = null;
        ItemStack baitRealItem = null;

        Effect initialEffect = new Effect();
        initialEffect.setWeightMD(new HashMap<>(8));
        initialEffect.setWeightAS(new HashMap<>(8));

        final PlayerInventory inventory = player.getInventory();
        final ItemStack mainHandItem = inventory.getItemInMainHand();
        final ItemStack offHandItem = inventory.getItemInOffHand();

        if (mainHandItem.getType() == Material.FISHING_ROD) {
            rodOnMainHand = true;
        }
        String rod_id = Optional.ofNullable(plugin.getIntegrationManager().getItemID(rodOnMainHand ? mainHandItem : offHandItem)).orElse("vanilla");
        final FishingCondition fishingCondition = new FishingCondition(player.getLocation(), player, rod_id, null);
        String bait_id = plugin.getIntegrationManager().getItemID(rodOnMainHand ? offHandItem: mainHandItem);
        if (bait_id != null) {
            Effect baitEffect = plugin.getEffectManager().getBaitEffect(bait_id);
            if (baitEffect != null && initialEffect.canAddEffect(baitEffect, fishingCondition)) {
                initialEffect.addEffect(baitEffect);
                baitAnimationItem = rodOnMainHand ? offHandItem.clone() : mainHandItem.clone();
                baitRealItem = rodOnMainHand ? offHandItem : mainHandItem;
                noBait = false;
            }
        }

        for (ActivatedTotem activatedTotem : activeTotemMap.values()) {
            if (activatedTotem.getNearbyPlayerSet().contains(player)) {
                initialEffect.addEffect(activatedTotem.getTotem().getEffect());
                break;
            }
        }

        if (ConfigManager.enableFishingBag) {
            Inventory fishingBag = plugin.getBagDataManager().getPlayerBagData(player.getUniqueId());
            HashSet<String> uniqueUtils = new HashSet<>(4);
            if (fishingBag != null) {
                for (int i = 0; i < fishingBag.getSize(); i++) {
                    ItemStack itemStack = fishingBag.getItem(i);
                    String bagItemID = plugin.getIntegrationManager().getItemID(itemStack);
                    if (bagItemID == null) continue;
                    if (noBait) {
                        Effect effect = plugin.getEffectManager().getBaitEffect(bagItemID);
                        if (effect != null) {
                            if (initialEffect.canAddEffect(effect, fishingCondition)) {
                                initialEffect.addEffect(effect);
                                noBait = false;
                                bait_id = bagItemID;
                                baitAnimationItem = itemStack.clone();
                                baitRealItem = itemStack;
                            }
                            continue;
                        }
                    }
                    Effect utilEffect = plugin.getEffectManager().getUtilEffect(bagItemID);
                    if (utilEffect != null && !uniqueUtils.contains(bagItemID) && initialEffect.canAddEffect(utilEffect, fishingCondition)) {
                        initialEffect.addEffect(utilEffect);
                        uniqueUtils.add(bagItemID);
                    }
                }
            }
        }

        Effect rod_effect = plugin.getEffectManager().getRodEffect(rod_id);
        if (rod_effect != null) {
            if (initialEffect.canAddEffect(rod_effect, new FishingCondition(player.getLocation(), player, rod_id, bait_id))) {
                initialEffect.addEffect(rod_effect);
            } else {
                event.setCancelled(true);
                return;
            }
            initialEffect.setSpecialRodID(rod_id);
        }
        this.addEnchantEffect(initialEffect, rodOnMainHand ? mainHandItem : offHandItem, fishingCondition);

        RodCastEvent rodCastEvent = new RodCastEvent(player, initialEffect);
        Bukkit.getPluginManager().callEvent(rodCastEvent);
        if (rodCastEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        if (baitRealItem != null) baitRealItem.setAmount(baitRealItem.getAmount() - 1);
        int lureLevel = rodOnMainHand ? mainHandItem.getEnchantmentLevel(Enchantment.LURE) : offHandItem.getEnchantmentLevel(Enchantment.LURE);

        fishHook.setMaxWaitTime((int) (fishHook.getMaxWaitTime() * initialEffect.getTimeModifier()));
        fishHook.setMinWaitTime((int) (fishHook.getMinWaitTime() * initialEffect.getTimeModifier()));

        this.nextEffect.put(player.getUniqueId(), initialEffect);
        if (ConfigManager.needRodToFish && !initialEffect.hasSpecialRod()) {
            this.nextLoot.put(player.getUniqueId(), LootImpl.EMPTY);
            return;
        }

        int entityID = 0;
        if (baitAnimationItem != null && ConfigManager.baitAnimation) {
            baitAnimationItem.setAmount(1);
            entityID = new Random().nextInt(Integer.MAX_VALUE);
            CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtils.getSpawnPacket(entityID, fishHook.getLocation()));
            CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtils.getMetaPacket(entityID, baitAnimationItem));
        }

        BobberCheckTask bobberCheckTask = new BobberCheckTask(plugin, player, initialEffect, fishHook, this, lureLevel, entityID, rod_id, bait_id);
        this.hookCheckTaskMap.put(player.getUniqueId(), bobberCheckTask);
    }

    public void onBite(PlayerFishEvent event) {
        if (ConfigManager.disableBar || !ConfigManager.instantBar) return;
        showBar(event.getPlayer());
    }

    public void getNextLoot(Player player, Effect initialEffect, List<LootImpl> possibleLoots) {
        List<LootImpl> availableLoots = new ArrayList<>();
        if (possibleLoots.size() == 0){
            nextLoot.put(player.getUniqueId(), null);
            return;
        }

        HashMap<String, Integer> as = initialEffect.getWeightAS();
        HashMap<String, Double> md = initialEffect.getWeightMD();

        double[] weights = new double[possibleLoots.size()];
        int index = 0;
        for (LootImpl loot : possibleLoots){
            double weight = loot.getWeight();
            String group = loot.getGroup();
            if (group != null){
                if (as.get(group) != null){
                    weight += as.get(group);
                }
                if (md.get(group) != null){
                    weight *= md.get(group);
                }
            }
            if (weight <= 0) continue;
            availableLoots.add(loot);
            weights[index++] = weight;
        }

        double total = Arrays.stream(weights).sum();
        double[] weightRatios = new double[index];
        for (int i = 0; i < index; i++){
            weightRatios[i] = weights[i]/total;
        }

        double[] weightRange = new double[index];
        double startPos = 0;
        for (int i = 0; i < index; i++) {
            weightRange[i] = startPos + weightRatios[i];
            startPos += weightRatios[i];
        }

        double random = Math.random();
        int pos = Arrays.binarySearch(weightRange, random);

        if (pos < 0) {
            pos = -pos - 1;
        }
        if (pos < weightRange.length && random < weightRange[pos]) {
            nextLoot.put(player.getUniqueId(), availableLoots.get(pos));
            return;
        }
        nextLoot.put(player.getUniqueId(), null);
    }

    public void onCaughtFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        if (!(event.getCaught() instanceof Item item)) return;
        UUID uuid = player.getUniqueId();
        FishingGame fishingGame = fishingPlayerMap.remove(uuid);
        // if the player is noy playing the game
        if (fishingGame == null) {
            // get his next loot
            LootImpl loot = nextLoot.get(uuid);
            if (loot == LootImpl.EMPTY) return;

            if (ConfigManager.enableVanillaLoot) {
                // Not a vanilla loot
                if (ConfigManager.vanillaLootRatio < Math.random()) {
                    if (loot != null) {
                        vanillaLoot.remove(uuid);
                        if (loot.isDisableBar() || ConfigManager.disableBar) {
                            noBarWaterReelIn(event);
                            return;
                        }
                        showFishingBar(player, loot);
                    }
                    else {
                        vanillaLoot.put(uuid, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                        if (ConfigManager.disableBar) {
                            noBarWaterReelIn(event);
                            return;
                        }
                        showFishingBar(player, plugin.getLootManager().getVanilla_loot());
                    }
                    event.setCancelled(true);
                }
                // Is vanilla loot
                else {
                    if (!plugin.getLootManager().getVanilla_loot().isDisableBar() && !ConfigManager.disableBar) {
                        event.setCancelled(true);
                        vanillaLoot.put(uuid, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                        showFishingBar(player, plugin.getLootManager().getVanilla_loot());
                    }
                    //else vanilla fishing mechanic
                }
            } else {
                // No custom loot
                if (loot == null) {
                    event.setCancelled(true);
                    removeHook(uuid);
                    AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.noLoot);
                }
                else {
                    if (loot.isDisableBar() || ConfigManager.disableBar) {
                        noBarWaterReelIn(event);
                        return;
                    }
                    event.setCancelled(true);
                    showFishingBar(player, loot);
                }
            }
        } else {
            event.setCancelled(true);
            removeHook(uuid);
            proceedReelIn(event.getHook().getLocation(), player, fishingGame);
        }
    }

    public void onReelIn(PlayerFishEvent event) {
        final Player player = event.getPlayer();

        if (ConfigManager.disableBar) {
            noBarLavaReelIn(event);
            return;
        }

        UUID uuid = player.getUniqueId();
        //in fishing game
        FishingGame fishingGame = fishingPlayerMap.remove(uuid);
        if (fishingGame != null) {
            proceedReelIn(event.getHook().getLocation(), player, fishingGame);
            hookCheckTaskMap.remove(uuid);
            return;
        }
        //not in fishing game
        BobberCheckTask bobberCheckTask = hookCheckTaskMap.get(uuid);
        if (bobberCheckTask != null && bobberCheckTask.isHooked()) {
            LootImpl loot = nextLoot.get(uuid);
            if (loot == LootImpl.EMPTY || loot == null) return;
            if (loot.isDisableBar()) {
                noBarLavaReelIn(event);
                return;
            }
            showFishingBar(player, loot);
            event.setCancelled(true);
        }
    }

    public void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        FishingGame fishingGame = fishingPlayerMap.remove(uuid);
        if (fishingGame != null) {
            Entity entity = event.getCaught();
            if (entity != null && entity.getType() == EntityType.ARMOR_STAND) {
                proceedReelIn(event.getHook().getLocation(), player, fishingGame);
            }
            else {
                fishingGame.cancel();
                nextEffect.remove(uuid);
                nextLoot.remove(uuid);
                AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.hookOther);
            }
        }
    }

    private void noBarWaterReelIn(PlayerFishEvent event) {
        Entity entity = event.getCaught();
        if (!(entity instanceof Item item)) {
            return;
        }
        entity.remove();
        event.setExpToDrop(0);
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        LootImpl loot = nextLoot.remove(uuid);
        VanillaLoot vanilla = vanillaLoot.remove(uuid);
        Effect effect = nextEffect.remove(uuid);
        if (vanilla != null) {
            dropVanillaLoot(player, vanilla, item.getLocation(), effect.getDoubleLootChance() > Math.random());
        } else if (loot instanceof Mob mob) {
            summonMob(player, loot, item.getLocation(), mob, effect.getScoreMultiplier());
        } else if (loot instanceof DroppedItem droppedItem){
            if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance){
                if (dropMcMMOLoot(player, item.getLocation(), effect.getDoubleLootChance() > Math.random())){
                    return;
                }
            }
            dropCustomFishingLoot(player, item.getLocation(), droppedItem, effect.getDoubleLootChance() > Math.random(), effect.getScoreMultiplier(), effect.getSizeMultiplier());
        }
    }

    private void noBarLavaReelIn(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        BobberCheckTask bobberCheckTask = hookCheckTaskMap.remove(uuid);
        if (bobberCheckTask != null && bobberCheckTask.isHooked()) {
            LootImpl loot = nextLoot.remove(uuid);
            VanillaLoot vanilla = vanillaLoot.remove(uuid);
            Effect effect = nextEffect.remove(uuid);
            if (vanilla != null) {
                dropVanillaLoot(player, vanilla, event.getHook().getLocation(), effect.getDoubleLootChance() > Math.random());
            } else if (loot instanceof Mob mob) {
                summonMob(player, loot, event.getHook().getLocation(), mob, effect.getScoreMultiplier());
            } else if (loot instanceof DroppedItem droppedItem) {
                if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance) {
                    if (dropMcMMOLoot(player, event.getHook().getLocation(), effect.getDoubleLootChance() > Math.random())){
                        return;
                    }
                }
                dropCustomFishingLoot(player, event.getHook().getLocation(), droppedItem, effect.getDoubleLootChance() > Math.random(), effect.getScoreMultiplier(), effect.getSizeMultiplier());
            }
        }
    }

    public void proceedReelIn(Location hookLoc, Player player, FishingGame fishingGame) {
        fishingGame.cancel();
        final UUID uuid = player.getUniqueId();
        LootImpl loot = nextLoot.remove(uuid);
        VanillaLoot vanilla = vanillaLoot.remove(uuid);
        Effect effect = nextEffect.remove(uuid);
        player.removePotionEffect(PotionEffectType.SLOW);
        if (fishingGame.isSuccess()) {
            if (ConfigManager.rodLoseDurability) loseDurability(player);
            if (hookLoc.getBlock().getType() == Material.LAVA) {
                hookLoc.add(0,0.3,0);
            }
            if (vanilla != null) {
                dropVanillaLoot(player, vanilla, hookLoc, effect.getDoubleLootChance() > Math.random());
            } else if (loot instanceof Mob mob) {
                summonMob(player, loot, hookLoc, mob, effect.getScoreMultiplier());
            } else if (loot instanceof DroppedItem droppedItem){
                if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance){
                    if (dropMcMMOLoot(player, hookLoc, effect.getDoubleLootChance() > Math.random())){
                        return;
                    }
                }
                dropCustomFishingLoot(player, hookLoc, droppedItem, effect.getDoubleLootChance() > Math.random(), effect.getScoreMultiplier(), effect.getSizeMultiplier());
            }
        } else {
            fail(player, loot, vanilla != null);
        }
    }

    private void dropCustomFishingLoot(Player player, Location location, DroppedItem droppedItem, boolean isDouble, double scoreMultiplier, double sizeMultiplier) {
        Pair<ItemStack, FishMeta> dropPair = getCustomFishingLootItemStack(droppedItem, player, sizeMultiplier);
        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_SPECIAL_ITEM, isDouble, dropPair.left(), droppedItem.getKey(), droppedItem);
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (Competition.currentCompetition != null) {
            float score = Competition.currentCompetition.getGoal() == CompetitionGoal.MAX_SIZE
                       || Competition.currentCompetition.getGoal() == CompetitionGoal.TOTAL_SIZE
                       ? dropPair.right().size() : (float) ((float) droppedItem.getScore() * scoreMultiplier);
            Competition.currentCompetition.refreshData(player, score, fishResultEvent.isDouble());
            Competition.currentCompetition.tryJoinCompetition(player);
        }

        if (droppedItem.getSuccessActions() != null)
            for (Action action : droppedItem.getSuccessActions())
                action.doOn(player, null, dropPair.right());

        if (plugin.getVersionHelper().isFolia()) {
            plugin.getScheduler().runTask(() -> dropItem(player, location, fishResultEvent.isDouble(), dropPair.left()), location);
        } else {
            dropItem(player, location, fishResultEvent.isDouble(), dropPair.left());
        }

        addStats(player, droppedItem, isDouble ? 2 : 1);
        sendSuccessTitle(player, droppedItem.getNick());
    }

    public ItemStack getCustomFishingLootItemStack(DroppedItem droppedItem, Player player) {
        return getCustomFishingLootItemStack(droppedItem, player, 1).left();
    }

    public Pair<ItemStack, FishMeta> getCustomFishingLootItemStack(DroppedItem droppedItem, @Nullable Player player, double sizeMultiplier) {
        ItemStack drop = plugin.getIntegrationManager().build(droppedItem.getMaterial(), player);
        FishMeta fishMeta = null;
        if (drop.getType() != Material.AIR) {
            if (droppedItem.getRandomEnchants() != null)
                ItemStackUtils.addRandomEnchants(drop, droppedItem.getRandomEnchants());
            if (droppedItem.isRandomDurability())
                ItemStackUtils.addRandomDamage(drop);
            if (ConfigManager.preventPickUp && player != null)
                ItemStackUtils.addOwner(drop, player.getName());
            fishMeta = ItemStackUtils.addExtraMeta(drop, droppedItem, sizeMultiplier, player);
        }
        return Pair.of(drop, fishMeta);
    }

    private boolean dropMcMMOLoot(Player player, Location location, boolean isDouble) {
        ItemStack itemStack = McMMOTreasure.getTreasure(player);
        if (itemStack == null) return false;

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_VANILLA_ITEM, isDouble, itemStack, "mcMMO", null);
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return true;
        }

        doVanillaActions(player, location, itemStack, fishResultEvent.isDouble());
        new VanillaXPImpl(new Random().nextInt(24), true, 1).doOn(player);
        return true;
    }

    private void dropVanillaLoot(Player player, VanillaLoot vanillaLoot, Location location, boolean isDouble) {
        ItemStack itemStack = vanillaLoot.getItemStack();
        if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance){
            ItemStack mcMMOItemStack = McMMOTreasure.getTreasure(player);
            if (mcMMOItemStack != null){
                itemStack = mcMMOItemStack;
            }
        }

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_VANILLA_ITEM, isDouble, itemStack, "vanilla", null);
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        doVanillaActions(player, location, itemStack, fishResultEvent.isDouble());
        new VanillaXPImpl(vanillaLoot.getXp(), true, 1).doOn(player);
    }

    private void doVanillaActions(Player player, Location location, ItemStack itemStack, boolean isDouble) {
        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.refreshData(player, (float) plugin.getLootManager().getVanilla_loot().getScore(), isDouble);
            Competition.currentCompetition.tryJoinCompetition(player);
        }

        LootImpl vanilla = plugin.getLootManager().getVanilla_loot();
        addStats(player, vanilla, isDouble ? 2 : 1);

        if (vanilla.getSuccessActions() != null)
            for (Action action : vanilla.getSuccessActions())
                action.doOn(player);

        AdventureUtils.playerSound(player, Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);

        if (plugin.getVersionHelper().isFolia()) {
            plugin.getScheduler().runTask(() -> dropItem(player, location, isDouble, itemStack), location);
        } else {
            dropItem(player, location, isDouble, itemStack);
        }

        sendSuccessTitle(player, itemStack);
    }

    private void dropItem(Player player, Location location, boolean isDouble, ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) return;
        Entity item = location.getWorld().dropItem(location, itemStack);
        Vector vector = player.getLocation().subtract(location).toVector().multiply(0.105);
        vector = vector.setY((vector.getY() + 0.2) * 1.18);
        item.setVelocity(vector);
        if (isDouble) {
            Entity item2 = location.getWorld().dropItem(location, itemStack);
            item2.setVelocity(vector);
        }
    }

    private void addStats(Player player, LootImpl loot, int amount) {
        player.setStatistic(Statistic.FISH_CAUGHT, player.getStatistic(Statistic.FISH_CAUGHT) + 1);
        if (!ConfigManager.enableStatistics) return;
        if (loot.isDisableStats()) return;
        plugin.getStatisticsManager().addFishAmount(player.getUniqueId(), loot, amount);
    }

    private void summonMob(Player player, LootImpl loot, Location location, Mob mob, double scoreMultiplier) {
        MobInterface mobInterface = plugin.getIntegrationManager().getMobInterface();
        if (mobInterface == null) return;

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_MOB, false, null, loot.getKey(), loot);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (Competition.currentCompetition != null) {
            float score = Competition.currentCompetition.getGoal() == CompetitionGoal.MAX_SIZE || Competition.currentCompetition.getGoal() == CompetitionGoal.TOTAL_SIZE ? 0 : (float) loot.getScore();
            Competition.currentCompetition.refreshData(player, (float) (score * scoreMultiplier), false);
            Competition.currentCompetition.tryJoinCompetition(player);
        }

        if (loot.getSuccessActions() != null)
            for (Action action : loot.getSuccessActions())
                action.doOn(player);

        mobInterface.summon(player.getLocation(), location, mob);
        addStats(player, mob, 1);
        sendSuccessTitle(player, loot.getNick());
    }

    @NotNull
    private Component getTitleComponent(ItemStack itemStack, String text) {
        Component titleComponent = Component.text("");
        int startIndex = 0;
        int lootIndex;
        while ((lootIndex = text.indexOf("{loot}", startIndex)) != -1) {
            String before = text.substring(startIndex, lootIndex);
            titleComponent = titleComponent.append(AdventureUtils.getComponentFromMiniMessage(before));
            startIndex = lootIndex + 6;
            titleComponent = titleComponent.append(getDisplayName(itemStack));
        }
        String after = text.substring(startIndex);
        titleComponent = titleComponent.append(AdventureUtils.getComponentFromMiniMessage(after));
        return titleComponent;
    }

    private void sendSuccessTitle(Player player, String loot) {
        if (!ConfigManager.enableSuccessTitle) return;
        plugin.getScheduler().runTaskAsyncLater(() -> AdventureUtils.playerTitle(
                player,
                ConfigManager.successTitle[new Random().nextInt(ConfigManager.successTitle.length)]
                        .replace("{loot}", loot)
                        .replace("{player}", player.getName()),
                ConfigManager.successSubTitle[new Random().nextInt(ConfigManager.successSubTitle.length)]
                        .replace("{loot}", loot)
                        .replace("{player}", player.getName()),
                ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut
        ), 400, TimeUnit.MILLISECONDS);
    }

    private void sendSuccessTitle(Player player, ItemStack itemStack) {
        if (!ConfigManager.enableSuccessTitle) return;
        String title = ConfigManager.successTitle[new Random().nextInt(ConfigManager.successTitle.length)];
        Component titleComponent = getTitleComponent(itemStack, title);
        String subTitle = ConfigManager.successSubTitle[new Random().nextInt(ConfigManager.successSubTitle.length)];
        Component subtitleComponent = getTitleComponent(itemStack, subTitle);
        plugin.getScheduler().runTaskAsyncLater(() -> AdventureUtils.playerTitle(
                player,
                titleComponent,
                subtitleComponent,
                ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut
        ), 400, TimeUnit.MILLISECONDS);
    }

    private void loseDurability(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        plugin.getScheduler().runTaskAsyncLater(() -> {
            ItemStack rod = getFishingRod(player);
            if (rod != null) {
                plugin.getIntegrationManager().loseCustomDurability(rod, player);
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    private ItemStack getFishingRod(Player player) {
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.FISHING_ROD) {
            return mainHand;
        }
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (offHand.getType() == Material.FISHING_ROD) {
            return offHand;
        }
        return null;
    }

    public void fail(Player player, LootImpl loot, boolean isVanilla) {
        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.FAILURE, false, null, null, null);
        Bukkit.getServer().getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (!isVanilla && loot != null && loot.getFailureActions() != null) {
            for (Action action : loot.getFailureActions())
                action.doOn(player);
        }

        if (!ConfigManager.enableFailureTitle) return;
        plugin.getScheduler().runTaskAsyncLater(() -> AdventureUtils.playerTitle(
                player,
                ConfigManager.failureTitle[new Random().nextInt(ConfigManager.failureTitle.length)],
                ConfigManager.failureSubTitle[new Random().nextInt(ConfigManager.failureSubTitle.length)],
                ConfigManager.failureFadeIn,
                ConfigManager.failureFadeStay,
                ConfigManager.failureFadeOut
        ), 400, TimeUnit.MILLISECONDS);
    }

    public void showBar(Player player) {
        final UUID uuid = player.getUniqueId();
        if (fishingPlayerMap.get(uuid) != null) return;
        LootImpl loot = nextLoot.get(uuid);
        if (loot != null) {
            if (loot == LootImpl.EMPTY) return;
            showFishingBar(player, loot);
        }
    }

    public boolean isCoolDown(Player player, long delay) {
        long time = System.currentTimeMillis();
        long last = coolDown.getOrDefault(player.getUniqueId(), time - delay);
        if (last + delay > time) {
            return true;
        } else {
            coolDown.put(player.getUniqueId(), time);
            return false;
        }
    }

    private void addEnchantEffect(Effect initialEffect, ItemStack itemStack, FishingCondition fishingCondition) {
        for (String key : plugin.getIntegrationManager().getEnchantmentInterface().getEnchants(itemStack)) {
            Effect enchantEffect = plugin.getEffectManager().getEnchantEffect(key);
            if (enchantEffect != null && enchantEffect.canAddEffect(enchantEffect, fishingCondition)) {
                initialEffect.addEffect(enchantEffect);
            }
        }
    }

    public List<LootImpl> getPossibleLootList(FishingCondition fishingCondition, boolean finder, Collection<LootImpl> values) {
        Stream<LootImpl> stream = values.stream();
        if (finder) {
            stream = stream.filter(LootImpl::isShowInFinder);
        }
        return stream.filter(loot -> {
            RequirementInterface[] requirements = loot.getRequirements();
            if (requirements == null) {
                return true;
            }
            for (RequirementInterface requirement : requirements) {
                if (!requirement.isConditionMet(fishingCondition)) {
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());
    }

    @Override
    public void onInteract(PlayerInteractEvent event) {
        ItemStack itemStack = event.getItem();
        final Player player = event.getPlayer();
        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound != null && cfCompound.getString("type").equals("util") && cfCompound.getString("id").equals("fishfinder")) {
            if (isCoolDown(player, 1000)) return;
            useFinder(event.getPlayer());
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) return;
        String totemID = nbtItem.getString("Totem");
        if (totemID.equals("")) return;
        TotemConfig totem = plugin.getTotemManager().getTotem(totemID);
        if (totem == null) return;
        if (isCoolDown(player, 1000)) return;
        String blockID = plugin.getIntegrationManager().getBlockInterface().getID(block);
        if (blockID == null) return;
        List<TotemConfig> totemList = plugin.getTotemManager().getTotemsByCoreID(blockID);
        if (totemList == null || !totemList.contains(totem)) return;

        FishingCondition fishingCondition = new FishingCondition(block.getLocation(), player);
        if (totem.getRequirements() != null)
            for (RequirementInterface requirement : totem.getRequirements()) {
                if (!requirement.isConditionMet(fishingCondition)) {
                    return;
                }
            }

        Location coreLoc = block.getLocation();
        int direction = plugin.getTotemManager().checkLocationModel(totem.getOriginalModel(), coreLoc);
        if (direction == 0) return;

        TotemActivationEvent totemActivationEvent = new TotemActivationEvent(player, coreLoc, totem);
        Bukkit.getPluginManager().callEvent(totemActivationEvent);
        if (totemActivationEvent.isCancelled()) {
            return;
        }

        if (activeTotemMap.get(LocationUtils.getSimpleLocation(coreLoc)) != null) {
            activeTotemMap.get(LocationUtils.getSimpleLocation(coreLoc)).stop();
        }

        plugin.getTotemManager().removeModel(totem.getFinalModel(), coreLoc, direction);
        if (player.getGameMode() != GameMode.CREATIVE) itemStack.setAmount(itemStack.getAmount() - 1);
        if (totem.getActivatorActions() != null)
            for (Action action : totem.getActivatorActions()) {
                action.doOn(player);
            }
        if (totem.getNearbyActions() != null)
            for (Action action : totem.getNearbyActions()) {
                for (Player nearby : LocationUtils.getNearbyPlayers(coreLoc, totem.getRadius())) {
                    action.doOn(nearby, player);
                }
            }

        ActivatedTotem activatedTotem = new ActivatedTotem(coreLoc, totem, this, direction, player.getName());
        activeTotemMap.put(LocationUtils.getSimpleLocation(coreLoc), activatedTotem);
    }

    private void useFinder(Player player) {
        if (!ConfigManager.getWorldsList().contains(player.getWorld().getName())) {
            return;
        }

        FishingCondition fishingCondition = new FishingCondition(player.getLocation(), player, "fish_finder", "fish_finder");
        List<LootImpl> possibleLoots = getPossibleLootList(fishingCondition, true, plugin.getLootManager().getAllLoots());

        FishFinderEvent fishFinderEvent = new FishFinderEvent(player, possibleLoots);
        Bukkit.getPluginManager().callEvent(fishFinderEvent);
        if (fishFinderEvent.isCancelled()) {
            return;
        }

        if (possibleLoots.size() == 0) {
            AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.noLoot);
            return;
        }
        StringJoiner stringJoiner = new StringJoiner(MessageManager.splitChar);
        possibleLoots.forEach(loot -> stringJoiner.add(loot.getNick()));
        AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.possibleLoots + stringJoiner);
    }

    private void showFishingBar(Player player, @NotNull LootImpl loot) {
        MiniGameConfig game = loot.getFishingGames() != null
                ? loot.getFishingGames()[new Random().nextInt(loot.getFishingGames().length)]
                : plugin.getBarMechanicManager().getRandomGame();
        int difficult = game.getRandomDifficulty() + nextEffect.getOrDefault(player.getUniqueId(), new Effect()).getDifficulty();
        MiniGameStartEvent miniGameStartEvent = new MiniGameStartEvent(player, Math.min(10, Math.max(1, difficult)));
        Bukkit.getPluginManager().callEvent(miniGameStartEvent);
        if (miniGameStartEvent.isCancelled()) {
            return;
        }
        FishingBar fishingBar = game.getRandomBar();
        FishingGame fishingGame = null;
        Location hookLoc = getHookLocation(player);
        if (hookLoc == null) return;
        if (fishingBar instanceof ModeOneBar modeOneBar) {
            fishingGame = new ModeOneGame(plugin, this, System.currentTimeMillis() + game.getTime() * 1000L, player, miniGameStartEvent.getDifficulty(), modeOneBar);
        }
        else if (fishingBar instanceof ModeTwoBar modeTwoBar) {
            fishingGame = new ModeTwoGame(plugin, this, System.currentTimeMillis() + game.getTime() * 1000L, player, miniGameStartEvent.getDifficulty(), modeTwoBar, hookLoc);
        }
        else if (fishingBar instanceof ModeThreeBar modeThreeBar) {
            fishingGame = new ModeThreeGame(plugin, this, System.currentTimeMillis() + game.getTime() * 1000L, player, miniGameStartEvent.getDifficulty(), modeThreeBar);
        }
        if (fishingGame != null) {
            fishingPlayerMap.put(player.getUniqueId(), fishingGame);
        }
        if (loot.getHookActions() != null) {
            for (Action action : loot.getHookActions()) {
                action.doOn(player);
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, game.getTime() * 20,3));
    }

    @Override
    public void onQuit(Player player) {
        final UUID uuid = player.getUniqueId();
        coolDown.remove(uuid);
        nextLoot.remove(uuid);
        nextEffect.remove(uuid);
        vanillaLoot.remove(uuid);
        BobberCheckTask task = hookCheckTaskMap.remove(uuid);
        if (task != null) task.stop();
        removeHook(uuid);
    }

    public void removeFishingPlayer(Player player) {
        fishingPlayerMap.remove(player.getUniqueId());
    }

    private Component getDisplayName(ItemStack itemStack){
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("display");
        if (nbtCompound != null){
            String name = nbtCompound.getString("Name");
            if (!name.equals("")){
                return GsonComponentSerializer.gson().deserialize(name);
            }
        }
        String type = itemStack.getType().toString().toLowerCase();
        if (itemStack.getType().isBlock()) return GsonComponentSerializer.gson().deserialize("{\"translate\":\"block.minecraft." + type + "\"}");
        else return GsonComponentSerializer.gson().deserialize("{\"translate\":\"item.minecraft." + type + "\"}");
    }

    public void removeTotem(Location coreLoc) {
        activeTotemMap.remove(LocationUtils.getSimpleLocation(coreLoc));
    }

    public void removePlayerFromLavaFishing(Player player) {
        this.hookCheckTaskMap.remove(player.getUniqueId());
    }

    public float getSize(ItemStack itemStack) {
        NBTCompound fishMeta = new NBTItem(itemStack).getCompound("FishMeta");
        return fishMeta != null ? fishMeta.getFloat("size") : 0;
    }

    public void addTotemBreakDetectToCache(SimpleLocation part, SimpleLocation coreLoc) {
        breakDetectionMap.put(part, coreLoc);
    }

    public void removeTotemBreakDetectFromCache(SimpleLocation part) {
        breakDetectionMap.remove(part);
    }

    @Override
    public void onBreakBlock(BlockBreakEvent event) {
        SimpleLocation coreLoc = breakDetectionMap.get(LocationUtils.getSimpleLocation(event.getBlock().getLocation()));
        if (coreLoc != null) {
            ActivatedTotem activatedTotem = activeTotemMap.get(coreLoc);
            if (activatedTotem != null) {
                activatedTotem.stop();
            }
        }
    }

    public void removeHook(UUID uuid) {
        FishHook fishHook = removeHookCache(uuid);
        if (fishHook != null) {
            plugin.getScheduler().runTask(fishHook::remove, fishHook.getLocation());
        }
    }

    public FishHook removeHookCache(UUID uuid) {
        return hooks.remove(uuid);
    }

    @Nullable
    public FishHook getHook(UUID uuid) {
        return hooks.get(uuid);
    }

    @Nullable
    public Location getHookLocation(Player player) {
        FishHook fishHook = hooks.get(player.getUniqueId());
        if (fishHook != null) {
            return fishHook.getLocation();
        }
        return null;
    }

    @Override
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        ItemStack itemStack = event.getItem();
        NBTCompound nbtCompound = new NBTItem(itemStack).getCompound("CustomFishing");
        if (nbtCompound == null) return;
        if (!nbtCompound.getString("type").equals("loot")) return;
        String lootKey = nbtCompound.getString("id");
        LootImpl loot = plugin.getLootManager().getLoot(lootKey);
        if (loot == null) return;
        if (!(loot instanceof DroppedItem droppedItem)) return;
        final Player player = event.getPlayer();
        if (droppedItem.getConsumeActions() != null)
            for (Action action : droppedItem.getConsumeActions())
                action.doOn(player);
    }

    public Effect getInitialEffect(Player player) {
        boolean noBait = true;
        boolean rodOnMainHand = false;

        Effect initialEffect = new Effect();
        initialEffect.setWeightMD(new HashMap<>(8));
        initialEffect.setWeightAS(new HashMap<>(8));

        final PlayerInventory inventory = player.getInventory();
        final ItemStack mainHandItem = inventory.getItemInMainHand();
        final ItemStack offHandItem = inventory.getItemInOffHand();

        if (mainHandItem.getType() == Material.FISHING_ROD) {
            rodOnMainHand = true;
        }
        String rod_id = Optional.ofNullable(rodOnMainHand ? CustomFishingAPI.getRodID(mainHandItem) : CustomFishingAPI.getRodID(offHandItem)).orElse("vanilla");
        final FishingCondition fishingCondition = new FishingCondition(player.getLocation(), player, rod_id, null);

        String bait_id = Optional.ofNullable(rodOnMainHand ? CustomFishingAPI.getBaitID(offHandItem) : CustomFishingAPI.getBaitID(mainHandItem)).orElse("");
        Effect baitEffect = plugin.getEffectManager().getBaitEffect(bait_id);
        if (baitEffect != null && initialEffect.canAddEffect(baitEffect, fishingCondition)) {
            initialEffect.addEffect(baitEffect);
            noBait = false;
        }

        for (ActivatedTotem activatedTotem : activeTotemMap.values()) {
            if (activatedTotem.getNearbyPlayerSet().contains(player)) {
                initialEffect.addEffect(activatedTotem.getTotem().getEffect());
                break;
            }
        }

        if (ConfigManager.enableFishingBag) {
            Inventory fishingBag = plugin.getBagDataManager().getPlayerBagData(player.getUniqueId());
            HashSet<String> uniqueUtils = new HashSet<>(4);
            if (fishingBag != null) {
                for (int i = 0; i < fishingBag.getSize(); i++) {
                    ItemStack itemStack = fishingBag.getItem(i);
                    if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                    NBTCompound cfCompound = new NBTItem(itemStack).getCompound("CustomFishing");
                    if (cfCompound == null) continue;
                    String type = cfCompound.getString("type"); String id = cfCompound.getString("id");
                    if (noBait && type.equals("bait")) {
                        Effect effect = plugin.getEffectManager().getBaitEffect(id);
                        if (effect != null && itemStack.getAmount() > 0 && initialEffect.canAddEffect(effect, fishingCondition)) {
                            initialEffect.addEffect(effect);
                            noBait = false;
                            bait_id = id;
                        }
                    } else if (type.equals("util")) {
                        Effect utilEffect = plugin.getEffectManager().getUtilEffect(id);
                        if (utilEffect != null && !uniqueUtils.contains(id)) {
                            initialEffect.addEffect(utilEffect);
                            uniqueUtils.add(id);
                        }
                    }
                }
            }
        }

        Effect rod_effect = plugin.getEffectManager().getRodEffect(rod_id);
        if (rod_effect != null) {
            if (initialEffect.canAddEffect(rod_effect, new FishingCondition(player.getLocation(), player, rod_id, bait_id))) {
                initialEffect.addEffect(rod_effect);
            } else {
                return null;
            }
            initialEffect.setSpecialRodID(rod_id);
        }
        this.addEnchantEffect(initialEffect, rodOnMainHand ? mainHandItem : offHandItem, fishingCondition);
        return initialEffect;
    }
}