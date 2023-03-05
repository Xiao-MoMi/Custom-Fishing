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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.*;
import net.momirealms.customfishing.data.PlayerBagData;
import net.momirealms.customfishing.fishing.*;
import net.momirealms.customfishing.fishing.bar.FishingBar;
import net.momirealms.customfishing.fishing.bar.ModeOneBar;
import net.momirealms.customfishing.fishing.bar.ModeThreeBar;
import net.momirealms.customfishing.fishing.bar.ModeTwoBar;
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.fishing.competition.CompetitionGoal;
import net.momirealms.customfishing.fishing.loot.DroppedItem;
import net.momirealms.customfishing.fishing.loot.Loot;
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
import net.momirealms.customfishing.object.SimpleLocation;
import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.FakeItemUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import net.momirealms.customfishing.util.LocationUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
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

public class FishingManager extends Function {

    private final CustomFishing plugin;
    private final PlayerFishListener playerFishListener;
    private final InteractListener interactListener;
    private final ConsumeItemListener consumeItemListener;
    private PickUpListener pickUpListener;
    private MMOItemsListener mmoItemsListener;
    private JobsRebornXPListener jobsRebornXPListener;
    private final JoinQuitListener joinQuitListener;
    private final BreakBlockListener breakBlockListener;
    private final HashMap<Player, Long> coolDown;
    private final HashMap<Player, FishHook> hooks;
    private final HashMap<Player, Loot> nextLoot;
    private final HashMap<Player, Effect> nextEffect;
    private final HashMap<Player, VanillaLoot> vanillaLoot;
    private final ConcurrentHashMap<Player, FishingGame> fishingPlayerMap;
    private final ConcurrentHashMap<SimpleLocation, ActivatedTotem> activeTotemMap;
    private final ConcurrentHashMap<SimpleLocation, SimpleLocation> breakDetectionMap;
    private final ConcurrentHashMap<Player, BobberCheckTask> hookCheckTaskMap;

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
        if (ConfigManager.convertMMOItems) {
            this.mmoItemsListener = new MMOItemsListener(this);
            Bukkit.getPluginManager().registerEvents(this.mmoItemsListener, plugin);
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
        if (this.mmoItemsListener != null) HandlerList.unregisterAll(this.mmoItemsListener);
        if (this.jobsRebornXPListener != null) HandlerList.unregisterAll(this.jobsRebornXPListener);
        for (BobberCheckTask bobberCheckTask : hookCheckTaskMap.values()) {
            bobberCheckTask.stop();
        }
    }

    public void onFishing(PlayerFishEvent event) {

        final Player player = event.getPlayer();
        final FishHook fishHook = event.getHook();

        hooks.put(player, fishHook);
        if (isCoolDown(player, 500)) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {

            PlayerInventory inventory = player.getInventory();

            boolean noSpecialRod = true;
            boolean noRod = true;
            boolean noBait = true;
            int lureLevel = 0;
            ItemStack baitItem = null;

            Effect initialEffect = new Effect();
            initialEffect.setDifficulty(0);
            initialEffect.setDoubleLootChance(0);
            initialEffect.setTimeModifier(1);
            initialEffect.setScoreMultiplier(0);
            initialEffect.setWeightMD(new HashMap<>());
            initialEffect.setWeightAS(new HashMap<>());

            ItemStack mainHandItem = inventory.getItemInMainHand();
            Material mainHandItemType = mainHandItem.getType();
            if (mainHandItemType != Material.AIR) {
                if (mainHandItemType == Material.FISHING_ROD) {
                    noRod = false;
                    enchantBonus(initialEffect, mainHandItem);
                    lureLevel = mainHandItem.getEnchantmentLevel(Enchantment.LURE);
                }
                NBTItem mainHandNBTItem = new NBTItem(mainHandItem);
                NBTCompound nbtCompound = mainHandNBTItem.getCompound("CustomFishing");
                if (nbtCompound != null) {
                    if (nbtCompound.getString("type").equals("rod")) {
                        Effect rodEffect = plugin.getEffectManager().getRodEffect(nbtCompound.getString("id"));
                        if (rodEffect != null){
                            initialEffect.addEffect(rodEffect);
                            noSpecialRod = false;
                        }
                    }
                    else if (nbtCompound.getString("type").equals("bait")) {
                        Effect baitEffect = plugin.getEffectManager().getBaitEffect(nbtCompound.getString("id"));
                        if (baitEffect != null) {
                            initialEffect.addEffect(baitEffect);
                            baitItem = mainHandItem.clone();
                            mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                            noBait = false;
                        }
                    }
                }
            }

            ItemStack offHandItem = inventory.getItemInOffHand();
            Material offHandItemType = offHandItem.getType();
            if (offHandItemType != Material.AIR){
                if (noRod && offHandItemType == Material.FISHING_ROD) {
                    enchantBonus(initialEffect, offHandItem);
                    lureLevel = offHandItem.getEnchantmentLevel(Enchantment.LURE);
                }
                NBTItem offHandNBTItem = new NBTItem(offHandItem);
                NBTCompound nbtCompound = offHandNBTItem.getCompound("CustomFishing");
                if (nbtCompound != null) {
                    if (noBait && nbtCompound.getString("type").equals("bait")) {
                        Effect baitEffect = plugin.getEffectManager().getBaitEffect(nbtCompound.getString("id"));
                        if (baitEffect != null){
                            initialEffect.addEffect(baitEffect);
                            offHandItem.setAmount(offHandItem.getAmount() - 1);
                            baitItem = offHandItem.clone();
                            noBait = false;
                        }
                    }
                    else if (noSpecialRod && nbtCompound.getString("type").equals("rod")) {
                        Effect rodEffect = plugin.getEffectManager().getRodEffect(nbtCompound.getString("id"));
                        if (rodEffect != null) {
                            initialEffect.addEffect(rodEffect);
                            noSpecialRod = false;
                        }
                    }
                }
            }

            for (ActivatedTotem activatedTotem : activeTotemMap.values()) {
                if (activatedTotem.getNearbyPlayerSet().contains(player)) {
                    initialEffect.addEffect(activatedTotem.getTotem().getBonus());
                    break;
                }
            }

            if (ConfigManager.enableFishingBag && noBait) {
                PlayerBagData playerBagData = plugin.getBagDataManager().getPlayerBagData(player.getUniqueId());
                if (playerBagData != null) {
                    Inventory baitInv = playerBagData.getInventory();
                    for (int i = 0; i < baitInv.getSize(); i++) {
                        ItemStack itemStack = baitInv.getItem(i);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        NBTItem nbtItem = new NBTItem(itemStack);
                        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
                        if (cfCompound == null) continue;
                        if (!cfCompound.getString("type").equals("bait")) continue;
                        Effect baitEffect = plugin.getEffectManager().getBaitEffect(cfCompound.getString("id"));
                        if (baitEffect != null) {
                            initialEffect.addEffect(baitEffect);
                            baitItem = itemStack.clone();
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            break;
                        }
                    }
                }
            }

            RodCastEvent rodCastEvent = new RodCastEvent(player, initialEffect);
            if (rodCastEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            fishHook.setMaxWaitTime((int) (fishHook.getMaxWaitTime() * initialEffect.getTimeModifier()));
            fishHook.setMinWaitTime((int) (fishHook.getMinWaitTime() * initialEffect.getTimeModifier()));

            nextEffect.put(player, initialEffect);

            if (ConfigManager.needRodToFish && noSpecialRod) {
                nextLoot.put(player, Loot.EMPTY);
                return;
            }

            initialEffect.setHasSpecialRod(!noSpecialRod);

            int entityID = 0;
            if (baitItem != null) {
                baitItem.setAmount(1);
                entityID = new Random().nextInt(Integer.MAX_VALUE);
                CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtil.getSpawnPacket(entityID, fishHook.getLocation()));
                CustomFishing.getProtocolManager().sendServerPacket(player, FakeItemUtil.getMetaPacket(entityID, baitItem));
            }

            BobberCheckTask bobberCheckTask = new BobberCheckTask(plugin, player, initialEffect, fishHook, this, lureLevel, entityID);
            bobberCheckTask.runTaskTimer(plugin, 1, 1);
            hookCheckTaskMap.put(player, bobberCheckTask);
        });
    }

    public void getNextLoot(Player player, Effect initialEffect, List<Loot> possibleLoots) {
        List<Loot> availableLoots = new ArrayList<>();
        if (possibleLoots.size() == 0){
            nextLoot.put(player, null);
            return;
        }

        HashMap<String, Integer> as = initialEffect.getWeightAS();
        HashMap<String, Double> md = initialEffect.getWeightMD();

        double[] weights = new double[possibleLoots.size()];
        int index = 0;
        for (Loot loot : possibleLoots){
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
            nextLoot.put(player, availableLoots.get(pos));
            return;
        }
        nextLoot.put(player, null);
    }

    public void onCaughtFish(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        if (!(event.getCaught() instanceof Item item)) return;

        if (ConfigManager.disableBar) {
            noBarWaterReelIn(event);
            return;
        }

        FishingGame fishingGame = fishingPlayerMap.remove(player);
        if (fishingGame == null) {

            Loot loot = nextLoot.get(player);
            if (loot == Loot.EMPTY) return;

            if (ConfigManager.enableVanillaLoot) {
                // Not a vanilla loot
                if (ConfigManager.vanillaLootRatio < Math.random()) {
                    event.setCancelled(true);
                    if (loot != null) {
                        vanillaLoot.remove(player);
                    }
                    else {
                        vanillaLoot.put(player, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                    }
                    showFishingBar(player, loot);
                }
                // Is vanilla loot
                else {
                    if (ConfigManager.alwaysFishingBar) {
                        event.setCancelled(true);
                        vanillaLoot.put(player, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                        showFishingBar(player, null);
                    }
                    //else vanilla fishing mechanic
                }
            }
            else {
                // No custom loot
                if (loot == null) {
                    item.remove();
                    event.setExpToDrop(0);
                    AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.noLoot);
                }
                else {
                    event.setCancelled(true);
                    showFishingBar(player, loot);
                }
            }
        }
        else {
            item.remove();
            event.setExpToDrop(0);
            proceedReelIn(event.getHook().getLocation(), player, fishingGame);
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
        Loot loot = nextLoot.remove(player);
        VanillaLoot vanilla = vanillaLoot.remove(player);
        Effect effect = nextEffect.remove(player);
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
        BobberCheckTask bobberCheckTask = hookCheckTaskMap.remove(player);
        if (bobberCheckTask != null && bobberCheckTask.isHooked()) {
            Loot loot = nextLoot.remove(player);
            VanillaLoot vanilla = vanillaLoot.remove(player);
            Effect effect = nextEffect.remove(player);
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
        Loot loot = nextLoot.remove(player);
        VanillaLoot vanilla = vanillaLoot.remove(player);
        Effect effect = nextEffect.remove(player);
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
        }
        else {
            fail(player, loot, vanilla != null);
        }
    }

    public void onReelIn(PlayerFishEvent event) {
        final Player player = event.getPlayer();

        if (ConfigManager.disableBar) {
            noBarLavaReelIn(event);
            return;
        }
        //in fishing
        FishingGame fishingGame = fishingPlayerMap.remove(player);
        if (fishingGame != null) {
            proceedReelIn(event.getHook().getLocation(), player, fishingGame);
            hookCheckTaskMap.remove(player);
            return;
        }
        //not in fishing
        BobberCheckTask bobberCheckTask = hookCheckTaskMap.get(player);
        if (bobberCheckTask != null && bobberCheckTask.isHooked()) {
            Loot loot = nextLoot.get(player);
            if (loot == Loot.EMPTY) return;
            showFishingBar(player, loot);
            event.setCancelled(true);
        }
    }

    private void dropCustomFishingLoot(Player player, Location location, DroppedItem droppedItem, boolean isDouble, double scoreMultiplier, double sizeMultiplier) {
        ItemStack drop = getCustomFishingLootItemStack(droppedItem, player, sizeMultiplier);
        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_SPECIAL_ITEM, isDouble, drop, droppedItem.getKey());
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (Competition.currentCompetition != null) {
            float score;
            if (Competition.currentCompetition.getGoal() == CompetitionGoal.MAX_SIZE || Competition.currentCompetition.getGoal() == CompetitionGoal.TOTAL_SIZE) {
                score = getSize(drop);
            }
            else {
                score = (float) ((float) droppedItem.getScore() * scoreMultiplier);
            }
            Competition.currentCompetition.refreshData(player, score, fishResultEvent.isDouble());
            Competition.currentCompetition.tryAddBossBarToPlayer(player);
        }

        dropItem(player, location, fishResultEvent.isDouble(), drop);
        for (ActionInterface action : droppedItem.getSuccessActions())
            action.doOn(player, null);
        sendSuccessTitle(player, droppedItem.getNick());
    }

    public ItemStack getCustomFishingLootItemStack(DroppedItem droppedItem, Player player) {
        return getCustomFishingLootItemStack(droppedItem, player, 1);
    }

    public ItemStack getCustomFishingLootItemStack(DroppedItem droppedItem, Player player, double sizeMultiplier) {
        ItemStack drop = plugin.getIntegrationManager().build(droppedItem.getMaterial());
        if (drop.getType() != Material.AIR) {
            if (droppedItem.getRandomEnchants() != null)
                ItemStackUtil.addRandomEnchants(drop, droppedItem.getRandomEnchants());
            if (droppedItem.isRandomDurability())
                ItemStackUtil.addRandomDamage(drop);
            if (ConfigManager.preventPickUp)
                ItemStackUtil.addOwner(drop, player.getName());
            if (ConfigManager.addTagToFish)
                ItemStackUtil.addIdentifier(drop, "loot", droppedItem.getKey());
            if (drop.getType() == Material.PLAYER_HEAD) {
                NBTItem nbtItem = new NBTItem(drop);
                NBTCompound nbtCompound = nbtItem.getCompound("SkullOwner");
                if (nbtCompound != null && !nbtCompound.hasTag("Id")) {
                    nbtCompound.setUUID("Id", UUID.randomUUID());
                    drop.setItemMeta(nbtItem.getItem().getItemMeta());
                }
            }
            ItemStackUtil.addExtraMeta(drop, droppedItem, sizeMultiplier);
        }
        return drop;
    }

    private boolean dropMcMMOLoot(Player player, Location location, boolean isDouble) {
        ItemStack itemStack = McMMOTreasure.getTreasure(player);
        if (itemStack == null) return false;

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_VANILLA_ITEM, isDouble, itemStack, "mcmmo");
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return true;
        }

        if (Competition.currentCompetition != null) {
            Competition.currentCompetition.refreshData(player, 0, fishResultEvent.isDouble());
            Competition.currentCompetition.tryAddBossBarToPlayer(player);
        }

        player.giveExp(new Random().nextInt(24), true);
        dropItem(player, location, fishResultEvent.isDouble(), itemStack);
        sendSuccessTitle(player, itemStack);
        return true;
    }

    private void dropItem(Player player, Location location, boolean isDouble, ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) return;
        Entity item = location.getWorld().dropItem(location, itemStack);
        Vector vector = player.getLocation().subtract(location).toVector().multiply(0.1);
        vector = vector.setY((vector.getY()+0.2) * 1.15);
        item.setVelocity(vector);
        if (isDouble) {
            Entity item2 = location.getWorld().dropItem(location, itemStack);
            item2.setVelocity(vector);
        }
    }

    private void dropVanillaLoot(Player player, VanillaLoot vanillaLoot, Location location, boolean isDouble) {
        ItemStack itemStack;
        itemStack = vanillaLoot.getItemStack();

        if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance){
            ItemStack mcMMOItemStack = McMMOTreasure.getTreasure(player);
            if (mcMMOItemStack != null){
                itemStack = mcMMOItemStack;
            }
        }

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_VANILLA_ITEM, isDouble, itemStack, "vanilla");
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (Competition.currentCompetition != null){
            Competition.currentCompetition.refreshData(player, 0, fishResultEvent.isDouble());
            Competition.currentCompetition.tryAddBossBarToPlayer(player);
        }

        player.giveExp(vanillaLoot.getXp(), true);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
        dropItem(player, location, isDouble, itemStack);
        sendSuccessTitle(player, itemStack);
    }

    private void summonMob(Player player, Loot loot, Location location, Mob mob, double scoreMultiplier) {
        MobInterface mobInterface = plugin.getIntegrationManager().getMobInterface();
        if (mobInterface == null) return;

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CATCH_MOB, false, null, loot.getKey());
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (Competition.currentCompetition != null) {
            float score;
            if (Competition.currentCompetition.getGoal() == CompetitionGoal.MAX_SIZE || Competition.currentCompetition.getGoal() == CompetitionGoal.TOTAL_SIZE) {
                score = 0;
            }
            else score = (float) loot.getScore();

            Competition.currentCompetition.refreshData(player, (float) (score * scoreMultiplier), false);
            Competition.currentCompetition.tryAddBossBarToPlayer(player);
        }

        mobInterface.summon(player.getLocation(), location, mob);
        for (ActionInterface action : loot.getSuccessActions())
            action.doOn(player, null);
        sendSuccessTitle(player, loot.getNick());
    }

    @NotNull
    private Component getTitleComponent(ItemStack itemStack, String text) {
        Component component;
        if (text.contains("{loot}")){
            text = text.replace("{loot}","|");
            if (text.startsWith("|")){
                component = getDisplayName(itemStack).append(MiniMessage.miniMessage().deserialize(AdventureUtil.replaceLegacy(text.substring(1))));
            }
            else if (text.endsWith("|")){
                component = MiniMessage.miniMessage().deserialize(AdventureUtil.replaceLegacy(text.substring(0,text.length() - 1))).append(getDisplayName(itemStack));
            }
            else {
                String[] titleSplit = StringUtils.split(text, "|");
                component = MiniMessage.miniMessage().deserialize(AdventureUtil.replaceLegacy(titleSplit[0])).append(getDisplayName(itemStack)).append(MiniMessage.miniMessage().deserialize(AdventureUtil.replaceLegacy(titleSplit[1])));
            }
        }
        else {
            component = MiniMessage.miniMessage().deserialize(AdventureUtil.replaceLegacy(text));
        }
        return component;
    }

    private void sendSuccessTitle(Player player, String loot) {
        if (!ConfigManager.enableSuccessTitle) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AdventureUtil.playerTitle(
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
            );
        }, 8);
    }

    private void sendSuccessTitle(Player player, ItemStack itemStack) {
        if (!ConfigManager.enableSuccessTitle) return;
        String title = ConfigManager.successTitle[new Random().nextInt(ConfigManager.successTitle.length)];
        Component titleComponent = getTitleComponent(itemStack, title);
        String subTitle = ConfigManager.successSubTitle[new Random().nextInt(ConfigManager.successSubTitle.length)];
        Component subtitleComponent = getTitleComponent(itemStack, subTitle);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AdventureUtil.playerTitle(
                    player,
                    titleComponent,
                    subtitleComponent,
                    ConfigManager.successFadeIn,
                    ConfigManager.successFadeStay,
                    ConfigManager.successFadeOut
            );
        }, 8);
    }

    private void loseDurability(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PlayerInventory inventory = player.getInventory();
            ItemStack mainHand = inventory.getItemInMainHand();
            if (mainHand.getType() == Material.FISHING_ROD){
                plugin.getIntegrationManager().loseCustomDurability(mainHand, player);
            }
            else {
                ItemStack offHand = inventory.getItemInOffHand();
                if (offHand.getType() == Material.FISHING_ROD){
                    plugin.getIntegrationManager().loseCustomDurability(offHand, player);
                }
            }
        }, 1);
    }

    public void fail(Player player, Loot loot, boolean isVanilla) {
        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.FAILURE, false, null, "null");
        Bukkit.getServer().getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (!isVanilla && loot != null) {
            for (ActionInterface action : loot.getFailureActions())
                action.doOn(player, null);
        }

        if (!ConfigManager.enableFailureTitle) return;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            AdventureUtil.playerTitle(
                    player,
                    ConfigManager.failureTitle[new Random().nextInt(ConfigManager.failureTitle.length)],
                    ConfigManager.failureSubTitle[new Random().nextInt(ConfigManager.failureSubTitle.length)],
                    ConfigManager.failureFadeIn,
                    ConfigManager.failureFadeStay,
                    ConfigManager.failureFadeOut
            );
        }, 8);
    }

    public void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        FishingGame fishingGame = fishingPlayerMap.remove(player);
        if (fishingGame != null) {
            Entity entity = event.getCaught();
            if (entity != null && entity.getType() == EntityType.ARMOR_STAND) {
                proceedReelIn(event.getHook().getLocation(), player, fishingGame);
            }
            else {
                fishingGame.cancel();
                nextEffect.remove(player);
                nextLoot.remove(player);
                AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.hookOther);
            }
        }
    }

    public void onFailedAttempt(PlayerFishEvent event) {
        //Empty
    }

    public void onBite(PlayerFishEvent event) {
        if (ConfigManager.disableBar) return;
        if (!ConfigManager.instantBar) return;
        final Player player = event.getPlayer();
        showBar(player);
    }

    public void showBar(Player player) {
        if (fishingPlayerMap.get(player) != null) return;
        Loot loot = nextLoot.get(player);
        if (loot != null) {
            if (loot == Loot.EMPTY) return;
            showFishingBar(player, loot);
        }
    }

    public void onInGround(PlayerFishEvent event) {
        //Empty
    }

    public void onMMOItemsRodCast(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        PlayerInventory inventory = player.getInventory();
        setCustomTag(inventory.getItemInMainHand());
        setCustomTag(inventory.getItemInOffHand());
    }

    private void setCustomTag(ItemStack itemStack) {
        if(itemStack.getType() != Material.FISHING_ROD) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getCompound("CustomFishing") != null) return;
        if (!nbtItem.hasTag("MMOITEMS_ITEM_ID")) return;
        ItemStackUtil.addIdentifier(itemStack, "rod", nbtItem.getString("MMOITEMS_ITEM_ID"));
    }

    public boolean isCoolDown(Player player, long delay) {
        long time = System.currentTimeMillis();
        if (time - (coolDown.getOrDefault(player, time - delay)) < delay) return true;
        coolDown.put(player, time);
        return false;
    }

    private void enchantBonus(Effect initialEffect, ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        for (Map.Entry<Enchantment, Integer> en : enchantments.entrySet()) {
            String key = en.getKey().getKey() + ":" + en.getValue();
            Effect enchantEffect = plugin.getEffectManager().getEnchantEffect(key);
            if (enchantEffect != null) {
                initialEffect.addEffect(enchantEffect);
            }
        }
    }

    public List<Loot> getPossibleLootList(FishingCondition fishingCondition, boolean finder, Collection<Loot> values) {
        List<Loot> available = new ArrayList<>();
        outer:
            for (Loot loot : values) {
                if (finder && !loot.isShowInFinder()) continue;
                RequirementInterface[] requirements = loot.getRequirements();
                if (requirements == null){
                    available.add(loot);
                }
                else {
                    for (RequirementInterface requirement : requirements) {
                        if (!requirement.isConditionMet(fishingCondition)) {
                            continue outer;
                        }
                    }
                    available.add(loot);
                }
            }
        return available;
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

        for (ActionInterface action : totem.getActivatorActions()) {
            action.doOn(player, null);
        }
        for (ActionInterface action : totem.getNearbyActions()) {
            for (Player nearby : coreLoc.getNearbyPlayers(totem.getRadius())) {
                action.doOn(nearby, player);
            }
        }

        Location bottomLoc = coreLoc.clone().subtract(0, totem.getOriginalModel().getCorePos().getY(), 0);
        ActivatedTotem activatedTotem = new ActivatedTotem(bottomLoc, totem, this, direction);
        activatedTotem.runTaskTimer(plugin, 10, 20);
        activeTotemMap.put(LocationUtils.getSimpleLocation(bottomLoc), activatedTotem);
    }

    private void useFinder(Player player) {
        FishingCondition fishingCondition = new FishingCondition(player.getLocation(), player);
        List<Loot> possibleLoots = getPossibleLootList(fishingCondition, true, plugin.getLootManager().getAllLoots());

        FishFinderEvent fishFinderEvent = new FishFinderEvent(player, possibleLoots);
        Bukkit.getPluginManager().callEvent(fishFinderEvent);
        if (fishFinderEvent.isCancelled()) {
            return;
        }

        if (possibleLoots.size() == 0) {
            AdventureUtil.playerMessage(player, MessageManager.prefix + MessageManager.noLoot);
            return;
        }
        StringBuilder stringBuilder = new StringBuilder(MessageManager.prefix + MessageManager.possibleLoots);
        possibleLoots.forEach(loot -> stringBuilder.append(loot.getNick()).append(MessageManager.splitChar));
        AdventureUtil.playerMessage(player, stringBuilder.substring(0, stringBuilder.length() - MessageManager.splitChar.length()));
    }

    private void showFishingBar(Player player, @Nullable Loot loot){
        MiniGameConfig game;
        if (loot != null && loot.getFishingGames() != null) {
            game = loot.getFishingGames()[new Random().nextInt(loot.getFishingGames().length)];
        }
        else {
            game = plugin.getBarMechanicManager().getRandomGame();
        }
        int difficult = game.getRandomDifficulty();
        Effect effect = nextEffect.get(player);
        if (effect != null) {
            difficult += effect.getDifficulty();
        }
        FishHookEvent fishHookEvent = new FishHookEvent(player, difficult);
        Bukkit.getPluginManager().callEvent(fishHookEvent);
        if (fishHookEvent.isCancelled()) {
            return;
        }
        difficult = fishHookEvent.getDifficulty();
        if (difficult < 1) {
            difficult = 1;
        }

        FishingBar fishingBar = game.getRandomBar();
        if (fishingBar instanceof ModeOneBar modeOneBar) {
            ModeOneGame modeOneGame = new ModeOneGame(plugin, this, System.currentTimeMillis() + game.getTime() * 1000L, player, difficult, modeOneBar);
            modeOneGame.runTaskTimer(plugin, 0, 1);
            fishingPlayerMap.put(player, modeOneGame);
        }
        else if (fishingBar instanceof ModeTwoBar modeTwoBar) {
            ModeTwoGame modeTwoGame = new ModeTwoGame(plugin, this, System.currentTimeMillis() + game.getTime() * 1000L, player, difficult, modeTwoBar);
            modeTwoGame.runTaskTimer(plugin, 0, 1);
            fishingPlayerMap.put(player, modeTwoGame);
        }
        else if (fishingBar instanceof ModeThreeBar modeThreeBar) {
            ModeThreeGame modeThreeGame = new ModeThreeGame(plugin, this, System.currentTimeMillis() + game.getTime() * 1000L, player, difficult, modeThreeBar);
            modeThreeGame.runTaskTimer(plugin, 0, 1);
            fishingPlayerMap.put(player, modeThreeGame);
        }
        if (vanillaLoot.get(player) == null && loot != null){
            for (ActionInterface action : loot.getHookActions()) {
                action.doOn(player, null);
            }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, game.getTime() * 20,3));
    }

    @Override
    public void onQuit(Player player) {
        coolDown.remove(player);
        nextLoot.remove(player);
        nextEffect.remove(player);
        vanillaLoot.remove(player);
        BobberCheckTask task = hookCheckTaskMap.remove(player);
        if (task != null) task.stop();
        // prevent bar duplication
        removeBobber(player);
    }

    @Nullable
    public FishingGame getFishingPlayer(Player player) {
        return fishingPlayerMap.get(player);
    }

    public void removeFishingPlayer(Player player) {
        fishingPlayerMap.remove(player);
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

    public void removeTotem(Location location) {
        activeTotemMap.remove(LocationUtils.getSimpleLocation(location));
    }

    public void removePlayerFromLavaFishing(Player player) {
        this.hookCheckTaskMap.remove(player);
    }

    public float getSize(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound fishMeta = nbtItem.getCompound("FishMeta");
        if (fishMeta != null) {
            return fishMeta.getFloat("size");
        }
        return 0;
    }

    public void addTotemBreakDetectToCache(SimpleLocation part, SimpleLocation bottom) {
        breakDetectionMap.put(part, bottom);
    }

    public void removeTotemBreakDetectFromCache(SimpleLocation part) {
        breakDetectionMap.remove(part);
    }

    @Override
    public void onBreakBlock(BlockBreakEvent event) {
        final Block block = event.getBlock();
        SimpleLocation bottomLoc = breakDetectionMap.get(LocationUtils.getSimpleLocation(block.getLocation()));
        if (bottomLoc == null) return;
        ActivatedTotem activatedTotem = activeTotemMap.get(bottomLoc);
        if (activatedTotem == null) return;
        activatedTotem.stop();
    }

    public void removeBobber(Player player) {
        FishHook fishHook = hooks.remove(player);
        if (fishHook != null) {
            fishHook.remove();
        }
    }

    @Nullable
    public FishHook getBobber(Player player) {
        return hooks.get(player);
    }

    @Override
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        ItemStack itemStack = event.getItem();
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        if (nbtCompound == null) return;
        if (!nbtCompound.getString("type").equals("loot")) return;
        String lootKey = nbtCompound.getString("id");
        Loot loot = plugin.getLootManager().getLoot(lootKey);
        if (loot == null) return;
        if (!(loot instanceof DroppedItem droppedItem)) return;
        final Player player = event.getPlayer();
        if (droppedItem.getConsumeActions() != null)
            for (ActionInterface action : droppedItem.getConsumeActions())
                action.doOn(player, null);
    }
}