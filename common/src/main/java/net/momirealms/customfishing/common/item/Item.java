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

package net.momirealms.customfishing.common.item;

import net.momirealms.customfishing.common.util.Key;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Interface representing an item.
 * This interface provides methods for managing item properties such as custom model data,
 * damage, display name, lore, enchantments, and tags.
 *
 * @param <I> the type of the item implementation
 */
public interface Item<I> {

    /**
     * Sets the custom model data for the item.
     *
     * @param data the custom model data to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> customModelData(Integer data);

    /**
     * Retrieves the custom model data of the item.
     *
     * @return an {@link Optional} containing the custom model data, or empty if not set
     */
    Optional<Integer> customModelData();

    /**
     * Sets the damage value for the item.
     *
     * @param data the damage value to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> damage(Integer data);

    /**
     * Retrieves the damage value of the item.
     *
     * @return an {@link Optional} containing the damage value, or empty if not set
     */
    Optional<Integer> damage();

    /**
     * Sets the maximum damage value for the item.
     *
     * @param data the maximum damage value to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> maxDamage(Integer data);

    /**
     * Retrieves the maximum damage value of the item.
     *
     * @return an {@link Optional} containing the maximum damage value, or empty if not set
     */
    Optional<Integer> maxDamage();

    /**
     * Sets the display name for the item.
     *
     * @param displayName the display name to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> displayName(String displayName);

    /**
     * Retrieves the display name of the item.
     *
     * @return an {@link Optional} containing the display name, or empty if not set
     */
    Optional<String> displayName();

    /**
     * Sets the lore for the item.
     *
     * @param lore the lore to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> lore(List<String> lore);

    /**
     * Retrieves the lore of the item.
     *
     * @return an {@link Optional} containing the lore, or empty if not set
     */
    Optional<List<String>> lore();

    /**
     * Sets whether the item is unbreakable.
     *
     * @param unbreakable true if the item should be unbreakable, false otherwise
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> unbreakable(boolean unbreakable);

    /**
     * Checks if the item is unbreakable.
     *
     * @return true if the item is unbreakable, false otherwise
     */
    boolean unbreakable();

    /**
     * Sets the skull data for the item.
     *
     * @param data the skull data to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> skull(String data);

    /**
     * Sets the enchantments for the item.
     *
     * @param enchantments a map of enchantments to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> enchantments(Map<Key, Short> enchantments);

    /**
     * Adds an enchantment to the item.
     *
     * @param enchantment the enchantment to add
     * @param level       the level of the enchantment
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> addEnchantment(Key enchantment, int level);

    /**
     * Sets the stored enchantments for the item.
     *
     * @param enchantments a map of stored enchantments to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> storedEnchantments(Map<Key, Short> enchantments);

    /**
     * Adds a stored enchantment to the item.
     *
     * @param enchantment the stored enchantment to add
     * @param level       the level of the stored enchantment
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> addStoredEnchantment(Key enchantment, int level);

    /**
     * Sets the item flags for the item.
     *
     * @param flags a list of item flags to set
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> itemFlags(List<String> flags);

    /**
     * Retrieves the tag value at the specified path.
     *
     * @param path the path to the tag value
     * @return an {@link Optional} containing the tag value, or empty if not found
     */
    Optional<Object> getTag(Object... path);

    /**
     * Sets the tag value at the specified path.
     *
     * @param value the value to set
     * @param path  the path to the tag value
     * @return the current {@link Item} instance for method chaining
     */
    Item<I> setTag(Object value, Object... path);

    /**
     * Checks if the item has a tag value at the specified path.
     *
     * @param path the path to the tag value
     * @return true if the tag value exists, false otherwise
     */
    boolean hasTag(Object... path);

    /**
     * Removes the tag value at the specified path.
     *
     * @param path the path to the tag value
     * @return true if the tag was removed, false otherwise
     */
    boolean removeTag(Object... path);

    /**
     * Retrieves the underlying item implementation.
     *
     * @return the item implementation of type {@link I}
     */
    I getItem();

    /**
     * Loads changes to the item.
     *
     * @return the loaded item implementation of type {@link I}
     */
    I load();

    /**
     * Loads the changes and gets a copy of the item.
     *
     * @return a copy of the loaded item implementation of type {@link I}
     */
    I loadCopy();

    /**
     * Loads the {@link I}'s changes to the {@link Item} instance.
     */
    void update();
}
