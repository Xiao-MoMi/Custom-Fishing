package net.momirealms.customfishing.api.util;

import net.momirealms.customfishing.api.mechanic.item.tag.TagValueType;
import net.momirealms.customfishing.common.util.Pair;

import java.util.Locale;

public class TagUtils {

    public static Pair<TagValueType, String> toTypeAndData(String str) {
        String[] parts = str.split("\\s+", 2);
        if (parts.length == 1) {
            return Pair.of(TagValueType.STRING, parts[0]);
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid value format: " + str);
        }
        TagValueType type = TagValueType.valueOf(parts[0].substring(1, parts[0].length() - 1).toUpperCase(Locale.ENGLISH));
        String data = parts[1];
        return Pair.of(type, data);
    }
}
