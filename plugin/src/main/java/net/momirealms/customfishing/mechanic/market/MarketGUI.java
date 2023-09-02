package net.momirealms.customfishing.mechanic.market;

import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.mechanic.market.MarketGUIHolder;
import net.momirealms.customfishing.api.util.InventoryUtils;
import net.momirealms.customfishing.api.util.LogUtils;
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

    public MarketGUI(MarketManagerImpl manager, Player player) {
        this.manager = manager;
        this.owner = player;
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
            if (content.length() != 9) {
                LogUtils.warn("Please make sure that GUI layout has 9 elements in each row");
                return;
            }
            for (int index = 0; index < 9; index++) {
                char symbol = content.charAt(index);
                MarketGUIElement element = itemsCharMap.get(symbol);
                element.addSlot(index + line * 9);
                itemsSlotMap.put(index + line * 9, element);
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

    public void refresh() {
        double totalWorth = getTotalWorth();
        if (totalWorth <= 0) {
            addElement(new MarketDynamicGUIElement(
                    manager.getFunctionSlot(),
                    manager.getFunctionIconDenyBuilder().build(owner,
                            Map.of("{worth}", String.format("%.2f", totalWorth)
                                    ,"{player}", owner.getName())
                    )
            ));
        } else {
            addElement(new MarketDynamicGUIElement(
                    manager.getFunctionSlot(),
                    manager.getFunctionIconAllowBuilder().build(owner,
                            Map.of("{worth}", String.format("%.2f", totalWorth)
                                    ,"{player}", owner.getName())
                    )
            ));
        }
        for (Map.Entry<Integer, MarketGUIElement> entry : itemsSlotMap.entrySet()) {
            if (entry.getValue() instanceof MarketDynamicGUIElement dynamicGUIElement) {
                this.inventory.setItem(entry.getKey(), dynamicGUIElement.getItemStack().clone());
            }
        }
    }

    public double getTotalWorth() {
        double money = 0d;
        MarketGUIElement itemElement = getElement(manager.getItemSlot());
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
}
