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

package net.momirealms.customfishing.api.integration;

import org.jetbrains.annotations.Nullable;

/**
 * Interface for managing integration providers in the custom fishing API.
 * This allows for the registration and retrieval of various types of providers
 * such as Leveler, Enchantment, and Season providers.
 */
public interface IntegrationManager {

    /**
     * Registers a LevelerProvider.
     *
     * @param level the LevelerProvider to register
     * @return true if registration is successful, false otherwise
     */
    boolean registerLevelerProvider(LevelerProvider level);

    /**
     * Unregisters a LevelerProvider by its ID.
     *
     * @param id the ID of the LevelerProvider to unregister
     * @return true if unregistration is successful, false otherwise
     */
    boolean unregisterLevelerProvider(String id);

    /**
     * Registers an EnchantmentProvider.
     *
     * @param enchantment the EnchantmentProvider to register
     * @return true if registration is successful, false otherwise
     */
    boolean registerEnchantmentProvider(EnchantmentProvider enchantment);

    /**
     * Unregisters an EnchantmentProvider by its ID.
     *
     * @param id the ID of the EnchantmentProvider to unregister
     * @return true if unregistration is successful, false otherwise
     */
    boolean unregisterEnchantmentProvider(String id);

    /**
     * Registers a SeasonProvider.
     *
     * @param season the SeasonProvider to register
     * @return true if registration is successful, false otherwise
     */
    boolean registerSeasonProvider(SeasonProvider season);

    /**
     * Unregisters a SeasonProvider by its ID.
     *
     * @param id the ID of the SeasonProvider to unregister
     * @return true if unregistration is successful, false otherwise
     */
    boolean unregisterSeasonProvider(String id);

    boolean registerEntityProvider(EntityProvider entity);

    boolean unregisterEntityProvider(String identifier);

    /**
     * Retrieves a registered LevelerProvider by its ID.
     *
     * @param id the ID of the LevelerProvider to retrieve
     * @return the LevelerProvider if found, or null if not found
     */
    @Nullable
    LevelerProvider getLevelerProvider(String id);

    /**
     * Retrieves a registered EnchantmentProvider by its ID.
     *
     * @param id the ID of the EnchantmentProvider to retrieve
     * @return the EnchantmentProvider if found, or null if not found
     */
    @Nullable
    EnchantmentProvider getEnchantmentProvider(String id);

    /**
     * Retrieves a registered SeasonProvider by its ID.
     *
     * @param id the ID of the SeasonProvider to retrieve
     * @return the SeasonProvider if found, or null if not found
     */
    @Nullable
    SeasonProvider getSeasonProvider(String id);
}
