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

package net.momirealms.customfishing.compatibility.item;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomFishingItemImpl implements ItemLibrary {

    @Override
    public String identification() {
        return "CustomFishing";
    }

    @Override
    public ItemStack buildItem(Player player, String id) {
        String[] split = id.split(":", 2);
        return CustomFishingPlugin.get().getItemManager().build(player, split[0], split[1]);
    }

    @Override
    public String getItemID(ItemStack itemStack) {
        return CustomFishingPlugin.get().getItemManager().getCustomFishingItemID(itemStack);
    }
}
