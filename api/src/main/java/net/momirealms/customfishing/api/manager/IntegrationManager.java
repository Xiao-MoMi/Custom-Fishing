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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IntegrationManager {

    /**
     * Registers a level plugin with the specified name.
     *
     * @param plugin The name of the level plugin.
     * @param level The implementation of the LevelInterface.
     * @return true if the registration was successful, false if the plugin name is already registered.
     */
    boolean registerLevelPlugin(String plugin, LevelInterface level);

    /**
     * Unregisters a level plugin with the specified name.
     *
     * @param plugin The name of the level plugin to unregister.
     * @return true if the unregistration was successful, false if the plugin name is not found.
     */
    boolean unregisterLevelPlugin(String plugin);

    /**
     * Registers an enchantment provided by a plugin.
     *
     * @param plugin      The name of the plugin providing the enchantment.
     * @param enchantment The enchantment to register.
     * @return true if the registration was successful, false if the enchantment name is already in use.
     */
    boolean registerEnchantment(String plugin, EnchantmentInterface enchantment);

    /**
     * Unregisters an enchantment provided by a plugin.
     *
     * @param plugin The name of the plugin providing the enchantment.
     * @return true if the enchantment was successfully unregistered, false if the enchantment was not found.
     */
    boolean unregisterEnchantment(String plugin);

    /**
     * Get the LevelInterface provided by a plugin.
     *
     * @param plugin The name of the plugin providing the LevelInterface.
     * @return The LevelInterface provided by the specified plugin, or null if the plugin is not registered.
     */
    @Nullable LevelInterface getLevelPlugin(String plugin);

    /**
     * Get an enchantment plugin by its plugin name.
     *
     * @param plugin The name of the enchantment plugin.
     * @return The enchantment plugin interface, or null if not found.
     */
    @Nullable EnchantmentInterface getEnchantmentPlugin(String plugin);

    /**
     * Get a list of enchantment keys with level applied to the given ItemStack.
     *
     * @param itemStack The ItemStack to check for enchantments.
     * @return A list of enchantment names applied to the ItemStack.
     */
    List<String> getEnchantments(ItemStack itemStack);

    /**
     * Get the current season interface, if available.
     *
     * @return The current season interface, or null if not available.
     */
    @Nullable SeasonInterface getSeasonInterface();

    /**
     * Set the current season interface.
     *
     * @param season The season interface to set.
     */
    void setSeasonInterface(SeasonInterface season);
}
