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
import net.momirealms.customfishing.api.util.LogUtils;
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
    private double earningLimit;
    private boolean allowItemWithNoPrice;
    private final ConcurrentHashMap<UUID, MarketGUI> marketGUIMap;
    private boolean enable;

    public MarketManagerImpl(CustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.priceMap = new HashMap<>();
        this.decorativeIcons = new HashMap<>();
        this.marketGUIMap = new ConcurrentHashMap<>();
    }

    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        this.priceMap.clear();
        this.decorativeIcons.clear();
    }

    public void disable() {
        unload();
    }

    private void loadConfig() {
        YamlConfiguration config = plugin.getConfig("market.yml");
        this.enable = config.getBoolean("enable", true);
        if (!this.enable) return;
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
        this.earningLimit = config.getBoolean("limitation.enable", true) ? config.getDouble("limitation.earnings", 100) : -1;
        this.allowItemWithNoPrice = config.getBoolean("item-slot.allow-items-with-no-price", true);

        ConfigurationSection priceSection = config.getConfigurationSection("item-price");
        if (priceSection != null) {
            for (Map.Entry<String, Object> entry : priceSection.getValues(false).entrySet()) {
                this.priceMap.put(entry.getKey(), ConfigUtils.getDoubleValue(entry.getValue()));
            }
        }
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

    @Override
    public void openMarketGUI(Player player) {
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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        MarketGUI gui = marketGUIMap.remove(event.getPlayer().getUniqueId());
        if (gui != null)
            gui.returnItems();
    }

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

    @EventHandler
    public void onClickInv(InventoryClickEvent event) {
        if (event.isCancelled())
            return;
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null)
            return;
        Player player = (Player) event.getWhoClicked();
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
            if (data.date != getDate()) {
                data.date = getDate();
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
                Condition condition = new Condition(player, new HashMap<>(Map.of(
                        "{money}", String.format("%.2f", worth)
                        ,"{rest}", String.format("%.2f", (earningLimit - data.earnings))
                )));
                if (worth > 0) {
                    if (earningLimit != -1 && (earningLimit - data.earnings) < worth) {
                        // can't earn more money
                        if (limitActions != null) {
                            for (Action action : limitActions) {
                                action.trigger(condition);
                            }
                        }
                    } else {
                        // clear items
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
                    // nothing to sell
                    if (denyActions != null) {
                        for (Action action : denyActions) {
                            action.trigger(condition);
                        }
                    }
                }
            }
        } else {
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

        plugin.getScheduler().runTaskSyncLater(gui::refresh, player.getLocation(), 50, TimeUnit.MILLISECONDS);
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
            return price * itemStack.getAmount();
        }
        String itemID = itemStack.getType().name();
        if (nbtItem.hasTag("CustomModelData")) {
            itemID = itemID + ":" + nbtItem.getInteger("CustomModelData");
        }
        return priceMap.getOrDefault(itemID, 0d) * itemStack.getAmount();
    }

    @Override
    public String getFormula() {
        return formula;
    }

    @Override
    public double getPrice(float base, float bonus, float size) {
        Expression expression = new ExpressionBuilder(getFormula())
                .variables("base", "bonus", "size")
                .build()
                .setVariable("base", base)
                .setVariable("bonus", bonus)
                .setVariable("size", size);
        return expression.evaluate();
    }

    public char getItemSlot() {
        return itemSlot;
    }

    public char getFunctionSlot() {
        return functionSlot;
    }

    public String[] getLayout() {
        return layout;
    }

    public String getTitle() {
        return title;
    }

    public double getEarningLimit() {
        return earningLimit;
    }

    public BuildableItem getFunctionIconLimitBuilder() {
        return functionIconLimitBuilder;
    }

    public BuildableItem getFunctionIconAllowBuilder() {
        return functionIconAllowBuilder;
    }

    public BuildableItem getFunctionIconDenyBuilder() {
        return functionIconDenyBuilder;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }
}
