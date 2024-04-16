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

package net.momirealms.customfishing.mechanic.market;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.data.EarningData;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import net.momirealms.customfishing.api.manager.MarketManager;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.api.scheduler.CancellableTask;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.NumberUtils;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MarketManagerImpl implements MarketManager, Listener {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, Double> priceMap;
    private String[] layout;
    private String title;
    private String formula;
    private final HashMap<Character, BuildableItem> decorativeIcons;
    private char itemSlot;
    private char sellSlot;
    private char sellAllSlot;
    private BuildableItem sellIconAllowBuilder;
    private BuildableItem sellIconDenyBuilder;
    private BuildableItem sellIconLimitBuilder;
    private BuildableItem sellAllIconAllowBuilder;
    private BuildableItem sellAllIconDenyBuilder;
    private BuildableItem sellAllIconLimitBuilder;
    private Action[] sellDenyActions;
    private Action[] sellAllowActions;
    private Action[] sellLimitActions;
    private Action[] sellAllDenyActions;
    private Action[] sellAllAllowActions;
    private Action[] sellAllLimitActions;
    private String earningLimitExpression;
    private boolean allowItemWithNoPrice;
    private boolean sellFishingBag;
    private final ConcurrentHashMap<UUID, MarketGUI> marketGUIMap;
    private boolean enable;
    private CancellableTask resetEarningsTask;
    private int date;

    public MarketManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.priceMap = new HashMap<>();
        this.decorativeIcons = new HashMap<>();
        this.marketGUIMap = new ConcurrentHashMap<>();
        this.date = getDate();
    }

    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        if (!enable) return;
        this.resetEarningsTask = plugin.getScheduler().runTaskAsyncTimer(() -> {
            int now = getDate();
            if (this.date != now) {
                this.date = now;
                for (OnlineUser onlineUser : plugin.getStorageManager().getOnlineUsers()) {
                    onlineUser.getEarningData().date = now;
                    onlineUser.getEarningData().earnings = 0d;
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        this.priceMap.clear();
        this.decorativeIcons.clear();
        if (this.resetEarningsTask != null && !this.resetEarningsTask.isCancelled()) {
            this.resetEarningsTask.cancel();
            this.resetEarningsTask = null;
        }
    }

    public void disable() {
        unload();
    }

    // Load configuration from the plugin's config file
    private void loadConfig() {
        YamlConfiguration config = plugin.getConfig("market.yml");
        this.enable = config.getBoolean("enable", true);
        this.formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        if (!this.enable) return;

        // Load various configuration settings
        this.layout = config.getStringList("layout").toArray(new String[0]);
        this.title = config.getString("title", "market.title");
        this.itemSlot = config.getString("item-slot.symbol", "I").charAt(0);
        this.allowItemWithNoPrice = config.getBoolean("item-slot.allow-items-with-no-price", true);

        ConfigurationSection sellAllSection = config.getConfigurationSection("sell-all-icons");
        if (sellAllSection != null) {
            this.sellAllSlot = sellAllSection.getString("symbol", "S").charAt(0);
            this.sellFishingBag = sellAllSection.getBoolean("fishingbag", true);

            this.sellAllIconAllowBuilder = plugin.getItemManager().getItemBuilder(sellAllSection.getConfigurationSection("allow-icon"), "gui", "sell-all");
            this.sellAllIconDenyBuilder = plugin.getItemManager().getItemBuilder(sellAllSection.getConfigurationSection("deny-icon"), "gui", "sell-all");
            this.sellAllIconLimitBuilder = plugin.getItemManager().getItemBuilder(sellAllSection.getConfigurationSection("limit-icon"), "gui", "sell-all");

            this.sellAllAllowActions = plugin.getActionManager().getActions(sellAllSection.getConfigurationSection("allow-icon.action"));
            this.sellAllDenyActions = plugin.getActionManager().getActions(sellAllSection.getConfigurationSection("deny-icon.action"));
            this.sellAllLimitActions = plugin.getActionManager().getActions(sellAllSection.getConfigurationSection("limit-icon.action"));
        }

        ConfigurationSection sellSection = config.getConfigurationSection("sell-icons");
        if (sellSection == null) {
            // for old config compatibility
            sellSection = config.getConfigurationSection("functional-icons");
        }
        if (sellSection != null) {
            this.sellSlot = sellSection.getString("symbol", "B").charAt(0);

            this.sellIconAllowBuilder = plugin.getItemManager().getItemBuilder(sellSection.getConfigurationSection("allow-icon"), "gui", "allow");
            this.sellIconDenyBuilder = plugin.getItemManager().getItemBuilder(sellSection.getConfigurationSection("deny-icon"), "gui", "deny");
            this.sellIconLimitBuilder = plugin.getItemManager().getItemBuilder(sellSection.getConfigurationSection("limit-icon"), "gui", "limit");

            this.sellAllowActions = plugin.getActionManager().getActions(sellSection.getConfigurationSection("allow-icon.action"));
            this.sellDenyActions = plugin.getActionManager().getActions(sellSection.getConfigurationSection("deny-icon.action"));
            this.sellLimitActions = plugin.getActionManager().getActions(sellSection.getConfigurationSection("limit-icon.action"));
        }

        this.earningLimitExpression = config.getBoolean("limitation.enable", true) ? config.getString("limitation.earnings", "10000") : "-1";

        // Load item prices from the configuration
        ConfigurationSection priceSection = config.getConfigurationSection("item-price");
        if (priceSection != null) {
            for (Map.Entry<String, Object> entry : priceSection.getValues(false).entrySet()) {
                this.priceMap.put(entry.getKey(), ConfigUtils.getDoubleValue(entry.getValue()));
            }
        }

        // Load decorative icons from the configuration
        ConfigurationSection decorativeSection = config.getConfigurationSection("decorative-icons");
        if (decorativeSection != null) {
            for (Map.Entry<String, Object> entry : decorativeSection.getValues(false).entrySet()) {
                if (entry.getValue() instanceof ConfigurationSection innerSection) {
                    char symbol = Objects.requireNonNull(innerSection.getString("symbol")).charAt(0);
                    var builder = plugin.getItemManager().getItemBuilder(innerSection, "gui", entry.getKey());
                    decorativeIcons.put(symbol, builder);
                }
            }
        }
    }

    /**
     * Open the market GUI for a player
     *
     * @param player player
     */
    @Override
    public void openMarketGUI(Player player) {
        if (!isEnable()) return;
        OnlineUser user = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
        if (user == null) {
            LogUtils.warn("Player " + player.getName() + "'s market data is not loaded yet.");
            return;
        }

        MarketGUI gui = new MarketGUI(this, player, user.getEarningData());
        gui.addElement(new MarketGUIElement(getItemSlot(), new ItemStack(Material.AIR)));
        gui.addElement(new MarketDynamicGUIElement(getSellSlot(), new ItemStack(Material.AIR)));
        gui.addElement(new MarketDynamicGUIElement(getSellAllSlot(), new ItemStack(Material.AIR)));
        for (Map.Entry<Character, BuildableItem> entry : decorativeIcons.entrySet()) {
            gui.addElement(new MarketGUIElement(entry.getKey(), entry.getValue().build(player)));
        }
        gui.build().refresh().show(player);
        marketGUIMap.put(player.getUniqueId(), gui);
    }

    /**
     * This method handles the closing of an inventory.
     *
     * @param event The InventoryCloseEvent that triggered this method.
     */
    @EventHandler
    public void onCloseInv(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player))
            return;
        if (!(event.getInventory().getHolder() instanceof MarketGUIHolder))
            return;
        MarketGUI gui = marketGUIMap.remove(player.getUniqueId());
        if (gui != null)
            gui.returnItems();
    }

    /**
     * This method handles a player quitting the server.
     *
     * @param event The PlayerQuitEvent that triggered this method.
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MarketGUI gui = marketGUIMap.remove(event.getPlayer().getUniqueId());
        if (gui != null)
            gui.returnItems();
    }

    /**
     * This method handles dragging items in an inventory.
     *
     * @param event The InventoryDragEvent that triggered this method.
     */
    @EventHandler
    public void onDragInv(InventoryDragEvent event) {
        if (event.isCancelled())
            return;
        Inventory inventory = event.getInventory();
        if (!(inventory.getHolder() instanceof MarketGUIHolder))
            return;
        Player player = (Player) event.getWhoClicked();
        MarketGUI gui = marketGUIMap.get(player.getUniqueId());
        if (gui == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        MarketGUIElement element = gui.getElement(itemSlot);
        if (element == null) {
            event.setCancelled(true);
            return;
        }

        List<Integer> slots = element.getSlots();
        for (int dragSlot : event.getRawSlots()) {
            if (!slots.contains(dragSlot)) {
                event.setCancelled(true);
                return;
            }
        }

        plugin.getScheduler().runTaskSyncLater(gui::refresh, player.getLocation(), 50, TimeUnit.MILLISECONDS);
    }

    /**
     * This method handles inventory click events.
     *
     * @param event The InventoryClickEvent that triggered this method.
     */
    @EventHandler
    public void onClickInv(InventoryClickEvent event) {
        if (event.isCancelled())
            return;

        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null)
            return;

        Player player = (Player) event.getWhoClicked();

        // Check if the clicked inventory is a MarketGUI
        if (!(event.getInventory().getHolder() instanceof MarketGUIHolder))
            return;

        MarketGUI gui = marketGUIMap.get(player.getUniqueId());
        if (gui == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        if (clickedInv != player.getInventory()) {
            EarningData data = gui.getEarningData();
            if (data.date != getCachedDate()) {
                data.date = getCachedDate();
                data.earnings = 0;
            }

            int slot = event.getSlot();
            MarketGUIElement element = gui.getElement(slot);
            if (element == null) {
                event.setCancelled(true);
                return;
            }

            if (element.getSymbol() != itemSlot) {
                event.setCancelled(true);
            }

            if (element.getSymbol() == sellSlot) {
                double worth = gui.getTotalWorthInMarketGUI();
                int amount = gui.getSoldAmount();
                double earningLimit = getEarningLimit(player);
                Condition condition = new Condition(player, new HashMap<>(Map.of(
                        "{money}", NumberUtils.money(worth),
                        "{rest}", NumberUtils.money(earningLimit - data.earnings),
                        "{money_formatted}", String.format("%.2f", worth)
                        ,"{rest_formatted}", String.format("%.2f", (earningLimit - data.earnings))
                        ,"{sold-item-amount}", String.valueOf(amount)
                )));
                if (worth > 0) {
                    if (earningLimit != -1 && (earningLimit - data.earnings) < worth) {
                        // Can't earn more money
                        if (getSellLimitActions() != null) {
                            for (Action action : getSellLimitActions()) {
                                action.trigger(condition);
                            }
                        }
                    } else {
                        // Clear items and update earnings
                        gui.clearWorthyItems();
                        data.earnings += worth;
                        condition.insertArg("{rest}", NumberUtils.money(earningLimit - data.earnings));
                        condition.insertArg("{rest_formatted}", String.format("%.2f", (earningLimit - data.earnings)));
                        if (getSellAllowActions() != null) {
                            for (Action action : getSellAllowActions()) {
                                action.trigger(condition);
                            }
                        }
                    }
                } else {
                    // Nothing to sell
                    if (getSellDenyActions() != null) {
                        for (Action action : getSellDenyActions()) {
                            action.trigger(condition);
                        }
                    }
                }
            } else if (element.getSymbol() == sellAllSlot) {
                double worth = getInventoryTotalWorth(player.getInventory());
                int amount = getInventorySellAmount(player.getInventory());
                double earningLimit = getEarningLimit(player);
                if (sellFishingBag() && CustomFishingPlugin.get().getBagManager().isEnabled()) {
                    Inventory bag = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(player.getUniqueId());
                    if (bag != null) {
                        worth += getInventoryTotalWorth(bag);
                        amount += getInventorySellAmount(bag);
                    }
                }
                Condition condition = new Condition(player, new HashMap<>(Map.of(
                        "{money}", NumberUtils.money(worth),
                        "{rest}", NumberUtils.money(earningLimit - data.earnings),
                        "{money_formatted}", String.format("%.2f", worth)
                        ,"{rest_formatted}", String.format("%.2f", (earningLimit - data.earnings))
                        ,"{sold-item-amount}", String.valueOf(amount)
                )));
                if (worth > 0) {
                    if (earningLimit != -1 && (earningLimit - data.earnings) < worth) {
                        // Can't earn more money
                        if (getSellAllLimitActions() != null) {
                            for (Action action : getSellAllLimitActions()) {
                                action.trigger(condition);
                            }
                        }
                    } else {
                        // Clear items and update earnings
                        clearWorthyItems(player.getInventory());
                        if (sellFishingBag() && CustomFishingPlugin.get().getBagManager().isEnabled()) {
                            Inventory bag = CustomFishingPlugin.get().getBagManager().getOnlineBagInventory(player.getUniqueId());
                            if (bag != null) {
                                clearWorthyItems(bag);
                            }
                        }
                        data.earnings += worth;
                        condition.insertArg("{rest}", NumberUtils.money(earningLimit - data.earnings));
                        condition.insertArg("{rest_formatted}", String.format("%.2f", (earningLimit - data.earnings)));
                        if (getSellAllAllowActions() != null) {
                            for (Action action : getSellAllAllowActions()) {
                                action.trigger(condition);
                            }
                        }
                    }
                } else {
                    // Nothing to sell
                    if (getSellAllDenyActions() != null) {
                        for (Action action : getSellAllDenyActions()) {
                            action.trigger(condition);
                        }
                    }
                }
            }
        } else {
            // Handle interactions with the player's inventory
            ItemStack current = event.getCurrentItem();
            if (!allowItemWithNoPrice) {
                double price = getItemPrice(current);
                if (price <= 0) {
                    event.setCancelled(true);
                    return;
                }
            }

            if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT)
            && (current != null && current.getType() != Material.AIR)) {
                event.setCancelled(true);
                MarketGUIElement element = gui.getElement(itemSlot);
                if (element == null) return;
                for (int slot : element.getSlots()) {
                    ItemStack itemStack = gui.getInventory().getItem(slot);
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (current.getType() == itemStack.getType()
                                && itemStack.getAmount() != itemStack.getType().getMaxStackSize()
                                && current.getItemMeta().equals(itemStack.getItemMeta())
                        ) {
                            int left = itemStack.getType().getMaxStackSize() - itemStack.getAmount();
                            if (current.getAmount() <= left) {
                                itemStack.setAmount(itemStack.getAmount() + current.getAmount());
                                current.setAmount(0);
                                break;
                            } else {
                                current.setAmount(current.getAmount() - left);
                                itemStack.setAmount(itemStack.getType().getMaxStackSize());
                            }
                        }
                    } else {
                        gui.getInventory().setItem(slot, current.clone());
                        current.setAmount(0);
                        break;
                    }
                }
            }
        }

        // Refresh the GUI
        plugin.getScheduler().runTaskSyncLater(gui::refresh, player.getLocation(), 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public int getCachedDate() {
        return date;
    }

    @Override
    public int getDate() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) +1) * 100 + calendar.get(Calendar.DATE);
    }

    @Override
    public double getItemPrice(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return 0;

        NBTItem nbtItem = new NBTItem(itemStack);
        Double price = nbtItem.getDouble("Price");
        if (price != null && price != 0) {
            // If a custom price is defined in the ItemStack's NBT data, use it.
            return price * itemStack.getAmount();
        }

        // If no custom price is defined, attempt to fetch the price from a predefined price map.
        String itemID = itemStack.getType().name();
        if (nbtItem.hasTag("CustomModelData")) {
            itemID = itemID + ":" + nbtItem.getInteger("CustomModelData");
        }

        // Use the price from the price map, or default to 0 if not found.
        return priceMap.getOrDefault(itemID, 0d) * itemStack.getAmount();
    }

    @Override
    public String getFormula() {
        return formula;
    }

    @Override
    public double getFishPrice(Player player, Map<String, String> vars) {
        String temp = PlaceholderManagerImpl.getInstance().parse(player, formula, vars);
        var placeholders = PlaceholderManagerImpl.getInstance().detectPlaceholders(temp);
        for (String placeholder : placeholders) {
            temp = temp.replace(placeholder, "0");
        }
        return new ExpressionBuilder(temp).build().evaluate();
    }

    @Override
    public char getItemSlot() {
        return itemSlot;
    }

    @Override
    public char getSellSlot() {
        return sellSlot;
    }

    @Override
    public char getSellAllSlot() {
        return sellAllSlot;
    }

    @Override
    public String[] getLayout() {
        return layout;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public double getEarningLimit(Player player) {
        return new ExpressionBuilder(
                PlaceholderManagerImpl.getInstance().parse(
                        player,
                        earningLimitExpression,
                        new HashMap<>()
                ))
                .build()
                .evaluate();
    }

    public BuildableItem getSellIconLimitBuilder() {
        return sellIconLimitBuilder;
    }

    public BuildableItem getSellIconAllowBuilder() {
        return sellIconAllowBuilder;
    }

    public BuildableItem getSellIconDenyBuilder() {
        return sellIconDenyBuilder;
    }

    public BuildableItem getSellAllIconAllowBuilder() {
        return sellAllIconAllowBuilder;
    }

    public BuildableItem getSellAllIconDenyBuilder() {
        return sellAllIconDenyBuilder;
    }

    public BuildableItem getSellAllIconLimitBuilder() {
        return sellAllIconLimitBuilder;
    }

    public Action[] getSellDenyActions() {
        return sellDenyActions;
    }

    public Action[] getSellAllowActions() {
        return sellAllowActions;
    }

    public Action[] getSellLimitActions() {
        return sellLimitActions;
    }

    public Action[] getSellAllDenyActions() {
        return sellAllDenyActions;
    }

    public Action[] getSellAllAllowActions() {
        return sellAllAllowActions;
    }

    public Action[] getSellAllLimitActions() {
        return sellAllLimitActions;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public boolean sellFishingBag() {
        return sellFishingBag;
    }

    @Override
    public double getInventoryTotalWorth(Inventory inventory) {
        double total = 0d;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            double price = getItemPrice(itemStack);
            total += price;
        }
        return total;
    }

    @Override
    public int getInventorySellAmount(Inventory inventory) {
        int amount = 0;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            double price = getItemPrice(itemStack);
            if (price > 0 && itemStack != null) {
                amount += itemStack.getAmount();
            }
        }
        return amount;
    }

    public void clearWorthyItems(Inventory inventory) {
        for (ItemStack itemStack : inventory.getStorageContents()) {
            double price = getItemPrice(itemStack);
            if (price > 0 && itemStack != null) {
                itemStack.setAmount(0);
            }
        }
    }
}
