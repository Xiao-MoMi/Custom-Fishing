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

package net.momirealms.customfishing.integration.enchantment;

import net.advancedplugins.ae.api.AEAPI;
import net.momirealms.customfishing.integration.EnchantmentInterface;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AEImpl implements EnchantmentInterface {

    @Override
    public List<String> getEnchants(ItemStack itemStack) {
        List<String> enchants = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : AEAPI.getEnchantmentsOnItem(itemStack).entrySet()) {
            enchants.add("AE:" + entry.getKey() + ":" + entry.getValue());
        }
        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        for (Map.Entry<Enchantment, Integer> en : enchantments.entrySet()) {
            String key = en.getKey().getKey() + ":" + en.getValue();
            enchants.add(key);
        }
        return enchants;
    }
}
