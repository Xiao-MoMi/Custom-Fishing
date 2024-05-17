package net.momirealms.customfishing.api.mechanic.item;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.function.PriorityFunction;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.TreeSet;
import java.util.function.BiConsumer;

public class CustomFishingItemImpl implements CustomFishingItem {

    private final String material;

    private final List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers;

    public CustomFishingItemImpl(String material, List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers) {
        this.material = material;
        this.tagConsumers = tagConsumers;
    }

    @Override
    public String material() {
        return material;
    }

    @Override
    public List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers() {
        return tagConsumers;
    }

    public static class BuilderImpl implements Builder {

        private String material = DEFAULT_MATERIAL;

        private final TreeSet<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers = new TreeSet<>();

        @Override
        public Builder material(String material) {
            this.material = material;
            return this;
        }

        @Override
        public Builder tagConsumers(List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers) {
            this.tagConsumers.addAll(tagConsumers);
            return this;
        }

        @Override
        public CustomFishingItem build() {
            return new CustomFishingItemImpl(material, tagConsumers.stream().map(PriorityFunction::get).toList());
        }
    }
}
