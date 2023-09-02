package net.momirealms.customfishing.mechanic.market;

import org.bukkit.inventory.ItemStack;

public class MarketDynamicGUIElement extends MarketGUIElement {

    public MarketDynamicGUIElement(char symbol, ItemStack itemStack) {
        super(symbol, itemStack);
    }

    public void setItemStack(ItemStack itemStack) {
        super.itemStack = itemStack;
    }
}
