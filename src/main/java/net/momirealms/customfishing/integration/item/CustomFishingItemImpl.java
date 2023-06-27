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

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;

public class CustomFishingItemImpl implements ItemInterface {

    private final CustomFishing plugin;

    public CustomFishingItemImpl(CustomFishing plugin) {
        this.plugin = plugin;
    }

    @Override
    @Nullable
    public ItemStack build(String material, Player player) {
        if (material.contains(":")) return null;
        return plugin.getLootManager().build(material);
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        Damageable damageable = (Damageable) itemStack.getItemMeta();
        if (Math.random() < (1 / (double) (damageable.getEnchantLevel(Enchantment.DURABILITY) + 1))) {
            damageable.setDamage(damageable.getDamage() + 1);
            itemStack.setItemMeta(damageable);
        }
        return true;
    }

    @Override
    public @Nullable String getID(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        if (nbtCompound == null) return null;
        return nbtCompound.getString("id");
    }
}
