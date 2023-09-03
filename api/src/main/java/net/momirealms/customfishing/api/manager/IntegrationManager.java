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

import net.momirealms.customfishing.api.integration.EnchantmentInterface;
import net.momirealms.customfishing.api.integration.LevelInterface;
import net.momirealms.customfishing.api.integration.SeasonInterface;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface IntegrationManager {

    boolean registerLevelPlugin(String plugin, LevelInterface level);

    boolean unregisterLevelPlugin(String plugin);

    boolean registerEnchantment(String plugin, EnchantmentInterface enchantment);

    boolean unregisterEnchantment(String plugin);

    LevelInterface getLevelHook(String plugin);

    List<String> getEnchantments(ItemStack rod);

    SeasonInterface getSeasonInterface();

    void setSeasonInterface(SeasonInterface season);
}
