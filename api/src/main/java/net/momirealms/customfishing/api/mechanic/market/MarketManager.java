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

package net.momirealms.customfishing.api.mechanic.market;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interface for managing the market
 */
public interface MarketManager extends Reloadable {

    /**
     * Opens the market GUI for the specified player.
     *
     * @param player the {@link Player} for whom the market GUI will be opened
     * @return true if the market GUI was successfully opened, false otherwise
     */
    boolean openMarketGUI(Player player);

    /**
     * Retrieves the price of the specified item within the given context.
     *
     * @param context   the {@link Context} in which the price is calculated
     * @param itemStack the {@link ItemStack} representing the item
     * @return the price of the item as a double
     */
    double getItemPrice(Context<Player> context, ItemStack itemStack);

    /**
     * Retrieves the formula used for calculating item prices.
     *
     * @return the pricing formula as a String
     */
    String getFormula();

    /**
     * Retrieves the earning limit within the given context.
     *
     * @param context the {@link Context} in which the earning limit is checked
     * @return the earning limit as a double
     */
    double earningLimit(Context<Player> context);
}
