package net.momirealms.customfishing.api.mechanic.item;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.function.PriorityFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.BiConsumer;

public interface CustomFishingItem {

    String DEFAULT_MATERIAL = "PAPER";

    /**
     * Returns the material type of the custom fishing item.
     *
     * @return the material type as a String.
     */
    String material();

    String id();

    /**
     * Returns a list of tag consumers which are functions that take an item and context as parameters
     * and perform some operation on them.
     *
     * @return a list of BiConsumer instances.
     */
    List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers();

    default ItemStack build(Context<Player> context) {
        return BukkitCustomFishingPlugin.getInstance().getItemManager().build(context, this);
    }

    /**
     * Creates a new Builder instance to construct a CustomFishingItem.
     *
     * @return a new Builder instance.
     */
    static Builder builder() {
        return new CustomFishingItemImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing instances of CustomFishingItem.
     */
    interface Builder {

        Builder id(String id);

        /**
         * Sets the material type for the CustomFishingItem being built.
         *
         * @param material the material type as a String.
         * @return the Builder instance for method chaining.
         */
        Builder material(String material);

        /**
         * Sets the list of tag consumers for the CustomFishingItem being built.
         *
         * @param tagConsumers a list of BiConsumer instances.
         * @return the Builder instance for method chaining.
         */
        Builder tagConsumers(List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers);

        /**
         * Builds and returns a new CustomFishingItem instance.
         *
         * @return a new CustomFishingItem instance.
         */
        CustomFishingItem build();
    }
}
