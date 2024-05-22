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

package net.momirealms.customfishing.bukkit.market;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.api.mechanic.market.MarketManager;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.bukkit.item.BukkitItemFactory;
import net.momirealms.customfishing.bukkit.util.NumberUtils;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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

public class BukkitMarketManager implements MarketManager, Listener {

    private final BukkitCustomFishingPlugin plugin;

    private boolean enable;

    private final HashMap<String, MathValue<Player>> priceMap;
    private String formula;
    private MathValue<Player> earningsLimit;
    private boolean allowItemWithNoPrice;
    private boolean sellFishingBag;

    private String title;
    private String[] layout;
    private final HashMap<Character, CustomFishingItem> decorativeIcons;
    private final ConcurrentHashMap<UUID, MarketGUI> marketGUIMap;

    private char itemSlot;
    private char sellSlot;
    private char sellAllSlot;

    private CustomFishingItem sellIconAllowItem;
    private CustomFishingItem sellIconDenyItem;
    private CustomFishingItem sellIconLimitItem;
    private CustomFishingItem sellAllIconAllowItem;
    private CustomFishingItem sellAllIconDenyItem;
    private CustomFishingItem sellAllIconLimitItem;
    private Action<Player>[] sellDenyActions;
    private Action<Player>[] sellAllowActions;
    private Action<Player>[] sellLimitActions;
    private Action<Player>[] sellAllDenyActions;
    private Action<Player>[] sellAllAllowActions;
    private Action<Player>[] sellAllLimitActions;

    private SchedulerTask resetEarningsTask;
    private int cachedDate;

    public BukkitMarketManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.priceMap = new HashMap<>();
        this.decorativeIcons = new HashMap<>();
        this.marketGUIMap = new ConcurrentHashMap<>();
        this.cachedDate = getRealTimeDate();
    }

    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
        if (!enable) return;
        this.resetEarningsTask = plugin.getScheduler().asyncRepeating(() -> {
            int now = getRealTimeDate();
            if (this.cachedDate != now) {
                this.cachedDate = now;
                for (UserData userData : plugin.getStorageManager().getOnlineUsers()) {
                    userData.earningData().refresh();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private int getRealTimeDate() {
        Calendar calendar = Calendar.getInstance();
        return (calendar.get(Calendar.MONTH) +1) * 100 + calendar.get(Calendar.DATE);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        this.priceMap.clear();
        this.decorativeIcons.clear();
        if (this.resetEarningsTask != null)
            this.resetEarningsTask.cancel();
    }

    // Load configuration from the plugin's config file
    private void loadConfig() {
        YamlDocument config = plugin.getConfigManager().loadConfig("market.yml");
        this.enable = config.getBoolean("enable", true);
        this.formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        if (!this.enable) return;

        // Load various configuration settings
        this.layout = config.getStringList("layout").toArray(new String[0]);
        this.title = config.getString("title", "market.title");
        this.itemSlot = config.getString("item-slot.symbol", "I").charAt(0);
        this.allowItemWithNoPrice = config.getBoolean("item-slot.allow-items-with-no-price", true);

        Section sellAllSection = config.getSection("sell-all-icons");
        if (sellAllSection != null) {
            this.sellAllSlot = sellAllSection.getString("symbol", "S").charAt(0);
            this.sellFishingBag = sellAllSection.getBoolean("fishingbag", true);

            this.sellAllIconAllowItem = plugin.getItemManager().getItemBuilder(sellAllSection.getSection("allow-icon"), "gui", "sell-all");
            this.sellAllIconDenyItem = plugin.getItemManager().getItemBuilder(sellAllSection.getSection("deny-icon"), "gui", "sell-all");
            this.sellAllIconLimitItem = plugin.getItemManager().getItemBuilder(sellAllSection.getSection("limit-icon"), "gui", "sell-all");

            this.sellAllAllowActions = plugin.getActionManager().parseActions(sellAllSection.getSection("allow-icon.action"));
            this.sellAllDenyActions = plugin.getActionManager().parseActions(sellAllSection.getSection("deny-icon.action"));
            this.sellAllLimitActions = plugin.getActionManager().parseActions(sellAllSection.getSection("limit-icon.action"));
        }

        Section sellSection = config.getSection("sell-icons");
        if (sellSection == null) {
            // for old config compatibility
            sellSection = config.getSection("functional-icons");
        }
        if (sellSection != null) {
            this.sellSlot = sellSection.getString("symbol", "B").charAt(0);

            this.sellIconAllowItem = plugin.getItemManager().getItemBuilder(sellSection.getSection("allow-icon"), "gui", "allow");
            this.sellIconDenyItem = plugin.getItemManager().getItemBuilder(sellSection.getSection("deny-icon"), "gui", "deny");
            this.sellIconLimitItem = plugin.getItemManager().getItemBuilder(sellSection.getSection("limit-icon"), "gui", "limit");

            this.sellAllowActions = plugin.getActionManager().parseActions(sellSection.getSection("allow-icon.action"));
            this.sellDenyActions = plugin.getActionManager().parseActions(sellSection.getSection("deny-icon.action"));
            this.sellLimitActions = plugin.getActionManager().parseActions(sellSection.getSection("limit-icon.action"));
        }

        this.earningsLimit = config.getBoolean("limitation.enable", true) ? MathValue.auto(config.getString("limitation.earnings", "10000")) : MathValue.plain(-1);

        // Load item prices from the configuration
        Section priceSection = config.getSection("item-price");
        if (priceSection != null) {
            for (Map.Entry<String, Object> entry : priceSection.getStringRouteMappedValues(false).entrySet()) {
                this.priceMap.put(entry.getKey(), MathValue.auto(entry.getValue()));
            }
        }

        // Load decorative icons from the configuration
        Section decorativeSection = config.getSection("decorative-icons");
        if (decorativeSection != null) {
            for (Map.Entry<String, Object> entry : decorativeSection.getStringRouteMappedValues(false).entrySet()) {
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
        if (!enable) return;
        Optional<UserData> optionalUserData = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
        if (optionalUserData.isEmpty()) {
            plugin.getPluginLogger().warn("Player " + player.getName() + "'s market data is not loaded yet.");
            return;
        }

        MarketGUI gui = new MarketGUI(this, player, optionalUserData.get().earningData());
        gui.addElement(new MarketGUIElement(getItemSlot(), new ItemStack(Material.AIR)));
        gui.addElement(new MarketDynamicGUIElement(getSellSlot(), new ItemStack(Material.AIR)));
        gui.addElement(new MarketDynamicGUIElement(getSellAllSlot(), new ItemStack(Material.AIR)));
        for (Map.Entry<Character, CustomFishingItem> entry : decorativeIcons.entrySet()) {
            gui.addElement(new MarketGUIElement(entry.getKey(), ));
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

        plugin.getScheduler().sync().runLater(gui::refresh, 1, player.getLocation());
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
            data.refresh();

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
                double earningLimit = earningLimit(player);

                Context<Player> context = Context.player(player);
                new Con(, new HashMap<>(Map.of(
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
                            for (Action<Player> action : getSellLimitActions()) {
                                action.trigger(context);
                            }
                        }
                    } else {
                        // Clear items and update earnings
                        gui.clearWorthyItems();
                        data.earnings += worth;
                        playerContext.insertArg("{rest}", NumberUtils.money(earningLimit - data.earnings));
                        playerContext.insertArg("{rest_formatted}", String.format("%.2f", (earningLimit - data.earnings)));
                        if (getSellAllowActions() != null) {
                            for (Action action : getSellAllowActions()) {
                                action.trigger(playerContext);
                            }
                        }
                    }
                } else {
                    // Nothing to sell
                    if (getSellDenyActions() != null) {
                        for (Action action : getSellDenyActions()) {
                            action.trigger(playerContext);
                        }
                    }
                }
            } else if (element.getSymbol() == sellAllSlot) {
                double worth = getInventoryTotalWorth(player.getInventory());
                int amount = getInventorySellAmount(player.getInventory());
                double earningLimit = earningLimit(player);
                if (sellFishingBag() && BukkitCustomFishingPlugin.get().getBagManager().isEnabled()) {
                    Inventory bag = BukkitCustomFishingPlugin.get().getBagManager().getOnlineBagInventory(player.getUniqueId());
                    if (bag != null) {
                        worth += getInventoryTotalWorth(bag);
                        amount += getInventorySellAmount(bag);
                    }
                }
                PlayerContext playerContext = new PlayerContext(player, new HashMap<>(Map.of(
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
                                action.trigger(playerContext);
                            }
                        }
                    } else {
                        // Clear items and update earnings
                        clearWorthyItems(player.getInventory());
                        if (sellFishingBag() && BukkitCustomFishingPlugin.get().getBagManager().isEnabled()) {
                            Inventory bag = BukkitCustomFishingPlugin.get().getBagManager().getOnlineBagInventory(player.getUniqueId());
                            if (bag != null) {
                                clearWorthyItems(bag);
                            }
                        }
                        data.earnings += worth;
                        playerContext.insertArg("{rest}", NumberUtils.money(earningLimit - data.earnings));
                        playerContext.insertArg("{rest_formatted}", String.format("%.2f", (earningLimit - data.earnings)));
                        if (getSellAllAllowActions() != null) {
                            for (Action action : getSellAllAllowActions()) {
                                action.trigger(playerContext);
                            }
                        }
                    }
                } else {
                    // Nothing to sell
                    if (getSellAllDenyActions() != null) {
                        for (Action action : getSellAllDenyActions()) {
                            action.trigger(playerContext);
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
        plugin.getScheduler().sync().runLater(gui::refresh, 1, player.getLocation());
    }

    @Override
    public double getItemPrice(Context<Player> context, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return 0;

        Item<ItemStack> wrapped = ((BukkitItemFactory) plugin.getItemManager().getFactory()).wrap(itemStack);
        double price = (double) wrapped.getTag("Price").orElse(0d);
        if (price != 0) {
            // If a custom price is defined in the ItemStack's NBT data, use it.
            return price * itemStack.getAmount();
        }

        // If no custom price is defined, attempt to fetch the price from a predefined price map.
        String itemID = itemStack.getType().name();
        Optional<Integer> optionalCMD = wrapped.customModelData();
        if (optionalCMD.isPresent()) {
            itemID = itemID + ":" + optionalCMD.get();
        }

        MathValue<Player> formula = priceMap.get(itemID);
        if (formula == null) return 0;

        return formula.evaluate(context) * itemStack.getAmount();
    }

    @Override
    public String getFormula() {
        return "";
    }

    @Override
    public double earningLimit(Context<Player> context) {
        return 0;
    }

    public double getInventoryTotalWorth(Inventory inventory) {
        double total = 0d;
        for (ItemStack itemStack : inventory.getStorageContents()) {
            double price = getItemPrice(itemStack);
            total += price;
        }
        return total;
    }

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
