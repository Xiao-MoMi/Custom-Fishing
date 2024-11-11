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

package net.momirealms.customfishing.api.mechanic.item;

import com.saicone.rtag.RtagItem;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.item.ItemFactory;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Interface for managing custom fishing items
 */
public interface ItemManager extends Reloadable {

    /**
     * Registers a new custom fishing item.
     *
     * @param item the {@link CustomFishingItem} to be registered
     * @return true if the item was successfully registered, false otherwise
     */
    boolean registerItem(@NotNull CustomFishingItem item);

    /**
     * Builds an internal representation of an item using the given context and item ID.
     *
     * @param context the {@link Context} in which the item is built
     * @param id      the ID of the item to be built
     * @return the built {@link ItemStack}
     * @throws NullPointerException if the item ID is not found
     */
    @Nullable
    ItemStack buildInternal(@NotNull Context<Player> context, @NotNull String id) throws NullPointerException;

    /**
     * Builds a custom fishing item using the given context and item definition.
     *
     * @param context the {@link Context} in which the item is built
     * @param item    the {@link CustomFishingItem} definition
     * @return the built {@link ItemStack}
     */
    ItemStack build(@NotNull Context<Player> context, @NotNull CustomFishingItem item);

    /**
     * Builds any item using the given context and item ID. Example: {@code CustomFishing:ID} / {@code Oraxen:ID} / {@code ItemsAdder:namespace:id}
     *
     * @param context the {@link Context} in which the item is built
     * @param id      the ID of the item to be built
     * @return the built {@link ItemStack}, or null if the item ID is not found
     */
    @Nullable
    ItemStack buildAny(@NotNull Context<Player> context, @NotNull String id);

    /**
     * Retrieves the item ID of the given item stack. If it's a vanilla item, the returned value would be capitalized for instance {@code PAPER}. If it's a CustomFishing
     * item, the returned value would be the ID for instance {@code beginner_rod}. If it's an item from other plugins, the returned value would be the
     * id from that plugin for instance {@code itemsadder_namespace:id} / {@code oraxen_item_id}
     *
     * @param itemStack the {@link ItemStack} to be checked
     * @return the custom fishing item ID, or null if the item stack is not a custom fishing item
     */
    @NotNull
    String getItemID(@NotNull ItemStack itemStack);

    /**
     * Retrieves the custom fishing item ID if the given item stack is a custom fishing item.
     *
     * @param itemStack the {@link ItemStack} to be checked
     * @return the custom fishing item ID, or null if the item stack is not a custom fishing item
     */
    @Nullable
    String getCustomFishingItemID(@NotNull ItemStack itemStack);

    /**
     * Gets the loot by providing the context
     *
     * @param context context
     * @param rod rod
     * @param hook hook
     * @return the loot
     */
    @NotNull
    ItemStack getItemLoot(@NotNull Context<Player> context, ItemStack rod, FishHook hook);

    /**
     * Drops a custom fishing item as loot.
     *
     * @param context the {@link Context} in which the item is dropped
     * @param rod     the fishing rod {@link ItemStack}
     * @param hook    the {@link FishHook} entity
     * @return the dropped {@link Item} entity
     */
    @Nullable
    Item dropItemLoot(@NotNull Context<Player> context, ItemStack rod, FishHook hook);

    /**
     * Checks if the given item stack has custom durability.
     *
     * @param itemStack the {@link ItemStack} to be checked
     * @return true if the item stack has custom durability, false otherwise
     */
    boolean hasCustomMaxDamage(ItemStack itemStack);

    /**
     * Gets the maximum damage value for the given item stack.
     *
     * @param itemStack the {@link ItemStack} to be checked
     * @return the maximum damage value
     */
    int getMaxDamage(ItemStack itemStack);

    /**
     * Decreases the damage of the given item stack.
     *
     * @param player     the {@link Player} holding the item
     * @param itemStack  the {@link ItemStack} to be modified
     * @param amount     the amount to decrease the damage by
     */
    void decreaseDamage(Player player, ItemStack itemStack, int amount);

    /**
     * Increases the damage of the given item stack.
     *
     * @param player         the {@link Player} holding the item
     * @param itemStack      the {@link ItemStack} to be modified
     * @param amount         the amount to increase the damage by
     * @param incorrectUsage true if the damage increase is due to incorrect usage, false otherwise
     */
    void increaseDamage(Player player, ItemStack itemStack, int amount, boolean incorrectUsage);

    /**
     * Sets the damage of the given item stack.
     *
     * @param player    the {@link Player} holding the item
     * @param itemStack the {@link ItemStack} to be modified
     * @param damage    the new damage value
     */
    void setDamage(Player player, ItemStack itemStack, int damage);

    /**
     * Returns the item factory used to create custom fishing items.
     *
     * @return the {@link ItemFactory} instance
     */
    ItemFactory<CustomFishingPlugin, RtagItem, ItemStack> getFactory();

    /**
     * Returns an array of item providers used to manage custom fishing items.
     *
     * @return an array of {@link ItemProvider} instances
     */
    ItemProvider[] getItemProviders();

    /**
     * Returns a collection of all registered item IDs.
     *
     * @return a collection of item ID strings
     */
    Collection<String> getItemIDs();

    /**
     * Wraps the given item stack in a custom fishing item wrapper.
     *
     * @param itemStack the {@link ItemStack} to be wrapped
     * @return the wrapped {@link net.momirealms.customfishing.common.item.Item} instance
     */
    net.momirealms.customfishing.common.item.Item<ItemStack> wrap(ItemStack itemStack);
}
