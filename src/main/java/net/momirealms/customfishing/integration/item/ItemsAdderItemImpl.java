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

package net.momirealms.customfishing.integration.item;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderItemImpl implements ItemInterface {

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (!material.startsWith("ItemsAdder:")) return null;
        material = material.substring(11);
        CustomStack customStack = CustomStack.getInstance(material);
        return customStack == null ? null : customStack.getItemStack();
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        CustomStack customStack = CustomStack.byItemStack(itemStack);
        if (customStack == null) return false;
        if (customStack.hasCustomDurability()) {
            customStack.setDurability(customStack.getDurability() - 1);
            return true;
        }
        return false;
    }
}
