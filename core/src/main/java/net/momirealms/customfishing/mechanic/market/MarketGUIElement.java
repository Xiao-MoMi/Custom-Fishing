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

    // Method to add a slot to the list of slots for this element
    public void addSlot(int slot) {
        slots.add(slot);
    }

    // Getter method to retrieve the symbol associated with this element
    public char getSymbol() {
        return symbol;
    }

    // Getter method to retrieve the cloned ItemStack associated with this element
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    // Getter method to retrieve the list of slots where this element can appear
    public List<Integer> getSlots() {
        return slots;
    }
}
