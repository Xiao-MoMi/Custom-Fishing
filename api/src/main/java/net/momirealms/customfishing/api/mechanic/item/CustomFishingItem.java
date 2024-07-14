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

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.function.PriorityFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Interface representing a custom fishing item
 */
public interface CustomFishingItem {

    String DEFAULT_MATERIAL = "PAPER";

    /**
     * Returns the material type of the custom fishing item.
     *
     * @return the material type as a String.
     */
    String material();

    /**
     * Returns the unique identifier of the custom fishing item.
     *
     * @return the unique identifier as a String.
     */
    String id();

    /**
     * Returns a list of tag consumers. Tag consumers are functions that take an {@link Item} and a {@link Context}
     * as parameters and perform some operation on them.
     *
     * @return a list of {@link BiConsumer} instances.
     */
    List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers();

    /**
     * Builds the custom fishing item using the given context.
     *
     * @param context the {@link Context} in which the item is built.
     * @return the built {@link ItemStack}.
     */
    default ItemStack build(Context<Player> context) {
        return BukkitCustomFishingPlugin.getInstance().getItemManager().build(context, this);
    }

    /**
     * Creates a new {@link Builder} instance to construct a {@link CustomFishingItem}.
     *
     * @return a new {@link Builder} instance.
     */
    static Builder builder() {
        return new CustomFishingItemImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing instances of {@link CustomFishingItem}.
     */
    interface Builder {

        /**
         * Sets the unique identifier for the {@link CustomFishingItem} being built.
         *
         * @param id the unique identifier as a String.
         * @return the {@link Builder} instance for method chaining.
         */
        Builder id(String id);

        /**
         * Sets the material type for the {@link CustomFishingItem} being built.
         *
         * @param material the material type as a String.
         * @return the {@link Builder} instance for method chaining.
         */
        Builder material(String material);

        /**
         * Sets the list of tag consumers for the {@link CustomFishingItem} being built.
         * Tag consumers are functions that take an {@link Item} and a {@link Context} as parameters and perform some operation on them.
         *
         * @param tagConsumers a list of {@link PriorityFunction} instances wrapping {@link BiConsumer} functions.
         * @return the {@link Builder} instance for method chaining.
         */
        Builder tagConsumers(List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers);

        /**
         * Builds and returns a new {@link CustomFishingItem} instance.
         *
         * @return a new {@link CustomFishingItem} instance.
         */
        CustomFishingItem build();
    }
}
