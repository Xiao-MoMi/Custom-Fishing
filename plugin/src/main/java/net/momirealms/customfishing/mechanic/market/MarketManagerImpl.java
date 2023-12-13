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
import net.objecthunter.exp4j.Expression;
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
    private char functionSlot;
    private BuildableItem functionIconAllowBuilder;
    private BuildableItem functionIconDenyBuilder;
    private BuildableItem functionIconLimitBuilder;
    private Action[] denyActions;
    private Action[] allowActions;
    private Action[] limitActions;
    private String earningLimitExpression;
    private boolean allowItemWithNoPrice;
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
        if (!this.enable) return;

        // Load various configuration settings
        this.layout = config.getStringList("layout").toArray(new String[0]);
        this.title = config.getString("title", "market.title");
        this.formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        this.itemSlot = config.getString("item-slot.symbol", "I").charAt(0);
        this.functionSlot = config.getString("functional-icons.symbol", "B").charAt(0);
        this.functionIconAllowBuilder = plugin.getItemManager().getItemBuilder(config.getConfigurationSection("functional-icons.allow-icon"), "gui", "allow");
        this.functionIconDenyBuilder = plugin.getItemManager().getItemBuilder(config.getConfigurationSection("functional-icons.deny-icon"), "gui", "deny");
        this.functionIconLimitBuilder = plugin.getItemManager().getItemBuilder(config.getConfigurationSection("functional-icons.limit-icon"), "gui", "limit");
        this.allowActions = plugin.getActionManager().getActions(config.getConfigurationSection("functional-icons.allow-icon.action"));
        this.denyActions = plugin.getActionManager().getActions(config.getConfigurationSection("functional-icons.deny-icon.action"));
        this.limitActions = plugin.getActionManager().getActions(config.getConfigurationSection("functional-icons.limit-icon.action"));
        this.earningLimitExpression = config.getBoolean("limitation.enable", true) ? config.getString("limitation.earnings", "10000") : "-1";
        this.allowItemWithNoPrice = config.getBoolean("item-slot.allow-items-with-no-price", true);

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
        gui.addElement(new MarketDynamicGUIElement(getFunctionSlot(), new ItemStack(Material.AIR)));
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

            if (element.getSymbol() == functionSlot) {
                double worth = gui.getTotalWorth();
                int amount = gui.getSoldAmount();
                double earningLimit = getEarningLimit(player);
                Condition condition = new Condition(player, new HashMap<>(Map.of(
                        "{money}", String.format("%.2f", worth)
                        ,"{rest}", String.format("%.2f", (earningLimit - data.earnings))
                        ,"{sold-item-amount}", String.valueOf(amount)
                )));
                if (worth > 0) {
                    if (earningLimit != -1 && (earningLimit - data.earnings) < worth) {
                        // Can't earn more money
                        if (limitActions != null) {
                            for (Action action : limitActions) {
                                action.trigger(condition);
                            }
                        }
                    } else {
                        // Clear items and update earnings
                        gui.clearWorthyItems();
                        data.earnings += worth;
                        condition.insertArg("{rest}", String.format("%.2f", (earningLimit - data.earnings)));
                        if (allowActions != null) {
                            for (Action action : allowActions) {
                                action.trigger(condition);
                            }
                        }
                    }
                } else {
                    // Nothing to sell
                    if (denyActions != null) {
                        for (Action action : denyActions) {
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

    /**
     * Retrieves the current date as an integer in the format MMDD (e.g., September 21 as 0921).
     *
     * @return An integer representing the current date.
     */
    @Override
    public int getCachedDate() {
        return date;
    }

    @Override
    public int getDate() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) +1) * 100 + calendar.get(Calendar.DATE);
    }

    /**
     * Calculates the price of an ItemStack based on custom data or a predefined price map.
     *
     * @param itemStack The ItemStack for which the price is calculated.
     * @return The calculated price of the ItemStack.
     */
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

    /**
     * Retrieves the formula used for calculating prices.
     *
     * @return The pricing formula as a string.
     */
    @Override
    public String getFormula() {
        return formula;
    }

    /**
     * Calculates the price based on a formula with provided variables.
     *
     * @param base  The base value for the formula.
     * @param bonus The bonus value for the formula.
     * @param size  The size value for the formula.
     * @return The calculated price based on the formula and provided variables.
     */
    @Override
    public double getFishPrice(float base, float bonus, float size) {
        Expression expression = new ExpressionBuilder(getFormula())
                .variables("base", "bonus", "size")
                .build()
                .setVariable("base", base)
                .setVariable("bonus", bonus)
                .setVariable("size", size);
        return expression.evaluate();
    }

    /**
     * Gets the character representing the item slot in the MarketGUI.
     *
     * @return The item slot character.
     */
    @Override
    public char getItemSlot() {
        return itemSlot;
    }

    /**
     * Gets the character representing the function slot in the MarketGUI.
     *
     * @return The function slot character.
     */
    @Override
    public char getFunctionSlot() {
        return functionSlot;
    }

    /**
     * Gets the layout of the MarketGUI as an array of strings.
     *
     * @return The layout of the MarketGUI.
     */
    @Override
    public String[] getLayout() {
        return layout;
    }

    /**
     * Gets the title of the MarketGUI.
     *
     * @return The title of the MarketGUI.
    */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * Gets the earning limit
     *
     * @return The earning limit
     */
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

    /**
     * Gets the builder for the function icon representing the limit in the MarketGUI.
     *
     * @return The function icon builder for the limit.
     */
    public BuildableItem getFunctionIconLimitBuilder() {
        return functionIconLimitBuilder;
    }

    /**
     * Gets the builder for the function icon representing allow actions in the MarketGUI.
     *
     * @return The function icon builder for allow actions.
     */
    public BuildableItem getFunctionIconAllowBuilder() {
        return functionIconAllowBuilder;
    }

    /**
     * Gets the builder for the function icon representing deny actions in the MarketGUI.
     *
     * @return The function icon builder for deny actions.
     */
    public BuildableItem getFunctionIconDenyBuilder() {
        return functionIconDenyBuilder;
    }

    /**
     * Is market enabled
     *
     * @return enable or not
     */
    @Override
    public boolean isEnable() {
        return enable;
    }
}
