package net.momirealms.customfishing.common.util;

import java.util.List;

public class ListUtils {

    private ListUtils() {
    }

    @SuppressWarnings("unchecked")
    public static List<String> toList(final Object obj) {
        if (obj instanceof String s) {
            return List.of(s);
        } else if (obj instanceof List<?> list) {
            return (List<String>) list;
        }
        throw new IllegalArgumentException("Cannot convert " + obj + " to a list");
    }
}
