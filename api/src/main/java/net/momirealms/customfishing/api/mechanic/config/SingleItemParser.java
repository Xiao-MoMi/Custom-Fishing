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

package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.config.function.ConfigParserFunction;
import net.momirealms.customfishing.api.mechanic.config.function.ItemParserFunction;
import net.momirealms.customfishing.api.mechanic.config.function.PriorityFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class SingleItemParser {

    private final String id;
    private final String material;
    private final MathValue<Player> amount;
    private final List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers = new ArrayList<>();

    public SingleItemParser(String id, Section section, Map<String, Node<ConfigParserFunction>> functionMap) {
        this.id = id;
        if (section == null) {
            this.material = "AIR";
            this.amount = MathValue.plain(1);
            return;
        }
        this.amount = MathValue.auto(section.get("amount", 1), true);
        this.material = section.getString("material");
        analyze(section, functionMap);
    }

    private void analyze(Section section, Map<String, Node<ConfigParserFunction>> functionMap) {
        Map<String, Object> dataMap = section.getStringRouteMappedValues(false);
        for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
            String key = entry.getKey();
            Node<ConfigParserFunction> node = functionMap.get(key);
            if (node == null) continue;
            ConfigParserFunction function = node.nodeValue();
            if (function != null) {
                if (function instanceof ItemParserFunction propertyFunction) {
                    BiConsumer<Item<ItemStack>, Context<Player>> result = propertyFunction.accept(entry.getValue());
                    tagConsumers.add(new PriorityFunction<>(propertyFunction.getPriority(), result));
                }
                continue;
            }
            if (entry.getValue() instanceof Section innerSection) {
                analyze(innerSection, node.getChildTree());
            }
        }
    }

    public CustomFishingItem getItem() {
        return CustomFishingItem.builder()
                .material(material)
                .id(id)
                .amount(amount)
                .tagConsumers(tagConsumers)
                .build();
    }
}
