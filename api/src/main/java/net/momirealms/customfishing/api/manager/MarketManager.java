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

package net.momirealms.customfishing.api.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public interface MarketManager {

    /**
     * Open the market GUI for a player
     *
     * @param player player
     */
    void openMarketGUI(Player player);

    /**
     * Retrieves the current date as an integer in the format MMDD (e.g., September 21 as 0921).
     *
     * @return An integer representing the current date.
     */
    int getCachedDate();

    /**
     * Retrieves the current date as an integer in the format MMDD (e.g., September 21 as 0921).
     *
     * @return An integer representing the current date.
     */
    int getDate();

    /**
     * Calculates the price of an ItemStack based on custom data or a predefined price map.
     *
     * @param itemStack The ItemStack for which the price is calculated.
     * @return The calculated price of the ItemStack.
     */
    double getItemPrice(ItemStack itemStack);

    /**
     * Retrieves the formula used for calculating prices.
     *
     * @return The pricing formula as a string.
     */
    String getFormula();

    /**
     * Calculates the price based on a formula with provided variables.
     *
     * @return The calculated price based on the formula and provided variables.
     */
    double getFishPrice(Player player, Map<String, String> vars);

    /**
     * Gets the character representing the item slot in the MarketGUI.
     *
     * @return The item slot character.
     */
    char getItemSlot();

    /**
     * Gets the character representing the sell slot in the MarketGUI.
     *
     * @return The sell slot character.
     */
    char getSellSlot();

    /**
     * Gets the character representing the sell-all slot in the MarketGUI.
     *
     * @return The sell-all slot character.
     */
    char getSellAllSlot();

    /**
     * Gets the layout of the MarketGUI as an array of strings.
     *
     * @return The layout of the MarketGUI.
     */
    String[] getLayout();

    /**
     * Gets the title of the MarketGUI.
     *
     * @return The title of the MarketGUI.
     */
    String getTitle();

    /**
     * Gets the earning limit
     *
     * @return The earning limit
     */
    double getEarningLimit(Player player);

    /**
     * Is market enabled
     *
     * @return enable or not
     */
    boolean isEnable();

    /**
     * Should fish in bag also be sold
     *
     * @return sell or not
     */
    boolean sellFishingBag();

    /**
     * Get the total worth of the items in inventory
     *
     * @param inventory inventory
     * @return total worth
     */
    double getInventoryTotalWorth(Inventory inventory);

    int getInventorySellAmount(Inventory inventory);
}
