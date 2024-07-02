package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.config.function.*;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.effect.LootBaseEffect;
import net.momirealms.customfishing.api.mechanic.event.EventCarrier;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.loot.LootType;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BaitConfigParser {

    private final String id;
    private final String material;
    private final List<PriorityFunction<BiConsumer<Item<ItemStack>, Context<Player>>>> tagConsumers = new ArrayList<>();
    private final List<Consumer<EventCarrier.Builder>> eventBuilderConsumers = new ArrayList<>();
    private final List<Consumer<EffectModifier.Builder>> effectBuilderConsumers = new ArrayList<>();
    private final List<Consumer<LootBaseEffect.Builder>> baseEffectBuilderConsumers = new ArrayList<>();
    private final List<Consumer<Loot.Builder>> lootBuilderConsumers = new ArrayList<>();

    public BaitConfigParser(String id, Section section, Map<String, Node<ConfigParserFunction>> functionMap) {
        this.id = id;
        this.material = section.getString("material");
        if (!section.contains("tag")) section.set("tag", true);
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
                    case BASE_EFFECT -> {
                        BaseEffectParserFunction baseEffectParserFunction = (BaseEffectParserFunction) function;
                        Consumer<LootBaseEffect.Builder> consumer = baseEffectParserFunction.accept(entry.getValue());
                        baseEffectBuilderConsumers.add(consumer);
                    }
                    case LOOT -> {
                        LootParserFunction lootParserFunction = (LootParserFunction) function;
                        Consumer<Loot.Builder> consumer = lootParserFunction.accept(entry.getValue());
                        lootBuilderConsumers.add(consumer);
                    }
                    case ITEM -> {
                        ItemParserFunction propertyFunction = (ItemParserFunction) function;
                        BiConsumer<Item<ItemStack>, Context<Player>> result = propertyFunction.accept(entry.getValue());
                        tagConsumers.add(new PriorityFunction<>(propertyFunction.getPriority(), result));
                    }
                    case EVENT -> {
                        EventParserFunction eventParserFunction = (EventParserFunction) function;
                        Consumer<EventCarrier.Builder> consumer = eventParserFunction.accept(entry.getValue());
                        eventBuilderConsumers.add(consumer);
                    }
                    case EFFECT_MODIFIER -> {
                        EffectModifierParserFunction effectModifierParserFunction = (EffectModifierParserFunction) function;
                        Consumer<EffectModifier.Builder> consumer = effectModifierParserFunction.accept(entry.getValue());
                        effectBuilderConsumers.add(consumer);
                    }
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

    public EventCarrier getEventCarrier() {
        EventCarrier.Builder builder = EventCarrier.builder()
                .id(id)
                .type(MechanicType.BAIT);
        for (Consumer<EventCarrier.Builder> consumer : eventBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }

    public EffectModifier getEffectModifier() {
        EffectModifier.Builder builder = EffectModifier.builder()
                .id(id)
                .type(MechanicType.BAIT);
        for (Consumer<EffectModifier.Builder> consumer : effectBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }

    private LootBaseEffect getBaseEffect() {
        LootBaseEffect.Builder builder = LootBaseEffect.builder();
        for (Consumer<LootBaseEffect.Builder> consumer : baseEffectBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }

    public Loot getLoot() {
        Loot.Builder builder = Loot.builder()
                .id(id)
                .type(LootType.ITEM)
                .lootBaseEffect(getBaseEffect());
        for (Consumer<Loot.Builder> consumer : lootBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }
}
