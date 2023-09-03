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

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.data.EarningData;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.api.util.InventoryUtils;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.mechanic.item.ItemManagerImpl;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MarketGUI {

    private final HashMap<Character, MarketGUIElement> itemsCharMap;
    private final HashMap<Integer, MarketGUIElement> itemsSlotMap;
    private final Inventory inventory;
    private final MarketManagerImpl manager;
    private final Player owner;
    private final EarningData earningData;

    public MarketGUI(MarketManagerImpl manager, Player player, EarningData earningData) {
        this.manager = manager;
        this.owner = player;
        this.earningData = earningData;
        this.itemsCharMap = new HashMap<>();
        this.itemsSlotMap = new HashMap<>();
        var holder = new MarketGUIHolder();
        this.inventory = InventoryUtils.createInventory(
                holder,
                manager.getLayout().length * 9,
                AdventureManagerImpl.getInstance().getComponentFromMiniMessage(manager.getTitle())
        );
        holder.setInventory(this.inventory);
    }

    private void init() {
        int line = 0;
        for (String content : manager.getLayout()) {
            for (int index = 0; index < 9; index++) {
                char symbol;
                if (index < content.length()) {
                    symbol = content.charAt(index);
                } else {
                    symbol = ' ';
                }
                MarketGUIElement element = itemsCharMap.get(symbol);
                if (element != null) {
                    element.addSlot(index + line * 9);
                    itemsSlotMap.put(index + line * 9, element);
                }
            }
            line++;
        }
        for (Map.Entry<Integer, MarketGUIElement> entry : itemsSlotMap.entrySet()) {
            this.inventory.setItem(entry.getKey(), entry.getValue().getItemStack().clone());
        }
    }

    public MarketGUI addElement(MarketGUIElement... elements) {
        for (MarketGUIElement element : elements) {
            itemsCharMap.put(element.getSymbol(), element);
        }
        return this;
    }

    public MarketGUI build() {
        init();
        return this;
    }

    public void show(Player player) {
        if (player != owner) return;
        player.openInventory(inventory);
    }

    @Nullable
    public MarketGUIElement getElement(int slot) {
        return itemsSlotMap.get(slot);
    }

    @Nullable
    public MarketGUIElement getElement(char slot) {
        return itemsCharMap.get(slot);
    }

    public MarketGUI refresh() {
        double totalWorth = getTotalWorth();
        MarketDynamicGUIElement functionElement = (MarketDynamicGUIElement) getElement(manager.getFunctionSlot());
        if (functionElement == null) {
            return this;
        }
        if (totalWorth <= 0) {
            functionElement.setItemStack(
                    manager.getFunctionIconDenyBuilder().build(owner,
                            Map.of("{money}", String.format("%.2f", totalWorth)
                                    ,"{player}", owner.getName()
                                    ,"{rest}", String.format("%.2f", manager.getEarningLimit() - earningData.earnings))
                    )
            );
        } else if (manager.getEarningLimit() != -1 && (manager.getEarningLimit() - earningData.earnings < totalWorth)) {
            functionElement.setItemStack(
                    manager.getFunctionIconLimitBuilder().build(owner,
                            Map.of("{money}", String.format("%.2f", totalWorth)
                                    ,"{player}", owner.getName()
                                    ,"{rest}", String.format("%.2f", manager.getEarningLimit() - earningData.earnings))
                    )
            );
        } else {
            functionElement.setItemStack(
                    manager.getFunctionIconAllowBuilder().build(owner,
                            Map.of("{money}", String.format("%.2f", totalWorth)
                                    ,"{player}", owner.getName()
                                    ,"{rest}", String.format("%.2f", manager.getEarningLimit() - earningData.earnings))
                    )
            );
        }
        for (Map.Entry<Integer, MarketGUIElement> entry : itemsSlotMap.entrySet()) {
            if (entry.getValue() instanceof MarketDynamicGUIElement dynamicGUIElement) {
                this.inventory.setItem(entry.getKey(), dynamicGUIElement.getItemStack().clone());
            }
        }
        return this;
    }

    public double getTotalWorth() {
        double money = 0d;
        MarketGUIElement itemElement = getElement(manager.getItemSlot());;
        if (itemElement == null) {
            LogUtils.warn("No item slot available. Please check if GUI layout contains the item slot symbol.");
            return money;
        }
        for (int slot : itemElement.getSlots()) {
            money += manager.getItemPrice(this.inventory.getItem(slot));
        }
        return money;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void clearWorthyItems() {
        MarketGUIElement itemElement = getElement(manager.getItemSlot());
        if (itemElement == null) {
            return;
        }
        for (int slot : itemElement.getSlots()) {
            double money = manager.getItemPrice(inventory.getItem(slot));
            if (money != 0) {
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }
        }
    }

    public int getEmptyItemSlot() {
        MarketGUIElement itemElement = getElement(manager.getItemSlot());
        if (itemElement == null) {
            return -1;
        }
        for (int slot : itemElement.getSlots()) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                return slot;
            }
        }
        return -1;
    }

    public void returnItems() {
        MarketGUIElement itemElement = getElement(manager.getItemSlot());
        if (itemElement == null) {
            return;
        }
        for (int slot : itemElement.getSlots()) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                ItemManagerImpl.giveCertainAmountOfItem(owner, itemStack, itemStack.getAmount());
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }
        }
    }

    public EarningData getEarningData() {
        return earningData;
    }
}
