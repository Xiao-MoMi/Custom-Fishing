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

package net.momirealms.customfishing.api.util;

import net.momirealms.customfishing.api.mechanic.item.tag.TagValueType;
import net.momirealms.customfishing.common.util.Pair;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

/**
 * Utility class for handling tag values.
 */
@ApiStatus.Internal
public class TagUtils {

    /**
     * Parses a string into a pair containing a {@link TagValueType} and its associated data.
     * The input string should be in the format "&lt;type&gt; data".
     *
     * @param str the string to be parsed
     * @return a {@link Pair} containing the {@link TagValueType} and its associated data
     * @throws IllegalArgumentException if the input string is in an invalid format
     */
    public static Pair<TagValueType, String> toTypeAndData(String str) {
        String[] parts = str.split("\\s+", 2);
        if (parts.length == 1) {
            return Pair.of(TagValueType.STRING, str);
        }
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid value format: " + str);
        }
        if (parts[0].startsWith("(") && parts[0].endsWith(")")) {
            TagValueType type = TagValueType.valueOf(parts[0].substring(1, parts[0].length() - 1).toUpperCase(Locale.ENGLISH));
            String data = parts[1];
            return Pair.of(type, data);
        } else {
            return Pair.of(TagValueType.STRING, str);
        }
    }
}
