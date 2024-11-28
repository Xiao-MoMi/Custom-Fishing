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

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.api.storage.data.EarningData;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.api.util.PlayerUtils;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class MarketGUI {

    private final HashMap<Character, MarketGUIElement> itemsCharMap;
    private final HashMap<Integer, MarketGUIElement> itemsSlotMap;
    private final BukkitMarketManager manager;
    protected final Inventory inventory;
    protected final Context<Player> context;
    protected final EarningData earningData;

    public MarketGUI(BukkitMarketManager manager, Context<Player> context, EarningData earningData) {
        this.manager = manager;
        this.context = context;
        this.earningData = earningData;
        this.itemsCharMap = new HashMap<>();
        this.itemsSlotMap = new HashMap<>();
        var holder = new MarketGUIHolder();
        this.inventory = Bukkit.createInventory(holder, manager.layout.length * 9);
        holder.setInventory(this.inventory);
    }

    private void init() {
        int line = 0;
        for (String content : manager.layout) {
            for (int index = 0; index < 9; index++) {
                char symbol;
                if (index < content.length()) symbol = content.charAt(index);
                else symbol = ' ';
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

    @SuppressWarnings("UnusedReturnValue")
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

    public void show() {
        context.holder().openInventory(inventory);
        SparrowHeart.getInstance().updateInventoryTitle(context.holder(), AdventureHelper.componentToJson(AdventureHelper.miniMessage(manager.title.render(context))));
    }

    @Nullable
    public MarketGUIElement getElement(int slot) {
        return itemsSlotMap.get(slot);
    }

    @Nullable
    public MarketGUIElement getElement(char slot) {
        return itemsCharMap.get(slot);
    }

    /**
     * Refresh the GUI, updating the display based on current data.
     * @return The MarketGUI instance.
     */
    public MarketGUI refresh() {
        double earningLimit = manager.earningLimit(context);
        MarketDynamicGUIElement sellElement = (MarketDynamicGUIElement) getElement(manager.sellSlot);
        if (sellElement != null && !sellElement.getSlots().isEmpty()) {
            Pair<Integer, Double> pair = manager.getItemsToSell(context, getItemsInGUI());
            double totalWorth = pair.right() * manager.earningsMultiplier(context);
            int soldAmount = pair.left();
            context.arg(ContextKeys.MONEY, manager.money(totalWorth))
                    .arg(ContextKeys.MONEY_FORMATTED, String.format("%.2f", totalWorth))
                    .arg(ContextKeys.REST, manager.money(earningLimit - earningData.earnings))
                    .arg(ContextKeys.REST_FORMATTED, String.format("%.2f", (earningLimit - earningData.earnings)))
                    .arg(ContextKeys.SOLD_ITEM_AMOUNT, soldAmount);
            if (totalWorth <= 0) {
                sellElement.setItemStack(manager.sellIconDenyItem.build(context));
            } else if (earningLimit != -1 && (earningLimit - earningData.earnings < totalWorth)) {
                sellElement.setItemStack(manager.sellIconLimitItem.build(context));
            } else {
                sellElement.setItemStack(manager.sellIconAllowItem.build(context));
            }
        }

        MarketDynamicGUIElement sellAllElement = (MarketDynamicGUIElement) getElement(manager.sellAllSlot);
        if (sellAllElement != null && !sellAllElement.getSlots().isEmpty()) {
            List<ItemStack> itemStacksToSell = manager.storageContentsToList(context.holder().getInventory().getStorageContents());
            if (manager.sellFishingBag) {
                Optional<UserData> optionalUserData = BukkitCustomFishingPlugin.getInstance().getStorageManager().getOnlineUser(context.holder().getUniqueId());
                optionalUserData.ifPresent(userData -> itemStacksToSell.addAll(manager.storageContentsToList(userData.holder().getInventory().getStorageContents())));
            }
            Pair<Integer, Double> pair = manager.getItemsToSell(context, itemStacksToSell);
            double totalWorth = pair.right() * manager.earningsMultiplier(context);
            int soldAmount = pair.left();
            context.arg(ContextKeys.MONEY, manager.money(totalWorth))
                    .arg(ContextKeys.MONEY_FORMATTED, String.format("%.2f", totalWorth))
                    .arg(ContextKeys.REST, manager.money(earningLimit - earningData.earnings))
                    .arg(ContextKeys.REST_FORMATTED, String.format("%.2f", (earningLimit - earningData.earnings)))
                    .arg(ContextKeys.SOLD_ITEM_AMOUNT, soldAmount);
            if (totalWorth <= 0) {
                sellAllElement.setItemStack(manager.sellAllIconAllowItem.build(context));
            } else if (earningLimit != -1 && (earningLimit - earningData.earnings < totalWorth)) {
                sellAllElement.setItemStack(manager.sellAllIconLimitItem.build(context));
            } else {
                sellAllElement.setItemStack(manager.sellAllIconAllowItem.build(context));
            }
        }

        for (Map.Entry<Integer, MarketGUIElement> entry : itemsSlotMap.entrySet()) {
            if (entry.getValue() instanceof MarketDynamicGUIElement dynamicGUIElement) {
                this.inventory.setItem(entry.getKey(), dynamicGUIElement.getItemStack().clone());
            }
        }
        return this;
    }

    public List<ItemStack> getItemsInGUI() {
        MarketGUIElement itemElement = getElement(manager.itemSlot);
        if (itemElement == null) return List.of();
        return itemElement.getSlots().stream().map(inventory::getItem).filter(Objects::nonNull).toList();
    }

    public int getEmptyItemSlot() {
        MarketGUIElement itemElement = getElement(manager.itemSlot);
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
        MarketGUIElement itemElement = getElement(manager.itemSlot);
        if (itemElement == null) {
            return;
        }
        for (int slot : itemElement.getSlots()) {
            ItemStack itemStack = inventory.getItem(slot);
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                PlayerUtils.giveItem(context.holder(), itemStack, itemStack.getAmount());
                inventory.setItem(slot, new ItemStack(Material.AIR));
            }
        }
    }
}
