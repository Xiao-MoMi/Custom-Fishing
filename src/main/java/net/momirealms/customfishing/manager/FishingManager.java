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
import net.momirealms.customcrops.config.MainConfig;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.*;
import net.momirealms.customfishing.competition.Competition;
import net.momirealms.customfishing.data.PlayerBagData;
import net.momirealms.customfishing.integration.AntiGriefInterface;
import net.momirealms.customfishing.integration.MobInterface;
import net.momirealms.customfishing.integration.item.McMMOTreasure;
import net.momirealms.customfishing.listener.*;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.action.ActionInterface;
import net.momirealms.customfishing.object.fishing.*;
import net.momirealms.customfishing.object.loot.DroppedItem;
import net.momirealms.customfishing.object.loot.Loot;
import net.momirealms.customfishing.object.loot.Mob;
import net.momirealms.customfishing.object.requirements.RequirementInterface;
import net.momirealms.customfishing.object.totem.ActivatedTotem;
import net.momirealms.customfishing.object.totem.Totem;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.FakeItemUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FishingManager extends Function {

    private final PlayerFishListener playerFishListener;
    private final InteractListener interactListener;
    private PickUpListener pickUpListener;
    private MMOItemsListener mmoItemsListener;
    private JobsRebornXPListener jobsRebornXPListener;
    private final HashMap<Player, Long> coolDown;
    private final HashMap<Player, FishHook> hooksCache;
    private final HashMap<Player, Loot> nextLoot;
    private final HashMap<Player, Bonus> nextBonus;
    private final HashMap<Player, VanillaLoot> vanillaLoot;
    private final ConcurrentHashMap<Player, FishingPlayer> fishingPlayerCache;
    private final ConcurrentHashMap<Location, ActivatedTotem> totemCache;
    private final ConcurrentHashMap<Player, BobberCheckTask> bobberTaskCache;

    public FishingManager() {
        this.playerFishListener = new PlayerFishListener(this);
        this.interactListener = new InteractListener(this);
        this.coolDown = new HashMap<>();
        this.hooksCache = new HashMap<>();
        this.nextLoot = new HashMap<>();
        this.nextBonus = new HashMap<>();
        this.vanillaLoot = new HashMap<>();
        this.fishingPlayerCache = new ConcurrentHashMap<>();
        this.totemCache = new ConcurrentHashMap<>();
        this.bobberTaskCache = new ConcurrentHashMap<>();
        load();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this.playerFishListener, CustomFishing.plugin);
        Bukkit.getPluginManager().registerEvents(this.interactListener, CustomFishing.plugin);
        if (ConfigManager.preventPickUp) {
            this.pickUpListener = new PickUpListener();
            Bukkit.getPluginManager().registerEvents(this.pickUpListener, CustomFishing.plugin);
        }
        if (ConfigManager.convertMMOItems) {
            this.mmoItemsListener = new MMOItemsListener(this);
            Bukkit.getPluginManager().registerEvents(this.mmoItemsListener, CustomFishing.plugin);
        }
        if (ConfigManager.disableJobsXp) {
            this.jobsRebornXPListener = new JobsRebornXPListener();
            Bukkit.getPluginManager().registerEvents(this.jobsRebornXPListener, CustomFishing.plugin);
        }
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this.playerFishListener);
        HandlerList.unregisterAll(this.interactListener);
        if (this.pickUpListener != null) HandlerList.unregisterAll(this.pickUpListener);
        if (this.mmoItemsListener != null) HandlerList.unregisterAll(this.mmoItemsListener);
        if (this.jobsRebornXPListener != null) HandlerList.unregisterAll(this.jobsRebornXPListener);
        for (BobberCheckTask bobberCheckTask : bobberTaskCache.values()) {
            bobberCheckTask.stop();
        }
    }

    public void onFishing(PlayerFishEvent event) {

        final Player player = event.getPlayer();
        final FishHook fishHook = event.getHook();

        hooksCache.put(player, fishHook);
        if (isCoolDown(player, 500)) return;

        Bukkit.getScheduler().runTaskAsynchronously(CustomFishing.plugin, () -> {

            PlayerInventory inventory = player.getInventory();

            boolean noSpecialRod = true;
            boolean noRod = true;
            boolean noBait = true;
            int lureLevel = 0;
            ItemStack baitItem = null;

            Bonus initialBonus = new Bonus();
            initialBonus.setDifficulty(0);
            initialBonus.setDoubleLoot(0);
            initialBonus.setTime(1);
            initialBonus.setScore(1);
            initialBonus.setWeightMD(new HashMap<>());
            initialBonus.setWeightAS(new HashMap<>());

            ItemStack mainHandItem = inventory.getItemInMainHand();
            Material mainHandItemType = mainHandItem.getType();
            if (mainHandItemType != Material.AIR) {
                if (mainHandItemType == Material.FISHING_ROD) {
                    noRod = false;
                    enchantBonus(initialBonus, mainHandItem);
                    lureLevel = mainHandItem.getEnchantmentLevel(Enchantment.LURE);
                }
                NBTItem mainHandNBTItem = new NBTItem(mainHandItem);
                NBTCompound nbtCompound = mainHandNBTItem.getCompound("CustomFishing");
                if (nbtCompound != null) {
                    if (nbtCompound.getString("type").equals("rod")) {
                        Bonus rodBonus = BonusManager.ROD.get(nbtCompound.getString("id"));
                        if (rodBonus != null){
                            initialBonus.addBonus(rodBonus);
                            noSpecialRod = false;
                        }
                    }
                    else if (nbtCompound.getString("type").equals("bait")) {
                        Bonus baitBonus = BonusManager.BAIT.get(nbtCompound.getString("id"));
                        if (baitBonus != null) {
                            initialBonus.addBonus(baitBonus);
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
                    enchantBonus(initialBonus, offHandItem);
                    lureLevel = offHandItem.getEnchantmentLevel(Enchantment.LURE);
                }
                NBTItem offHandNBTItem = new NBTItem(offHandItem);
                NBTCompound nbtCompound = offHandNBTItem.getCompound("CustomFishing");
                if (nbtCompound != null) {
                    if (noBait && nbtCompound.getString("type").equals("bait")) {
                        Bonus baitBonus = BonusManager.BAIT.get(nbtCompound.getString("id"));
                        if (baitBonus != null){
                            initialBonus.addBonus(baitBonus);
                            offHandItem.setAmount(offHandItem.getAmount() - 1);
                            baitItem = offHandItem.clone();
                            noBait = false;
                        }
                    }
                    else if (noSpecialRod && nbtCompound.getString("type").equals("rod")) {
                        Bonus rodBonus = BonusManager.ROD.get(nbtCompound.getString("id"));
                        if (rodBonus != null) {
                            initialBonus.addBonus(rodBonus);
                            noSpecialRod = false;
                        }
                    }
                }
            }

            for (ActivatedTotem activatedTotem : totemCache.values()) {
                if (activatedTotem.getNearbyPlayerSet().contains(player)) {
                    initialBonus.addBonus(activatedTotem.getTotem().getBonus());
                    break;
                }
            }

            if (ConfigManager.enableFishingBag && noBait) {
                PlayerBagData playerBagData = BagDataManager.dataCache.get(player.getUniqueId());
                if (playerBagData != null) {
                    Inventory baitInv = playerBagData.getInventory();
                    for (int i = 0; i < baitInv.getSize(); i++) {
                        ItemStack itemStack = baitInv.getItem(i);
                        if (itemStack == null || itemStack.getType() == Material.AIR) continue;
                        NBTItem nbtItem = new NBTItem(itemStack);
                        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
                        if (cfCompound == null) continue;
                        if (!cfCompound.getString("type").equals("bait")) continue;
                        Bonus baitBonus = BonusManager.BAIT.get(cfCompound.getString("id"));
                        if (baitBonus != null) {
                            initialBonus.addBonus(baitBonus);
                            baitItem = itemStack.clone();
                            itemStack.setAmount(itemStack.getAmount() - 1);
                            break;
                        }
                    }
                }
            }

            RodCastEvent rodCastEvent = new RodCastEvent(player, initialBonus);
            if (rodCastEvent.isCancelled()) {
                event.setCancelled(true);
                return;
            }

            fishHook.setMaxWaitTime((int) (fishHook.getMaxWaitTime() * initialBonus.getTime()));
            fishHook.setMinWaitTime((int) (fishHook.getMinWaitTime() * initialBonus.getTime()));

            nextBonus.put(player, initialBonus);

            if (ConfigManager.needRodToFish && noSpecialRod) {
                nextLoot.put(player, Loot.EMPTY);
                return;
            }

            initialBonus.setHasSpecialRod(!noSpecialRod);

            int entityID = 0;
            if (baitItem != null) {
                baitItem.setAmount(1);
                entityID = new Random().nextInt(100000000);
                try {
                    CustomFishing.protocolManager.sendServerPacket(player, FakeItemUtil.getSpawnPacket(entityID, fishHook.getLocation()));
                    CustomFishing.protocolManager.sendServerPacket(player, FakeItemUtil.getMetaPacket(entityID, baitItem));
                }
                catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }

            BobberCheckTask bobberCheckTask = new BobberCheckTask(player, initialBonus, fishHook, this, lureLevel, entityID);
            bobberCheckTask.runTaskTimer(CustomFishing.plugin, 1, 1);
            bobberTaskCache.put(player, bobberCheckTask);
        });
    }

    public void getNextLoot(Player player, Bonus initialBonus, List<Loot> possibleLoots) {
        List<Loot> availableLoots = new ArrayList<>();
        if (possibleLoots.size() == 0){
            nextLoot.put(player, null);
            return;
        }

        HashMap<String, Integer> as = initialBonus.getWeightAS();
        HashMap<String, Double> md = initialBonus.getWeightMD();

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

        FishingPlayer fishingPlayer = fishingPlayerCache.remove(player);
        if (fishingPlayer == null) {

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
                    showPlayerBar(player, loot);
                }
                // Is vanilla loot
                else {
                    if (ConfigManager.alwaysFishingBar) {
                        event.setCancelled(true);
                        vanillaLoot.put(player, new VanillaLoot(item.getItemStack(), event.getExpToDrop()));
                        showPlayerBar(player, null);
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
                    showPlayerBar(player, loot);
                }
            }
        }
        else {
            item.remove();
            event.setExpToDrop(0);
            proceedReelIn(event, player, fishingPlayer);
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
        Bonus bonus = nextBonus.remove(player);
        if (vanilla != null) {
            dropVanillaLoot(player, vanilla, item.getLocation(), bonus.getDoubleLoot() > Math.random());
            return;
        }
        if (loot instanceof Mob mob) {
            summonMob(player, loot, item.getLocation(), mob, bonus.getScore());
            return;
        }
        if (loot instanceof DroppedItem droppedItem){
            if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance){
                if (dropMcMMOLoot(player, item.getLocation(), bonus.getDoubleLoot() > Math.random())){
                    return;
                }
            }
            dropCustomFishingLoot(player, item.getLocation(), droppedItem, bonus.getDoubleLoot() > Math.random(), bonus.getScore());
        }
    }

    private void noBarLavaReelIn(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        BobberCheckTask bobberCheckTask = bobberTaskCache.remove(player);
        if (bobberCheckTask != null && bobberCheckTask.isHooked()) {
            Loot loot = nextLoot.remove(player);
            VanillaLoot vanilla = vanillaLoot.remove(player);
            Bonus bonus = nextBonus.remove(player);
            if (vanilla != null) {
                dropVanillaLoot(player, vanilla, event.getHook().getLocation(), bonus.getDoubleLoot() > Math.random());
                return;
            }
            if (loot instanceof Mob mob) {
                summonMob(player, loot, event.getHook().getLocation(), mob, bonus.getScore());
                return;
            }
            if (loot instanceof DroppedItem droppedItem) {
                if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance) {
                    if (dropMcMMOLoot(player, event.getHook().getLocation(), bonus.getDoubleLoot() > Math.random())){
                        return;
                    }
                }
                dropCustomFishingLoot(player, event.getHook().getLocation(), droppedItem, bonus.getDoubleLoot() > Math.random(), bonus.getScore());
            }
        }
    }

    private void proceedReelIn(PlayerFishEvent event, Player player, FishingPlayer fishingPlayer) {
        fishingPlayer.cancel();
        Loot loot = nextLoot.remove(player);
        VanillaLoot vanilla = vanillaLoot.remove(player);
        player.removePotionEffect(PotionEffectType.SLOW);
        if (fishingPlayer.isSuccess()) {
            if (ConfigManager.rodLoseDurability) loseDurability(player);
            Location location = event.getHook().getLocation();
            if (location.getBlock().getType() == Material.LAVA) {
                location.add(0,0.3,0);
            }
            if (vanilla != null) {
                dropVanillaLoot(player, vanilla, location, fishingPlayer.isDouble());
                return;
            }
            if (loot instanceof Mob mob) {
                summonMob(player, loot, location, mob, fishingPlayer.getScoreMultiplier());
                return;
            }
            if (loot instanceof DroppedItem droppedItem){
                if (ConfigManager.enableMcMMOLoot && Math.random() < ConfigManager.mcMMOLootChance){
                    if (dropMcMMOLoot(player, location, fishingPlayer.isDouble())){
                        return;
                    }
                }
                dropCustomFishingLoot(player, location, droppedItem, fishingPlayer.isDouble(), fishingPlayer.getScoreMultiplier());
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
        FishingPlayer fishingPlayer = fishingPlayerCache.remove(player);
        if (fishingPlayer != null) {
            proceedReelIn(event, player, fishingPlayer);
            bobberTaskCache.remove(player);
            return;
        }
        //not in fishing
        BobberCheckTask bobberCheckTask = bobberTaskCache.get(player);
        if (bobberCheckTask != null && bobberCheckTask.isHooked()) {
            Loot loot = nextLoot.get(player);
            if (loot == Loot.EMPTY) return;
            showPlayerBar(player, loot);
            event.setCancelled(true);
        }
    }

    private void dropCustomFishingLoot(Player player, Location location, DroppedItem droppedItem, boolean isDouble, double scoreMultiplier) {
        ItemStack drop = getCustomFishingLootItemStack(droppedItem, player);
        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CAUGHT_LOOT, isDouble, drop);
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        Bonus bonus = nextBonus.remove(player);

        if (Competition.currentCompetition != null){
            float score = (float) (droppedItem.getScore() * scoreMultiplier);
            Competition.currentCompetition.refreshData(player, (float) (score * bonus.getScore()), isDouble);
            Competition.currentCompetition.getBossBarManager().tryJoin(player);
        }

        dropItem(player, location, fishResultEvent.isDouble(), drop);
        for (ActionInterface action : droppedItem.getSuccessActions())
            action.doOn(player, null);
        sendSuccessTitle(player, droppedItem.getNick());
    }

    private ItemStack getCustomFishingLootItemStack(DroppedItem droppedItem, Player player) {
        String key = droppedItem.getMaterial();
        ItemStack drop = CustomFishing.plugin.getIntegrationManager().build(key);

        if (drop.getType() != Material.AIR) {
            if (droppedItem.getRandomEnchants() != null)
                ItemStackUtil.addRandomEnchants(drop, droppedItem.getRandomEnchants());
            if (droppedItem.isRandomDurability())
                ItemStackUtil.addRandomDamage(drop);
            if (ConfigManager.preventPickUp)
                ItemStackUtil.addOwner(drop, player.getName());
            ItemStackUtil.addExtraMeta(drop, droppedItem);
            if (ConfigManager.addTagToFish) ItemStackUtil.addIdentifier(drop, "loot", droppedItem.getKey());
        }
        return drop;
    }

    private boolean dropMcMMOLoot(Player player, Location location, boolean isDouble) {
        ItemStack itemStack = McMMOTreasure.getTreasure(player);
        if (itemStack == null) return false;

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CAUGHT_VANILLA, isDouble, itemStack);
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return true;
        }

        nextBonus.remove(player);

        if (Competition.currentCompetition != null){
            Competition.currentCompetition.refreshData(player, 0, isDouble);
            Competition.currentCompetition.getBossBarManager().tryJoin(player);
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
        vector = vector.setY((vector.getY()+0.18) * 1.15);
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

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CAUGHT_VANILLA, isDouble, itemStack);
        Bukkit.getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        nextBonus.remove(player);

        if (Competition.currentCompetition != null){
            Competition.currentCompetition.refreshData(player, 0, isDouble);
            Competition.currentCompetition.getBossBarManager().tryJoin(player);
        }

        player.giveExp(vanillaLoot.getXp(), true);
        AdventureUtil.playerSound(player, Sound.Source.PLAYER, Key.key("minecraft:entity.experience_orb.pickup"), 1, 1);
        dropItem(player, location, isDouble, itemStack);
        sendSuccessTitle(player, itemStack);
    }

    private void summonMob(Player player, Loot loot, Location location, Mob mob, double scoreMultiplier) {
        MobInterface mobInterface = CustomFishing.plugin.getIntegrationManager().getMobInterface();
        if (mobInterface == null) return;

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.CAUGHT_MOB, false, null);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        Bonus bonus = nextBonus.remove(player);

        if (Competition.currentCompetition != null) {
            float score = (float) (loot.getScore() * scoreMultiplier);
            Competition.currentCompetition.refreshData(player, (float) (score * bonus.getScore()), false);
            Competition.currentCompetition.getBossBarManager().tryJoin(player);
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
    }

    private void sendSuccessTitle(Player player, ItemStack itemStack) {
        String title = ConfigManager.successTitle[new Random().nextInt(ConfigManager.successTitle.length)];
        Component titleComponent = getTitleComponent(itemStack, title);
        String subTitle = ConfigManager.successSubTitle[new Random().nextInt(ConfigManager.successSubTitle.length)];
        Component subtitleComponent = getTitleComponent(itemStack, subTitle);
        AdventureUtil.playerTitle(
                player,
                titleComponent,
                subtitleComponent,
                ConfigManager.successFadeIn,
                ConfigManager.successFadeStay,
                ConfigManager.successFadeOut
        );
    }

    private void loseDurability(Player player) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        Bukkit.getScheduler().runTaskLater(CustomFishing.plugin, () -> {
            PlayerInventory inventory = player.getInventory();
            ItemStack mainHand = inventory.getItemInMainHand();
            if (mainHand.getType() == Material.FISHING_ROD){
                setDurability(mainHand);
            }
            else {
                ItemStack offHand = inventory.getItemInOffHand();
                if (offHand.getType() == Material.FISHING_ROD){
                    setDurability(offHand);
                }
            }
        }, 1);
    }

    private void setDurability(ItemStack rod) {
        Damageable damageable = (Damageable) rod.getItemMeta();
        if (damageable.isUnbreakable()) return;
        Enchantment enchantment = Enchantment.DURABILITY;
        if (Math.random() < (1 / (double) (damageable.getEnchantLevel(enchantment) + 1))){
            damageable.setDamage(damageable.getDamage() + 1);
            Bukkit.getScheduler().runTaskLater(CustomFishing.plugin, () -> rod.setItemMeta(damageable),1);
        }
    }

    private void fail(Player player, Loot loot, boolean isVanilla) {

        FishResultEvent fishResultEvent = new FishResultEvent(player, FishResult.FAILURE, false, null);
        Bukkit.getServer().getPluginManager().callEvent(fishResultEvent);
        if (fishResultEvent.isCancelled()) {
            return;
        }

        if (!isVanilla && loot != null){
            for (ActionInterface action : loot.getFailureActions())
                action.doOn(player, null);
        }

        nextBonus.remove(player);

        AdventureUtil.playerTitle(
                player,
                ConfigManager.failureTitle[new Random().nextInt(ConfigManager.failureTitle.length)],
                ConfigManager.failureSubTitle[new Random().nextInt(ConfigManager.failureSubTitle.length)],
                ConfigManager.failureFadeIn,
                ConfigManager.failureFadeStay,
                ConfigManager.failureFadeOut
        );
    }

    public void onCaughtEntity(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        FishingPlayer fishingPlayer = fishingPlayerCache.remove(player);
        if (fishingPlayer != null) {
            Entity entity = event.getCaught();
            if (entity != null && entity.getType() == EntityType.ARMOR_STAND) {
                proceedReelIn(event, player, fishingPlayer);
            }
            else {
                fishingPlayer.cancel();
                nextBonus.remove(player);
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
        if (fishingPlayerCache.get(player) != null) return;
        Loot loot = nextLoot.get(player);
        if (loot != null) {
            if (loot == Loot.EMPTY) return;
            showPlayerBar(player, loot);
        }
    }

    public void onInGround(PlayerFishEvent event) {
        //Empty
    }

    public void onMMOItemsRodCast(PlayerFishEvent event) {
        final Player player = event.getPlayer();
        if (isCoolDown(player, 5000)) return;
        PlayerInventory inventory = player.getInventory();
        setCustomTag(inventory.getItemInMainHand());
        setCustomTag(inventory.getItemInOffHand());
    }

    private void setCustomTag(ItemStack itemStack) {
        if(itemStack.getType() != Material.FISHING_ROD) return;
        NBTItem nbtItem = new NBTItem(itemStack);
        if (nbtItem.getCompound("CustomFishing") != null) return;
        if (!nbtItem.hasKey("MMOITEMS_ITEM_ID")) return;
        ItemStackUtil.addIdentifier(itemStack, "rod", nbtItem.getString("MMOITEMS_ITEM_ID"));
    }

    public boolean isCoolDown(Player player, long delay) {
        long time = System.currentTimeMillis();
        if (time - (coolDown.getOrDefault(player, time - delay)) < delay) return true;
        coolDown.put(player, time);
        return false;
    }

    private void enchantBonus(Bonus initialBonus, ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        for (Map.Entry<Enchantment, Integer> en : enchantments.entrySet()) {
            String key = en.getKey().getKey() + ":" + en.getValue();
            Bonus enchantBonus = BonusManager.ENCHANTS.get(key);
            if (enchantBonus != null) {
                initialBonus.addBonus(enchantBonus);
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
                    for (RequirementInterface requirement : requirements){
                        if (!requirement.isConditionMet(fishingCondition)){
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
        Totem totem = TotemManager.TOTEMS.get(totemID);
        if (totem == null) return;
        if (isCoolDown(player, 1000)) return;
        String blockID = CustomFishing.plugin.getIntegrationManager().getBlockInterface().getID(block);
        if (blockID == null) return;
        List<Totem> totemList = TotemManager.CORES.get(blockID);
        if (totemList == null || !totemList.contains(totem)) return;
        Location coreLoc = block.getLocation();
        int type = CustomFishing.plugin.getTotemManager().checkLocationModel(totem.getOriginalModel(), coreLoc);
        if (type == 0) return;

        if (!AntiGriefInterface.testBreak(player, coreLoc)) return;
        TotemActivationEvent totemActivationEvent = new TotemActivationEvent(player, coreLoc, totem);
        Bukkit.getPluginManager().callEvent(totemActivationEvent);
        if (totemActivationEvent.isCancelled()) {
            return;
        }

        if (totemCache.get(coreLoc) != null) {
            totemCache.get(coreLoc).stop();
        }

        CustomFishing.plugin.getTotemManager().removeModel(totem.getFinalModel(), coreLoc, type);
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
        ActivatedTotem activatedTotem = new ActivatedTotem(bottomLoc, totem, this);
        activatedTotem.runTaskTimer(CustomFishing.plugin, 10, 20);
        totemCache.put(bottomLoc, activatedTotem);
    }

    private void useFinder(Player player) {
        FishingCondition fishingCondition = new FishingCondition(player.getLocation(), player);
        List<Loot> possibleLoots = getPossibleLootList(fishingCondition, true, LootManager.WATERLOOTS.values());
        possibleLoots.addAll(getPossibleLootList(fishingCondition, true, LootManager.LAVALOOTS.values()));

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

    private void showPlayerBar(Player player, @Nullable Loot loot){

        Layout layout;
        if (loot != null && loot.getLayout() != null){
            layout = loot.getLayout()[new Random().nextInt(loot.getLayout().length)];
        }
        else {
            layout = (Layout) LayoutManager.LAYOUTS.values().stream().toArray()[new Random().nextInt(LayoutManager.LAYOUTS.values().size())];
        }

        int speed;
        int timer;
        int time;
        if (loot != null){
            speed = loot.getDifficulty().speed();
            timer = loot.getDifficulty().timer();
            time = loot.getTime();
        }
        else {
            speed = new Random().nextInt(5);
            time = 10000;
            timer = 1;
        }

        Bonus bonus = nextBonus.get(player);
        boolean isDouble = false;
        double scoreMultiplier = 0;
        if (bonus != null) {
            speed += bonus.getDifficulty();
            isDouble = Math.random() < bonus.getDoubleLoot();
            scoreMultiplier = bonus.getScore() + 1;
        }

        if (speed < 1){
            speed = 1;
        }

        Difficulty difficult = new Difficulty(timer, speed);

        FishHookEvent fishHookEvent = new FishHookEvent(player, difficult);
        Bukkit.getPluginManager().callEvent(fishHookEvent);
        if (fishHookEvent.isCancelled()) {
            return;
        }

        FishingPlayer fishingPlayer = new FishingPlayer(System.currentTimeMillis() + time, player, layout, difficult, this, isDouble, scoreMultiplier);
        fishingPlayer.runTaskTimerAsynchronously(CustomFishing.plugin, 0, 1);
        fishingPlayerCache.put(player, fishingPlayer);

        if (vanillaLoot.get(player) == null && loot != null){
            for (ActionInterface action : loot.getHookActions()) {
                action.doOn(player, null);
            }
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time/50,3));
    }

    @Override
    public void onQuit(Player player) {
        coolDown.remove(player);
        nextLoot.remove(player);
        nextBonus.remove(player);
        vanillaLoot.remove(player);
        BobberCheckTask task = bobberTaskCache.remove(player);
        if (task != null) task.stop();
        // prevent bar duplication
        FishHook fishHook = hooksCache.remove(player);
        if (fishHook != null) fishHook.remove();
    }

    @Nullable
    public FishingPlayer getFishingPlayer(Player player) {
        return fishingPlayerCache.get(player);
    }

    public void removeFishingPlayer(Player player) {
        fishingPlayerCache.remove(player);
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
        totemCache.remove(location);
    }

    public void addPlayerToLavaFishing(Player player, BobberCheckTask task) {
        this.bobberTaskCache.put(player, task);
    }

    public void removePlayerFromLavaFishing(Player player) {
        this.bobberTaskCache.remove(player);
    }
}
