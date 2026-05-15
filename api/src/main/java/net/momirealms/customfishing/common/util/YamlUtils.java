package net.momirealms.customfishing.common.util;

import dev.dejvokep.boostedyaml.block.implementation.Section;

import java.util.HashMap;
import java.util.Map;

public final class YamlUtils {
    private YamlUtils() {}

    public static void sectionToMap(Section section, Map<String, Object> outPut) {
        for (Map.Entry<String, Object> entry : section.getStringRouteMappedValues(false).entrySet()) {
            if (entry.getValue() instanceof Section inner) {
                HashMap<String, Object> map = new HashMap<>();
                outPut.put(entry.getKey(), map);
                sectionToMap(inner, map);
            } else {
                outPut.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
