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

package net.momirealms.customfishing.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class ItemUtils {

    public static void loseDurability(ItemStack itemStack, int amount) {
        if (itemStack.getItemMeta() instanceof Damageable damageable) {
            if (damageable.isUnbreakable()) {
                return;
            }
            int unBreakingLevel = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);
            if (Math.random() > (double) 1 / (unBreakingLevel + 1)) {
                return;
            }

            NBTItem nbtItem = new NBTItem(itemStack);
            NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
            if (cfCompound != null && cfCompound.hasTag("max_dur")) {
                int max = cfCompound.getInteger("max_dur");
                int current = cfCompound.getInteger("cur_dur") - amount;
                cfCompound.setInteger("cur_dur", current);
                int damage = (int) (itemStack.getType().getMaxDurability() * (1 - ((double) current / max)));
                nbtItem.setInteger("Damage", damage);
                if (current > 0) {
                    itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
                } else {
                    itemStack.setAmount(0);
                }
            } else {
                int damage = damageable.getDamage() + amount;
                if (damage > itemStack.getType().getMaxDurability()) {
                    itemStack.setAmount(0);
                } else {
                    damageable.setDamage(damage);
                    itemStack.setItemMeta(damageable);
                }
            }
        }
    }

    public static void addDurability(ItemStack itemStack, int amount) {
        if (itemStack.getItemMeta() instanceof Damageable damageable) {
            if (damageable.isUnbreakable()) {
                return;
            }
            NBTItem nbtItem = new NBTItem(itemStack);
            NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
            if (cfCompound != null && cfCompound.hasTag("max_dur")) {
                int max = cfCompound.getInteger("max_dur");
                int current = Math.min(max, cfCompound.getInteger("cur_dur") + amount);
                cfCompound.setInteger("cur_dur", current);
                int damage = (int) (itemStack.getType().getMaxDurability() * (1 - ((double) current / max)));
                nbtItem.setInteger("Damage", damage);
                itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
            } else {
                int damage = Math.max(damageable.getDamage() - amount, 0);
                damageable.setDamage(damage);
                itemStack.setItemMeta(damageable);
            }
        }
    }
}
