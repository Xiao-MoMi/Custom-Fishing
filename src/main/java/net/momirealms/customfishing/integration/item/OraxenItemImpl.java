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

import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.mechanics.provided.gameplay.durability.DurabilityMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.durability.DurabilityMechanicFactory;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class OraxenItemImpl implements ItemInterface {

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (!material.startsWith("Oraxen:")) return null;
        material = material.substring(7);
        ItemBuilder itemBuilder = OraxenItems.getItemById(material);
        return itemBuilder == null ? null : itemBuilder.build();
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        DurabilityMechanic mechanic = (DurabilityMechanic) DurabilityMechanicFactory.get().getMechanic(OraxenItems.getIdByItem(itemStack));
        if (mechanic == null) {
            return false;
        }
        if (Math.random() < (1 / (double) (itemStack.getEnchantmentLevel(Enchantment.DURABILITY) + 1))) {
            mechanic.changeDurability(itemStack, -1);
        }
        return true;
    }

    @Override
    public @Nullable String getID(ItemStack itemStack) {
        return OraxenItems.getIdByItem(itemStack);
    }
}
