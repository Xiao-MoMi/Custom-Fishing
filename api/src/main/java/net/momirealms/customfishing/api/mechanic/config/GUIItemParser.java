package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.config.function.ConfigParserFunction;
import net.momirealms.customfishing.api.mechanic.config.function.ItemParserFunction;
import net.momirealms.customfishing.api.mechanic.config.function.PriorityFunction;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GUIItemParser {

    private final String id;
    private final String material;
    private final List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers = new ArrayList<>();

    public GUIItemParser(String id, Section section, Map<String, Node<ConfigParserFunction>> functionMap) {
        this.id = id;
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
                .tagConsumers(tagConsumers)
                .build();
    }
}