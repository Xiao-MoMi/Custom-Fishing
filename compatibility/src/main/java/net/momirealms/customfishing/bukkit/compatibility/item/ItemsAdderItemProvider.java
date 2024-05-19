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

package net.momirealms.customfishing.bukkit.compatibility.item;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customfishing.api.integration.ItemProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ItemsAdderItemProvider implements ItemProvider {

    @Override
    public String identifier() {
        return "ItemsAdder";
    }

    @NotNull
    @Override
    public ItemStack buildItem(@NotNull Player player, @NotNull String id) {
        CustomStack stack = requireNonNull(CustomStack.getInstance(id));
        return stack.getItemStack();
    }

    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        return Optional.ofNullable(CustomStack.byItemStack(itemStack)).map(CustomStack::getNamespacedID).orElse(null);
    }
}