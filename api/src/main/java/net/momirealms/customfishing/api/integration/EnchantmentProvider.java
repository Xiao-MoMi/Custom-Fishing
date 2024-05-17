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

package net.momirealms.customfishing.api.integration;

import net.kyori.adventure.key.Key;
import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface EnchantmentProvider extends ExternalProvider {

    /**
     * Get a list of enchantments with level for itemStack
     *
     * @param itemStack itemStack
     * @return enchantment list
     */
    List<Pair<Key, Short>> getEnchants(ItemStack itemStack);
}
