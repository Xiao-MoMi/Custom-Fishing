package net.momirealms.customfishing.mechanic.market;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class MarketGUIElement {

    private final char symbol;
    private final List<Integer> slots;
    protected ItemStack itemStack;

    public MarketGUIElement(char symbol, ItemStack itemStack) {
        this.symbol = symbol;
        this.itemStack = itemStack;
        this.slots = new ArrayList<>();
    }

    public void addSlot(int slot) {
        slots.add(slot);
    }

    public char getSymbol() {
        return symbol;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Integer> getSlots() {
        return slots;
    }
}
