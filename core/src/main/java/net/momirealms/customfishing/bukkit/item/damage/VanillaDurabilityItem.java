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

package net.momirealms.customfishing.bukkit.item.damage;

import net.momirealms.customfishing.common.item.Item;
import org.bukkit.inventory.ItemStack;

public class VanillaDurabilityItem implements DurabilityItem {

    private final Item<ItemStack> item;

    public VanillaDurabilityItem(Item<ItemStack> item) {
        this.item = item;
    }

    @Override
    public void damage(int value) {
        item.damage(value);
    }

    @Override
    public int damage() {
        return item.damage().orElse(0);
    }

    @Override
    public int maxDamage() {
        return item.maxDamage().get();
    }
}
