package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.LootBaseEffect;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.misc.function.*;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.util.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ItemLootConfig {

    private final String id;
    private final String material;
    private final List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers = new ArrayList<>();
    private final List<Consumer<LootBaseEffect.Builder>> effectBuilderConsumers = new ArrayList<>();
    private final List<Consumer<Loot.Builder>> lootBuilderConsumers = new ArrayList<>();

    public ItemLootConfig(String id, Section section, Map<String, Node<ConfigParserFunction>> functionMap) {
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
                switch (function.type()) {
                    case ITEM -> {
                        ItemParserFunction propertyFunction = (ItemParserFunction) function;
                        BiConsumer<Item<ItemStack>, Context<Player>> result = propertyFunction.accept(entry.getValue());
                        tagConsumers.add(new PriorityFunction<>(propertyFunction.getPriority(), result));
                    }
                    case BASE_EFFECT -> {
                        BaseEffectParserFunction baseEffectParserFunction = (BaseEffectParserFunction) function;
                        Consumer<LootBaseEffect.Builder> consumer = baseEffectParserFunction.accept(entry.getValue());
                        effectBuilderConsumers.add(consumer);
                    }
                    case LOOT -> {
                        LootParserFunction lootParserFunction = (LootParserFunction) function;
                        Consumer<Loot.Builder> consumer = lootParserFunction.accept(entry.getValue());
                        lootBuilderConsumers.add(consumer);
                    }
                    case EVENT -> {

                    }
                }
                continue;
            }
            if (entry.getValue() instanceof Section innerSection) {
                analyze(innerSection, node.getChildTree());
            }
        }
    }

    public Key key() {
        return Key.of("item", id);
    }

    public CustomFishingItem getItem() {
        return CustomFishingItem.builder()
                .material(material)
                .tagConsumers(tagConsumers)
                .build();
    }

    private LootBaseEffect getBaseEffect() {
        LootBaseEffect.Builder builder = LootBaseEffect.builder();
        for (Consumer<LootBaseEffect.Builder> consumer : effectBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }

    public Loot getLoot() {
        Loot.Builder builder = Loot.builder();
        builder.id(id);
        builder.lootBaseEffect(getBaseEffect());
        for (Consumer<Loot.Builder> consumer : lootBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }
}
