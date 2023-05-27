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

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MMOItemsItemImpl implements ItemInterface {

    @Nullable
    @Override
    public ItemStack build(String material) {
        if (!material.startsWith("MMOItems:")) return null;
        material = material.substring(9);
        String[] split = material.split(":");
        MMOItem mmoItem = MMOItems.plugin.getMMOItem(Type.get(split[0]), split[1].toUpperCase());
        return mmoItem == null ? null : mmoItem.newBuilder().build();
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("MMOITEMS_MAX_DURABILITY")) return false;
        DurabilityItem durabilityItem = new DurabilityItem(player, itemStack);
        if (Math.random() < (1 / (double) (itemStack.getEnchantmentLevel(Enchantment.DURABILITY) + 1))) {
            durabilityItem.decreaseDurability(1);
            final ItemStack newVersion = durabilityItem.toItem();
            if (newVersion == null) return false;
            itemStack.setItemMeta(newVersion.getItemMeta());
        }
        return true;
    }
}
