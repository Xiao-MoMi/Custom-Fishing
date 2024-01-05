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

package net.momirealms.customfishing.compatibility.item;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import net.momirealms.customfishing.api.util.LogUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderItemImpl implements ItemLibrary {

    @Override
    public String identification() {
        return "ItemsAdder";
    }

    @Override
    public ItemStack buildItem(Player player, String id) {
        CustomStack stack = CustomStack.getInstance(id);
        if (stack == null) {
            LogUtils.severe(id + " doesn't exist in ItemsAdder configs.");
            return null;
        }
        return stack.getItemStack();
    }

    @Override
    public String getItemID(ItemStack itemStack) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        if (customStack == null) return null;
        return customStack.getNamespacedID();
    }
}
