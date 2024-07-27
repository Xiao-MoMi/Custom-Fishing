/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.config.SingleItemParser;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.api.mechanic.market.MarketManager;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.bukkit.config.BukkitConfigManager;
import net.momirealms.customfishing.bukkit.item.BukkitItemFactory;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.plugin.scheduler.SchedulerTask;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("DuplicatedCode")
public class BukkitMarketManager implements MarketManager, Listener {

    private final BukkitCustomFishingPlugin plugin;

    private final HashMap<String, MathValue<Player>> priceMap;
    private String formula;
    private MathValue<Player> earningsLimit;
    private boolean allowItemWithNoPrice;
    protected boolean sellFishingBag;

    protected TextValue<Player> title;
    protected String[] layout;
    protected final HashMap<Character, CustomFishingItem> decorativeIcons;
    protected final ConcurrentHashMap<UUID, MarketGUI> marketGUICache;

    protected char itemSlot;
    protected char sellSlot;
    protected char sellAllSlot;

    protected CustomFishingItem sellIconAllowItem;
    protected CustomFishingItem sellIconDenyItem;
    protected CustomFishingItem sellIconLimitItem;
    protected CustomFishingItem sellAllIconAllowItem;
    protected CustomFishingItem sellAllIconDenyItem;
    protected CustomFishingItem sellAllIconLimitItem;
    protected Action<Player>[] sellDenyActions;
    protected Action<Player>[] sellAllowActions;
    protected Action<Player>[] sellLimitActions;
    protected Action<Player>[] sellAllDenyActions;
    protected Action<Player>[] sellAllAllowActions;
    protected Action<Player>[] sellAllLimitActions;

    private SchedulerTask resetEarningsTask;
    private int cachedDate;

    private boolean allowBundle;
    private boolean allowShulkerBox;

    public BukkitMarketManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.priceMap = new HashMap<>();
        this.decorativeIcons = new HashMap<>();
        this.marketGUICache = new ConcurrentHashMap<>();
        this.cachedDate = getRealTimeDate();
    }

    @Override
    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
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

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        this.priceMap.clear();
        this.decorativeIcons.clear();
        if (this.resetEarningsTask != null)
            this.resetEarningsTask.cancel();
    }

    private void loadConfig() {
        Section config = BukkitConfigManager.getMainConfig().getSection("mechanics.market");

        this.formula = config.getString("price-formula", "{base} + {bonus} * {size}");
        this.layout = config.getStringList("layout").toArray(new String[0]);
        this.title = TextValue.auto(config.getString("title", "market.title"));
        this.itemSlot = config.getString("item-slot.symbol", "I").charAt(0);
        this.allowItemWithNoPrice = config.getBoolean("item-slot.allow-items-with-no-price", true);
        this.allowBundle = config.getBoolean("allow-bundle", true);
        this.allowShulkerBox = config.getBoolean("allow-shulker-box", true);

        Section sellAllSection = config.getSection("sell-all-icons");
        if (sellAllSection != null) {
            this.sellAllSlot = sellAllSection.getString("symbol", "S").charAt(0);
            this.sellFishingBag = sellAllSection.getBoolean("fishingbag", true);

            this.sellAllIconAllowItem = new SingleItemParser("allow", sellAllSection.getSection("allow-icon"), plugin.getConfigManager().getItemFormatFunctions()).getItem();
            this.sellAllIconDenyItem = new SingleItemParser("deny", sellAllSection.getSection("deny-icon"), plugin.getConfigManager().getItemFormatFunctions()).getItem();
            this.sellAllIconLimitItem = new SingleItemParser("limit", sellAllSection.getSection("limit-icon"), plugin.getConfigManager().getItemFormatFunctions()).getItem();

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

            this.sellIconAllowItem = new SingleItemParser("allow", sellSection.getSection("allow-icon"), plugin.getConfigManager().getItemFormatFunctions()).getItem();
            this.sellIconDenyItem = new SingleItemParser("deny", sellSection.getSection("deny-icon"), plugin.getConfigManager().getItemFormatFunctions()).getItem();
            this.sellIconLimitItem = new SingleItemParser("limit", sellSection.getSection("limit-icon"), plugin.getConfigManager().getItemFormatFunctions()).getItem();

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
                if (entry.getValue() instanceof Section innerSection) {
                    char symbol = Objects.requireNonNull(innerSection.getString("symbol")).charAt(0);
                    decorativeIcons.put(symbol, new SingleItemParser("gui", innerSection, plugin.getConfigManager().getItemFormatFunctions()).getItem());
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
    public boolean openMarketGUI(Player player) {
        Optional<UserData> optionalUserData = plugin.getStorageManager().getOnlineUser(player.getUniqueId());
        if (optionalUserData.isEmpty()) {
            plugin.getPluginLogger().warn("Player " + player.getName() + "'s market data has not been loaded yet.");
            return false;
        }
        Context<Player> context = Context.player(player);
        MarketGUI gui = new MarketGUI(this, context, optionalUserData.get().earningData());
        gui.addElement(new MarketGUIElement(itemSlot, new ItemStack(Material.AIR)));
        gui.addElement(new MarketDynamicGUIElement(sellSlot, new ItemStack(Material.AIR)));
        gui.addElement(new MarketDynamicGUIElement(sellAllSlot, new ItemStack(Material.AIR)));
        for (Map.Entry<Character, CustomFishingItem> entry : decorativeIcons.entrySet()) {
            gui.addElement(new MarketGUIElement(entry.getKey(), entry.getValue().build(context)));
        }
        gui.build().refresh().show();
        marketGUICache.put(player.getUniqueId(), gui);
        return true;
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
        MarketGUI gui = marketGUICache.remove(player.getUniqueId());
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
        MarketGUI gui = marketGUICache.remove(event.getPlayer().getUniqueId());
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
        MarketGUI gui = marketGUICache.get(player.getUniqueId());
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
    @EventHandler (ignoreCancelled = true)
    public void onClickInv(InventoryClickEvent event) {
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;

        Player player = (Player) event.getWhoClicked();

        // Check if the clicked inventory is a MarketGUI
        if (!(event.getInventory().getHolder() instanceof MarketGUIHolder))
            return;

        MarketGUI gui = marketGUICache.get(player.getUniqueId());
        if (gui == null) {
            event.setCancelled(true);
            player.closeInventory();
            return;
        }

        EarningData earningData = gui.earningData;
        earningData.refresh();
        double earningLimit = earningLimit(gui.context);

        if (clickedInv != player.getInventory()) {
            int slot = event.getSlot();
            MarketGUIElement element = gui.getElement(slot);
            if (element == null) {
                event.setCancelled(true);
                return;
            }

            if (element.getSymbol() == itemSlot) {
                if (!allowItemWithNoPrice) {
                    if (event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD || event.getAction() == InventoryAction.HOTBAR_SWAP) {
                        ItemStack moved = player.getInventory().getItem(event.getHotbarButton());
                        double price = getItemPrice(gui.context, moved);
                        if (price <= 0) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            } else {
                event.setCancelled(true);
            }

            if (element.getSymbol() == sellSlot) {

                Pair<Integer, Double> pair = getItemsToSell(gui.context, gui.getItemsInGUI());
                double totalWorth = pair.right();
                gui.context.arg(ContextKeys.MONEY, money(totalWorth))
                        .arg(ContextKeys.MONEY_FORMATTED, String.format("%.2f", totalWorth))
                        .arg(ContextKeys.REST, money(earningLimit - earningData.earnings))
                        .arg(ContextKeys.REST_FORMATTED, String.format("%.2f", (earningLimit - earningData.earnings)))
                        .arg(ContextKeys.SOLD_ITEM_AMOUNT, pair.left());

                if (totalWorth > 0) {
                    if (earningLimit != -1 && (earningLimit - earningData.earnings) < totalWorth) {
                        // Can't earn more money
                        ActionManager.trigger(gui.context, sellLimitActions);
                    } else {
                        // Clear items and update earnings
                        clearWorthyItems(gui.context, gui.getItemsInGUI());
                        earningData.earnings += totalWorth;
                        gui.context.arg(ContextKeys.REST, money(earningLimit - earningData.earnings));
                        gui.context.arg(ContextKeys.REST_FORMATTED, String.format("%.2f", (earningLimit - earningData.earnings)));
                        ActionManager.trigger(gui.context, sellAllowActions);
                    }
                } else {
                    // Nothing to sell
                    ActionManager.trigger(gui.context, sellDenyActions);
                }
            } else if (element.getSymbol() == sellAllSlot) {
                List<ItemStack> itemStacksToSell = storageContentsToList(gui.context.getHolder().getInventory().getStorageContents());
                if (sellFishingBag) {
                    Optional<UserData> optionalUserData = BukkitCustomFishingPlugin.getInstance().getStorageManager().getOnlineUser(gui.context.getHolder().getUniqueId());
                    optionalUserData.ifPresent(userData -> itemStacksToSell.addAll(storageContentsToList(userData.holder().getInventory().getStorageContents())));
                }
                Pair<Integer, Double> pair = getItemsToSell(gui.context, itemStacksToSell);
                double totalWorth = pair.right();
                gui.context.arg(ContextKeys.MONEY, money(totalWorth))
                        .arg(ContextKeys.MONEY_FORMATTED, String.format("%.2f", totalWorth))
                        .arg(ContextKeys.REST, money(earningLimit - earningData.earnings))
                        .arg(ContextKeys.REST_FORMATTED, String.format("%.2f", (earningLimit - earningData.earnings)))
                        .arg(ContextKeys.SOLD_ITEM_AMOUNT, pair.left());

                if (totalWorth > 0) {
                    if (earningLimit != -1 && (earningLimit - earningData.earnings) < totalWorth) {
                        // Can't earn more money
                        ActionManager.trigger(gui.context, sellAllLimitActions);
                    } else {
                        // Clear items and update earnings
                        clearWorthyItems(gui.context, itemStacksToSell);
                        earningData.earnings += totalWorth;
                        gui.context.arg(ContextKeys.REST, money(earningLimit - earningData.earnings));
                        gui.context.arg(ContextKeys.REST_FORMATTED, String.format("%.2f", (earningLimit - earningData.earnings)));
                        ActionManager.trigger(gui.context, sellAllAllowActions);
                    }
                } else {
                    // Nothing to sell
                    ActionManager.trigger(gui.context, sellAllDenyActions);
                }
            }
        } else {
            // Handle interactions with the player's inventory
            ItemStack current = event.getCurrentItem();
            if (!allowItemWithNoPrice) {
                double price = getItemPrice(gui.context, current);
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
                    ItemStack itemStack = gui.inventory.getItem(slot);
                    if (itemStack != null && itemStack.getType() != Material.AIR) {
                        if (current.getType() == itemStack.getType()
                                && itemStack.getAmount() != itemStack.getMaxStackSize()
                                && current.getItemMeta().equals(itemStack.getItemMeta())
                        ) {
                            int left = itemStack.getMaxStackSize() - itemStack.getAmount();
                            if (current.getAmount() <= left) {
                                itemStack.setAmount(itemStack.getAmount() + current.getAmount());
                                current.setAmount(0);
                                break;
                            } else {
                                current.setAmount(current.getAmount() - left);
                                itemStack.setAmount(itemStack.getMaxStackSize());
                            }
                        }
                    } else {
                        gui.inventory.setItem(slot, current.clone());
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
    @SuppressWarnings("UnstableApiUsage")
    public double getItemPrice(Context<Player> context, ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return 0;

        Item<ItemStack> wrapped = ((BukkitItemFactory) plugin.getItemManager().getFactory()).wrap(itemStack);
        double price = (double) wrapped.getTag("Price").orElse(0d);
        if (price != 0) {
            // If a custom price is defined in the ItemStack's NBT data, use it.
            return price * itemStack.getAmount();
        }

        if (allowBundle && itemStack.getItemMeta() instanceof BundleMeta bundleMeta) {
            Pair<Integer, Double> pair = getItemsToSell(context, bundleMeta.getItems());
            return pair.right();
        }

        if (allowShulkerBox && itemStack.getItemMeta() instanceof BlockStateMeta stateMeta) {
            if (stateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                Pair<Integer, Double> pair = getItemsToSell(context, Arrays.stream(shulkerBox.getInventory().getStorageContents()).filter(Objects::nonNull).toList());
                return pair.right();
            }
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
        return formula;
    }

    @Override
    public double earningLimit(Context<Player> context) {
        return earningsLimit.evaluate(context);
    }

    public Pair<Integer, Double> getItemsToSell(Context<Player> context, List<ItemStack> itemStacks) {
        int amount = 0;
        double worth = 0d;
        for (ItemStack itemStack : itemStacks) {
            double price = getItemPrice(context, itemStack);
            if (price > 0 && itemStack != null) {
                amount += itemStack.getAmount();
                worth += price;
            }
        }
        return Pair.of(amount, worth);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void clearWorthyItems(Context<Player> context, List<ItemStack> itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            double price = getItemPrice(context, itemStack);
            if (price > 0 && itemStack != null) {
                if (allowBundle && itemStack.getItemMeta() instanceof BundleMeta bundleMeta) {
                    clearWorthyItems(context, bundleMeta.getItems());
                    itemStack.setItemMeta(bundleMeta);
                    continue;
                }
                if (allowShulkerBox && itemStack.getItemMeta() instanceof BlockStateMeta stateMeta) {
                    if (stateMeta.getBlockState() instanceof ShulkerBox shulkerBox) {
                        clearWorthyItems(context, Arrays.stream(shulkerBox.getInventory().getStorageContents()).filter(Objects::nonNull).toList());
                        stateMeta.setBlockState(shulkerBox);
                        itemStack.setItemMeta(stateMeta);
                        continue;
                    }
                }
                itemStack.setAmount(0);
            }
        }
    }

    protected String money(double money) {
        String str = String.format("%.2f", money);
        return str.replace(",", ".");
    }

    protected List<ItemStack> storageContentsToList(ItemStack[] itemStacks) {
        ArrayList<ItemStack> list = new ArrayList<>();
        if (itemStacks != null) {
            for (ItemStack itemStack : itemStacks) {
                if (itemStack != null && itemStack.getType() != Material.AIR) {
                    list.add(itemStack);
                }
            }
        }
        return list;
    }
}
