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

import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface ItemManager {

    /**
     * Build an ItemStack with a specified namespace and value for a player.
     *
     * @param player   The player for whom the ItemStack is being built.
     * @param namespace The namespace of the item.
     * @param value    The value of the item.
     * @return The constructed ItemStack.
     */
    @Nullable
    ItemStack build(Player player, String namespace, String value);

    /**
     * Build an ItemStack with a specified namespace and value, replacing placeholders,
     * for a player.
     *
     * @param player      The player for whom the ItemStack is being built.
     * @param namespace   The namespace of the item.
     * @param value       The value of the item.
     * @param placeholders The placeholders to replace in the item's attributes.
     * @return The constructed ItemStack, or null if the item doesn't exist.
     */
    @Nullable
    ItemStack build(Player player, String namespace, String value, Map<String, String> placeholders);

    /**
     * Build an ItemStack using an ItemBuilder for a player.
     *
     * @param player      The player for whom the ItemStack is being built.
     * @param builder     The ItemBuilder used to construct the ItemStack.
     * @return The constructed ItemStack.
     */
    @NotNull ItemStack build(Player player, ItemBuilder builder);

    /**
     * Build an ItemStack using the provided ItemBuilder, player, and placeholders.
     *
     * @param player       The player for whom the item is being built.
     * @param builder      The ItemBuilder that defines the item's properties.
     * @param placeholders A map of placeholders and their corresponding values to be applied to the item.
     * @return The constructed ItemStack.
     */
    @NotNull ItemStack build(Player player, ItemBuilder builder, Map<String, String> placeholders);

    /**
     * Build an ItemStack for a player based on the provided item ID.
     *
     * @param player The player for whom the ItemStack is being built.
     * @param id     The item ID, which include an identification (e.g., "Oraxen:id").
     * @return The constructed ItemStack or null if the ID is not valid.
     */
    @Nullable ItemStack buildAnyPluginItemByID(Player player, String id);

    /**
     * Get the item ID associated with the given ItemStack, if available.
     *
     * @param itemStack The ItemStack to retrieve the item ID from.
     * @return The item ID without type or null if not found or if the ItemStack is null or empty.
     */
    @Nullable String getCustomFishingItemID(ItemStack itemStack);

    /**
     * Get the item ID associated with the given ItemStack by checking all available item libraries.
     * The detection order is determined by the configuration.
     *
     * @param itemStack The ItemStack to retrieve the item ID from.
     * @return The item ID or "AIR" if not found or if the ItemStack is null or empty.
     */
    @NotNull String getAnyPluginItemID(ItemStack itemStack);

    /**
     * Create a ItemBuilder instance for an item configuration section
     * <p>
     * xxx_item:  <- section
     *   material: xxx
     *   custom-model-data: xxx
     *
     * @param section The configuration section containing item settings.
     * @param type The type of the item (e.g., "rod", "bait").
     * @param id The unique identifier for the item.
     * @return A CFBuilder instance representing the configured item, or null if the section is null.
     */
    @Nullable ItemBuilder getItemBuilder(ConfigurationSection section, String type, String id);

    /**
     * Get a set of all item keys in the CustomFishing plugin.
     *
     * @return A set of item keys.
     */
    Set<Key> getAllItemsKey();

    /**
     * Retrieve a BuildableItem by its namespace and value.
     *
     * @param namespace The namespace of the BuildableItem.
     * @param value     The value of the BuildableItem.
     * @return The BuildableItem with the specified namespace and value, or null if not found.
     */
    @Nullable
    BuildableItem getBuildableItem(String namespace, String value);

    ItemStack getItemStackAppearance(Player player, String material);

    /**
     * Register an item library.
     *
     * @param itemLibrary The item library to register.
     * @return True if the item library was successfully registered, false if it already exists.
     */
    boolean registerItemLibrary(ItemLibrary itemLibrary);

    /**
     * Unregister an item library.
     *
     * @param identification The item library to unregister.
     * @return True if the item library was successfully unregistered, false if it doesn't exist.
     */
    boolean unRegisterItemLibrary(String identification);

    /**
     * Drops an item based on the provided loot, applying velocity from a hook location to a player location.
     *
     * @param player         The player for whom the item is intended.
     * @param hookLocation   The location where the item will initially drop.
     * @param playerLocation The target location towards which the item's velocity is applied.
     * @param itemStack      The loot to drop
     * @param condition      A map of placeholders for item customization.
     */
    void dropItem(Player player, Location hookLocation, Location playerLocation, ItemStack itemStack, Condition condition);

    /**
     * Checks if the provided ItemStack is a custom fishing item
     *
     * @param itemStack The ItemStack to check.
     * @return True if the ItemStack is a custom fishing item; otherwise, false.
     */
    boolean isCustomFishingItem(ItemStack itemStack);

    /**
     * Decreases the durability of an ItemStack by a specified amount and optionally updates its lore.
     *
     * @param itemStack   The ItemStack to modify.
     * @param amount      The amount by which to decrease the durability.
     * @param updateLore  Whether to update the lore of the ItemStack.
     */
    void decreaseDurability(Player player, ItemStack itemStack, int amount, boolean updateLore);

    /**
     * Increases the durability of an ItemStack by a specified amount and optionally updates its lore.
     *
     * @param itemStack   The ItemStack to modify.
     * @param amount      The amount by which to increase the durability.
     * @param updateLore  Whether to update the lore of the ItemStack.
     */
    void increaseDurability(ItemStack itemStack, int amount, boolean updateLore);

    /**
     * Sets the durability of an ItemStack to a specific amount and optionally updates its lore.
     *
     * @param itemStack   The ItemStack to modify.
     * @param amount      The new durability value.
     * @param updateLore  Whether to update the lore of the ItemStack.
     */
    void setDurability(ItemStack itemStack, int amount, boolean updateLore);
}
