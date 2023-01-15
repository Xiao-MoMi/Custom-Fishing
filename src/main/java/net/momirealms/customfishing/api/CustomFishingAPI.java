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

package net.momirealms.customfishing.api;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.competition.Competition;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CustomFishingAPI {

    /**
     * return null if there's no competition
     * @return competition
     */
    @Nullable
    public static Competition getCurrentCompetition() {
        return Competition.getCurrentCompetition();
    }

    /**
     * get a fish's size
     * @return size
     */
    public static float getFishSize(ItemStack fish) {
        return CustomFishing.plugin.getFishingManager().getSize(fish);
    }

    /**
     * get plugin instance
     * @return plugin instance
     */
    public static CustomFishing getInstance() {
        return CustomFishing.plugin;
    }

    /**
     * get an item's price
     * @param itemStack item to sell
     * @return price
     */
    public static double getItemPrice(ItemStack itemStack) {
        return CustomFishing.plugin.getSellManager().getSingleItemPrice(itemStack);
    }

    /**
     * build an itemStack instance from key
     * @param id item_id
     * @return itemStack
     */
    public static ItemStack buildItem(String id) {
        return CustomFishing.plugin.getIntegrationManager().build(id);
    }
}
