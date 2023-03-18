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
import net.momirealms.customfishing.fishing.competition.Competition;
import net.momirealms.customfishing.fishing.loot.DroppedItem;
import net.momirealms.customfishing.fishing.loot.Loot;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

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
        return CustomFishing.getInstance().getFishingManager().getSize(fish);
    }

    /**
     * get plugin instance
     * @return plugin instance
     */
    public static CustomFishing getPluginInstance() {
        return CustomFishing.getInstance();
    }

    /**
     * get an item's price
     * @param itemStack item to sell
     * @return price
     */
    public static double getItemPrice(ItemStack itemStack) {
        return CustomFishing.getInstance().getSellManager().getSingleItemPrice(itemStack);
    }

    /**
     * get items directly from item library
     * return AIR if the loot does not exist
     * @param id item_id
     * @return itemStack
     */
    @NotNull
    public static ItemStack getLootByID(String id) {
        return CustomFishing.getInstance().getIntegrationManager().build(id);
    }

    /**
     * get items obtained by fishing
     * return AIR if the loot does not exist
     * @param id item_id
     * @param player player
     * @return itemStack
     */
    @NotNull
    public static ItemStack getLootByID(String id, @Nullable Player player) {
        Loot loot = CustomFishing.getInstance().getLootManager().getLoot(id);
        if (!(loot instanceof DroppedItem droppedItem)) return new ItemStack(Material.AIR);
        return CustomFishing.getInstance().getFishingManager().getCustomFishingLootItemStack(droppedItem, player);
    }

    /**
     * get the catch amount of a certain loot
     * return -1 if player's data is not loaded
     * @param id loot id
     * @param uuid uuid
     * @return amount
     */
    public static int getCertainLootCatchAmount(String id, UUID uuid) {
        return CustomFishing.getInstance().getStatisticsManager().getFishAmount(uuid, id);
    }
}
