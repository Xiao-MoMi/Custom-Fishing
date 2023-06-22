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
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.api.event.SellFishEvent;
import net.momirealms.customfishing.data.PlayerSellData;
import net.momirealms.customfishing.fishing.loot.Item;
import net.momirealms.customfishing.listener.InventoryListener;
import net.momirealms.customfishing.listener.JoinQuitListener;
import net.momirealms.customfishing.listener.WindowPacketListener;
import net.momirealms.customfishing.object.InventoryFunction;
import net.momirealms.customfishing.util.AdventureUtils;
import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.InventoryUtils;
import net.momirealms.customfishing.util.ItemStackUtils;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class SellManager extends InventoryFunction {

    private final WindowPacketListener windowPacketListener;
    private final InventoryListener inventoryListener;
    private final JoinQuitListener joinQuitListener;
    private final CustomFishing plugin;
    private String formula;
    private String title;
    private int guiSize;
    private String msgNotification;
    private String actionbarNotification;
    private String titleNotification;
    private String subtitleNotification;
    private int titleIn;
    private int titleStay;
    private int titleOut;
    private String[] commands;
    private Item sellIcon;
    private Item denyIcon;
    private Key closeKey;
    private Key openKey;
    private Key successKey;
    private Key denyKey;
    private Sound.Source soundSource;
    private HashMap<Integer, ItemStack> guiItems;
    private HashSet<Integer> functionIconSlots;
    private HashMap<String, Float> customItemPrices = new HashMap<>();
    private boolean sellLimitation;
    private int upperLimit;
    private final ConcurrentHashMap<UUID, PlayerSellData> sellDataMap;

    public SellManager(CustomFishing plugin) {
        super();
        this.plugin = plugin;
        this.windowPacketListener = new WindowPacketListener(this);
        this.inventoryListener = new InventoryListener(this);
        this.joinQuitListener = new JoinQuitListener(this);
        this.sellDataMap = new ConcurrentHashMap<>();
    }

    @Override
    public void load() {
        functionIconSlots = new HashSet<>();
        guiItems = new HashMap<>();
        customItemPrices = new HashMap<>();
        loadConfig();
        CustomFishing.getProtocolManager().addPacketListener(windowPacketListener);
        Bukkit.getPluginManager().registerEvents(inventoryListener, plugin);
        Bukkit.getPluginManager().registerEvents(joinQuitListener, plugin);
    }

    @Override
    public void unload() {
        CustomFishing.getProtocolManager().removePacketListener(windowPacketListener);
        HandlerList.unregisterAll(inventoryListener);
        HandlerList.unregisterAll(joinQuitListener);
        msgNotification = null;
        actionbarNotification = null;
        commands = null;
        titleNotification = null;
    }

    @Override
    public void disable() {
        unload();
        plugin.getDataManager().getDataStorageInterface().saveSellData(sellDataMap.entrySet(), true);
        sellDataMap.clear();
    }

    @Override
    public void onQuit(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerSellData sellData = sellDataMap.remove(uuid);
        if (sellData == null) return;
        plugin.getScheduler().runTaskAsync(() -> plugin.getDataManager().getDataStorageInterface().saveSellData(uuid, sellData, true));
    }

    @Override
    public void onJoin(Player player) {
        plugin.getScheduler().runTaskAsyncLater(() -> joinReadData(player, false), 1, TimeUnit.SECONDS);
    }

    public void joinReadData(Player player, boolean force) {
        if (player == null || !player.isOnline()) return;
        PlayerSellData sellData = plugin.getDataManager().getDataStorageInterface().loadSellData(player.getUniqueId(), force);
        if (sellData != null) {
            sellDataMap.put(player.getUniqueId(), sellData);
        } else if (!force) {
            if (checkTriedTimes(player.getUniqueId())) {
                plugin.getScheduler().runTaskAsyncLater(() -> joinReadData(player, false), 2500, TimeUnit.MILLISECONDS);
            } else {
                plugin.getScheduler().runTaskAsyncLater(() -> joinReadData(player, true), 2500, TimeUnit.MILLISECONDS);
            }
        }
    }

    private void loadConfig() {
        YamlConfiguration config = ConfigUtils.getConfig("sell-fish.yml");
        formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        sellLimitation = config.getBoolean("sell-limitation.enable", false);
        upperLimit = config.getInt("sell-limitation.upper-limit", 10000);
        title = config.getString("container-title");
        guiSize = config.getInt("rows") * 9;
        setSounds(config);
        setActions(config);
        setIcons(config);
        ConfigurationSection configurationSection = config.getConfigurationSection("item-price");
        if (configurationSection == null) {
            configurationSection = config.getConfigurationSection("vanilla-item-price");
        }
        if (configurationSection != null) {
            for (String vanilla : configurationSection.getKeys(false)) {
                customItemPrices.put(vanilla.toUpperCase(Locale.ENGLISH), (float) configurationSection.getDouble(vanilla));
            }
        }
    }

    @SuppressWarnings("all")
    private void setSounds(ConfigurationSection config) {
        openKey = config.contains("sounds.open") ? Key.key(config.getString("sounds.open")) : null;
        closeKey = config.contains("sounds.close") ? Key.key(config.getString("sounds.close")) : null;
        successKey = config.contains("sounds.success") ? Key.key(config.getString("sounds.success")) : null;
        denyKey = config.contains("sounds.deny") ? Key.key(config.getString("sounds.deny")) : null;
        soundSource = Sound.Source.valueOf(config.getString("sounds.type","player").toUpperCase(Locale.ENGLISH));
    }

    private void setActions(ConfigurationSection config) {
        if (config.getBoolean("actions.message.enable", false)) {
            msgNotification = config.getString("actions.message.text");
        }
        if (config.getBoolean("actions.actionbar.enable", false)) {
            actionbarNotification = config.getString("actions.actionbar.text");
        }
        if (config.getBoolean("actions.title.enable", false)) {
            titleNotification = config.getString("actions.title.title");
            subtitleNotification = config.getString("actions.title.subtitle");
            titleIn = config.getInt("actions.title.in");
            titleStay = config.getInt("actions.title.stay");
            titleOut = config.getInt("actions.title.out");
        }
        if (config.getBoolean("actions.commands.enable")) {
            commands = config.getStringList("actions.commands.value").toArray(new String[0]);
        }
    }

    private void setIcons(ConfigurationSection config) {
        if (config.contains("decorative-icons")){
            ConfigurationSection dec_section = config.getConfigurationSection("decorative-icons");
            if (dec_section != null) {
                for (String key : dec_section.getKeys(false)) {
                    ConfigurationSection item_section = dec_section.getConfigurationSection(key);
                    if (item_section == null) continue;
                    Item item = new Item(item_section, key);
                    ItemStack itemStack = ItemStackUtils.getFromItem(item);
                    if (item_section.contains("slots")) {
                        for (int slot : item_section.getIntegerList("slots")) {
                            guiItems.put(slot - 1, itemStack);
                        }
                    }
                }
            }
        }
        ConfigurationSection sellIconSection = config.getConfigurationSection("functional-icons.sell");
        if (sellIconSection != null) {
            sellIcon = new Item(sellIconSection, "sellIcon");
        } else {
            AdventureUtils.consoleMessage("<red>[CustomFishing] Sell icon is missing");
        }
        ConfigurationSection denyIconSection = config.getConfigurationSection("functional-icons.deny");
        if (denyIconSection != null) {
            denyIcon = new Item(denyIconSection, "denyIcon");
        } else {
            AdventureUtils.consoleMessage("<red>[CustomFishing] Deny icon is missing");
        }
        for (int slot : config.getIntegerList("functional-icons.slots")) {
            guiItems.put(slot - 1, ItemStackUtils.getFromItem(sellIcon));
            functionIconSlots.add(slot - 1);
        }
    }

    public void openGuiForPlayer(Player player) {
        player.closeInventory();
        if (!sellDataMap.containsKey(player.getUniqueId())) {
            AdventureUtils.consoleMessage("<red>Sell cache is not loaded for player " + player.getName());
            return;
        }
        SellGUI sellGUI = new SellGUI(player);
        sellGUI.open();
    }

    @Override
    public void onClickInventory(InventoryClickEvent event) {
        final Player player = (Player) event.getView().getPlayer();
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof SellGUI)) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;
        if (clickedInventory == player.getInventory()) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                int empty_slot = getEmptySlot(inventory);
                if (empty_slot == -1) return;
                ItemStack clicked = event.getCurrentItem();
                if (clicked == null || clicked.getType() == Material.AIR) return;
                inventory.setItem(empty_slot, clicked.clone());
                clicked.setAmount(0);
            }
        } else {
            int clickedSlot = event.getSlot();
            if (guiItems.containsKey(clickedSlot)) {
                event.setCancelled(true);
            }
            if (functionIconSlots.contains(clickedSlot)) {
                List<ItemStack> playerItems = getPlayerItems(inventory);
                float totalPrice = getTotalPrice(playerItems);
                if (totalPrice > 0) {
                    PlayerSellData sellData = sellDataMap.get(player.getUniqueId());

                    if (sellData == null) {
                        inventory.close();
                        AdventureUtils.playerMessage(player, MessageManager.prefix + "Your data is not loaded! Try to rejoin the server");
                        AdventureUtils.consoleMessage("<red>[CustomFishing] Unexpected issue, " + player.getName() + "'s sell-cache is not loaded!");
                        if (denyKey != null) AdventureUtils.playerSound(player, soundSource, denyKey, 1, 1);
                        return;
                    }

                    Calendar calendar = Calendar.getInstance();
                    int currentDate = (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DATE);
                    if (currentDate != sellData.getDate()) {
                        sellData.setDate(currentDate);
                        sellData.setMoney(0);
                    }

                    double sell = sellData.getMoney();

                    if (sellLimitation && sell + totalPrice > upperLimit) {
                        inventory.close();
                        AdventureUtils.playerMessage(player, MessageManager.prefix + MessageManager.reachSellLimit);
                        if (denyKey != null) AdventureUtils.playerSound(player, soundSource, denyKey, 1, 1);
                        return;
                    }

                    SellFishEvent sellFishEvent = new SellFishEvent(player, totalPrice);
                    Bukkit.getPluginManager().callEvent(sellFishEvent);
                    if (sellFishEvent.isCancelled()) {
                        return;
                    }

                    for (ItemStack playerItem : playerItems) {
                        if (playerItem == null || playerItem.getType() == Material.AIR) continue;
                        if (getSingleItemPrice(playerItem) == 0) continue;
                        playerItem.setAmount(0);
                    }

                    sellData.setMoney(sellFishEvent.getMoney() + sell);
                    doActions(player, sellFishEvent.getMoney(), upperLimit - sell - sellFishEvent.getMoney());
                    inventory.close();
                } else {
                    for (int slot : functionIconSlots) {
                        inventory.setItem(slot, ItemStackUtils.getFromItem(denyIcon));
                    }
                    if (denyKey != null) AdventureUtils.playerSound(player, soundSource, denyKey, 1, 1);
                }
            }
        }

        plugin.getScheduler().runTaskAsyncLater(() -> {
            ItemStack icon = ItemStackUtils.getFromItem(sellIcon.cloneWithPrice(getTotalPrice(getPlayerItems(inventory))));
            for (int slot : functionIconSlots) {
                inventory.setItem(slot, icon);
            }
        }, 25, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDragInventory(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof SellGUI)) return;
        for (int i : event.getRawSlots()) {
            if (guiItems.containsKey(i)) {
                event.setCancelled(true);
                return;
            }
        }
        plugin.getScheduler().runTaskAsync(() -> {
            ItemStack icon = ItemStackUtils.getFromItem(sellIcon.cloneWithPrice(getTotalPrice(getPlayerItems(inventory))));
            for (int slot : functionIconSlots) {
                inventory.setItem(slot, icon);
            }
        });
    }

    @Override
    public void onCloseInventory(InventoryCloseEvent event) {
        final Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof SellGUI)) return;
        returnItems(getPlayerItems(inventory), player);
        if (closeKey != null) AdventureUtils.playerSound(player, soundSource, closeKey, 1, 1);
    }

    private List<ItemStack> getPlayerItems(Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (int i = 0; i < guiSize; i++) {
            if (guiItems.containsKey(i)) continue;
            items.add(inventory.getItem(i));
        }
        return items;
    }

    private int getEmptySlot(Inventory inventory) {
        for (int i = 0; i < guiSize; i++) {
            if (guiItems.containsKey(i)) continue;
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }

    private void returnItems(List<ItemStack> itemStacks, Player player){
        PlayerInventory inventory = player.getInventory();
        for (ItemStack stack : itemStacks) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            if (hasEmptySlot(inventory)) inventory.addItem(stack);
            else player.getLocation().getWorld().dropItemNaturally(player.getLocation(), stack);
        }
    }

    private boolean hasEmptySlot(PlayerInventory inventory) {
        for (ItemStack itemStack : inventory.getStorageContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) return true;
        }
        return false;
    }

    private float getTotalPrice(List<ItemStack> itemStacks){
        float totalPrice = 0;
        for (ItemStack stack : itemStacks) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            float price = getSingleItemPrice(stack);
            price *= stack.getAmount();
            totalPrice += price;
        }
        return totalPrice;
    }

    public float getCFFishPrice(NBTItem cfFish) {
        NBTCompound fishMeta = cfFish.getCompound("FishMeta");
        if (fishMeta != null) {
            float base = fishMeta.getFloat("base");
            float bonus = fishMeta.getFloat("bonus");
            float size = fishMeta.getFloat("size");
            Expression expression = new ExpressionBuilder(formula)
                    .variables("base", "bonus","size")
                    .build()
                    .setVariable("base", base)
                    .setVariable("bonus", bonus)
                    .setVariable("size", size);
            return  (float) expression.evaluate();
        }
        return 0;
    }

    public float getSingleItemPrice(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound fishMeta = nbtItem.getCompound("FishMeta");
        float price = 0;
        if (fishMeta != null) {
            float base = fishMeta.getFloat("base");
            float bonus = fishMeta.getFloat("bonus");
            float size = fishMeta.getFloat("size");
            Expression expression = new ExpressionBuilder(formula)
                    .variables("base", "bonus","size")
                    .build()
                    .setVariable("base", base)
                    .setVariable("bonus", bonus)
                    .setVariable("size", size);
            price = (float) expression.evaluate();
        }
        Double money = Optional.ofNullable(nbtItem.getDouble("Price")).orElse(0d);
        price += money;
        if (price == 0) {
            String type = itemStack.getType().name();
            if (nbtItem.hasTag("CustomModelData")) type = type + ":" + nbtItem.getInteger("CustomModelData");
            price = Optional.ofNullable(customItemPrices.get(type)).orElse(0f);
        }
        return price;
    }

    private void doActions(Player player, float earnings, double remains) {
        if (titleNotification != null) AdventureUtils.playerTitle(player, titleNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", sellLimitation ? String.format("%.2f", remains) : "unlimited"), subtitleNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", sellLimitation ? String.format("%.2f", remains) : "unlimited"), titleIn * 50, titleStay * 50, titleOut * 50);
        if (msgNotification != null) AdventureUtils.playerMessage(player, msgNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", sellLimitation ? String.format("%.2f", remains) : "unlimited"));
        if (actionbarNotification != null) AdventureUtils.playerActionbar(player, actionbarNotification.replace("{money}", String.format("%.2f", earnings)).replace("{remains}", sellLimitation ? String.format("%.2f", remains) : "unlimited"));
        if (ConfigManager.logEarning) AdventureUtils.consoleMessage("[CustomFishing] Log: " + player.getName() + " earns " + String.format("%.2f", earnings) + " from selling fish");
        if (successKey != null) AdventureUtils.playerSound(player, soundSource, successKey, 1, 1);
        if (plugin.getIntegrationManager().getVaultHook() != null) plugin.getIntegrationManager().getVaultHook().getEconomy().depositPlayer(player, earnings);
        if (commands != null)
            for (String cmd : commands)
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{player}", player.getName()).replace("{money}", String.format("%.2f", earnings)).replace("{remains}", sellLimitation ? String.format("%.2f", remains) : "unlimited"));
    }

    public double getTodayEarning(Player player) {
        PlayerSellData playerSellData = sellDataMap.get(player.getUniqueId());
        if (playerSellData == null) return 0d;
        Calendar calendar = Calendar.getInstance();
        int currentDate = (calendar.get(Calendar.MONTH) + 1) * 100 + calendar.get(Calendar.DATE);
        if (currentDate != playerSellData.getDate()) {
            playerSellData.setDate(currentDate);
            playerSellData.setMoney(0);
        }
        return playerSellData.getMoney();
    }

    public class SellGUI implements InventoryHolder {

        private final Inventory inventory;
        private final Player player;

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }

        public SellGUI(Player player) {
            this.player = player;
            this.inventory = InventoryUtils.createInventory(this, guiSize, plugin.getIntegrationManager().getPlaceholderManager().parse(player, title));
        }

        public void open() {
            for (Map.Entry<Integer, ItemStack> entry : guiItems.entrySet()) {
                inventory.setItem(entry.getKey(), entry.getValue());
            }
            for (int slot : functionIconSlots) {
                inventory.setItem(slot, ItemStackUtils.getFromItem(sellIcon.cloneWithPrice(getTotalPrice(getPlayerItems(inventory)))));
            }
            if (openKey != null) AdventureUtils.playerSound(player, soundSource, openKey, 1, 1);
            player.openInventory(inventory);
        }
    }
}
