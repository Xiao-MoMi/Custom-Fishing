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

import net.momirealms.customfishing.api.mechanic.config.function.PriorityFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public class CustomFishingItemImpl implements CustomFishingItem {

    private final String material;
    private final String id;
    private final MathValue<Player> amount;

    private final List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers;

    public CustomFishingItemImpl(String id, String material, MathValue<Player> amount, List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers) {
        this.material = material;
        this.id = id;
        this.tagConsumers = tagConsumers;
        this.amount = amount;
    }

    @Override
    public String material() {
        return Optional.ofNullable(material).orElse("AIR");
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public MathValue<Player> amount() {
        return amount == null ? MathValue.plain(1) : amount;
    }

    @Override
    public List<BiConsumer<Item<ItemStack>, Context<Player>>> tagConsumers() {
        return tagConsumers;
    }

    public static class BuilderImpl implements Builder {

        private String material = DEFAULT_MATERIAL;
        private String id;
        private MathValue<Player> amount;
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
        public Builder amount(MathValue<Player> amount) {
            this.amount = requireNonNull(amount);
            return this;
        }

        @Override
        public Builder tagConsumers(List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers) {
            this.tagConsumers.addAll(tagConsumers);
            return this;
        }

        @Override
        public CustomFishingItem build() {
            return new CustomFishingItemImpl(requireNonNull(id), material, amount, tagConsumers.stream().map(PriorityFunction::get).toList());
        }
    }
}
