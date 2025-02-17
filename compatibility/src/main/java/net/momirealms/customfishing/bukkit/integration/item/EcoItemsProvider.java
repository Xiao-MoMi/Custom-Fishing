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

package net.momirealms.customfishing.bukkit.integration.item;
import com.willfp.ecoitems.items.EcoItem;
import com.willfp.ecoitems.items.EcoItemFinder;
import com.willfp.ecoitems.items.EcoItems;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class EcoItemsProvider implements ItemProvider {

    @Override
    public String identifier() {
        return "EcoItems";
    }

    @NotNull
    @Override
    public ItemStack buildItem(@NotNull Context<Player> player, @NotNull String id) {
        EcoItem item = EcoItems.INSTANCE.getByID(id);
        requireNonNull(item, "EcoItems cannot find item with ID " + id);
        return item.getItemStack();
    }

    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        List<EcoItem> list = EcoItemFinder.INSTANCE.find(itemStack);
        if (list.isEmpty()) return null;
        return list.get(0).getID();
    }
}
