package net.momirealms.customfishing.api.mechanic.item;

import net.momirealms.customfishing.api.mechanic.config.function.PriorityFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public class CustomFishingItemImpl implements CustomFishingItem {

    private final String material;
    private final String id;

    private final List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers;

    public CustomFishingItemImpl(String id, String material, List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers) {
        this.material = material;
        this.id = id;
        this.tagConsumers = tagConsumers;
    }

    @Override
    public String material() {
        return material;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers() {
        return tagConsumers;
    }

    public static class BuilderImpl implements Builder {

        private String material = DEFAULT_MATERIAL;
        private String id;
        private final TreeSet<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers = new TreeSet<>();

        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }

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
            return new CustomFishingItemImpl(requireNonNull(id), material, tagConsumers.stream().map(PriorityFunction::get).toList());
        }
    }
}
