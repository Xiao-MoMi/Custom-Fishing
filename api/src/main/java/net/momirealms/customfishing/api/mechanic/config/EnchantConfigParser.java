package net.momirealms.customfishing.api.mechanic.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.config.function.ConfigParserFunction;
import net.momirealms.customfishing.api.mechanic.config.function.EffectModifierParserFunction;
import net.momirealms.customfishing.api.mechanic.config.function.EventParserFunction;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.event.EventCarrier;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.common.config.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EnchantConfigParser {

    private final String id;
    private final List<Consumer<EventCarrier.Builder>> eventBuilderConsumers = new ArrayList<>();
    private final List<Consumer<EffectModifier.Builder>> effectBuilderConsumers = new ArrayList<>();

    public EnchantConfigParser(String id, Section section, Map<String, Node<ConfigParserFunction>> functionMap) {
        this.id = id;
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

    public EventCarrier getEventCarrier() {
        EventCarrier.Builder builder = EventCarrier.builder()
                .id(id)
                .type(MechanicType.ENCHANT);
        for (Consumer<EventCarrier.Builder> consumer : eventBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }

    public EffectModifier getEffectModifier() {
        EffectModifier.Builder builder = EffectModifier.builder()
                .id(id)
                .type(MechanicType.ENCHANT);
        for (Consumer<EffectModifier.Builder> consumer : effectBuilderConsumers) {
            consumer.accept(builder);
        }
        return builder.build();
    }
}
