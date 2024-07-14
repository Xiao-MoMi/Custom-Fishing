/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

import net.momirealms.customfishing.common.util.Pair;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The EnchantmentProvider interface defines methods to interact with external
 * enchantment systems, allowing retrieval of enchantments for specific items.
 * Implementations of this interface should provide the logic to fetch enchantments
 * and their respective levels for a given item.
 */
public interface EnchantmentProvider extends ExternalProvider {

    /**
     * Get a list of enchantments with level for itemStack
     *
     * @param itemStack itemStack
     * @return enchantment list
     */
    List<Pair<String, Short>> getEnchants(@NotNull ItemStack itemStack);
}
