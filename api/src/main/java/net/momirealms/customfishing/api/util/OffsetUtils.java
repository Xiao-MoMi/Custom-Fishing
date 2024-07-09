/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import org.jetbrains.annotations.ApiStatus;

/**
 * Utility class for generating offset characters based on a font configuration.
 */
@SuppressWarnings("DuplicatedCode")
public class OffsetUtils {

    private OffsetUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    private static String font;
    private static String negative_1;
    private static String negative_2;
    private static String negative_4;
    private static String negative_8;
    private static String negative_16;
    private static String negative_32;
    private static String negative_64;
    private static String negative_128;
    private static String positive_1;
    private static String positive_2;
    private static String positive_4;
    private static String positive_8;
    private static String positive_16;
    private static String positive_32;
    private static String positive_64;
    private static String positive_128;

    @ApiStatus.Internal
    public static void load(Section section) {
        if (section != null) {
            font = section.getString("font", "customfishing:offset_chars");
            positive_1 = section.getString("1");
            positive_2 = section.getString("2");
            positive_4 = section.getString("4");
            positive_8 = section.getString("8");
            positive_16 = section.getString("16");
            positive_32 = section.getString("32");
            positive_64 = section.getString("64");
            positive_128 = section.getString("128");
            negative_1 = section.getString("-1");
            negative_2 = section.getString("-2");
            negative_4 = section.getString("-4");
            negative_8 = section.getString("-8");
            negative_16 = section.getString("-16");
            negative_32 = section.getString("-32");
            negative_64 = section.getString("-64");
            negative_128 = section.getString("-128");
        }
    }

    /**
     * Get the shortest negative offset characters for a given number.
     *
     * @param n The number for which to generate offset characters.
     * @return Offset characters as a string.
     */
    public static String getShortestNegChars(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        while (n >= 128) {
            stringBuilder.append(negative_128);
            n -= 128;
        }
        if (n - 64 >= 0) {
            stringBuilder.append(negative_64);
            n -= 64;
        }
        if (n - 32 >= 0) {
            stringBuilder.append(negative_32);
            n -= 32;
        }
        if (n - 16 >= 0) {
            stringBuilder.append(negative_16);
            n -= 16;
        }
        if (n - 8 >= 0) {
            stringBuilder.append(negative_8);
            n -= 8;
        }
        if (n - 4 >= 0) {
            stringBuilder.append(negative_4);
            n -= 4;
        }
        if (n - 2 >= 0) {
            stringBuilder.append(negative_2);
            n -= 2;
        }
        if (n - 1 >= 0) {
            stringBuilder.append(negative_1);
        }
        return stringBuilder.toString();
    }

    /**
     * Get the shortest positive offset characters for a given number.
     *
     * @param n The number for which to generate offset characters.
     * @return Offset characters as a string.
     */
    public static String getShortestPosChars(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        while (n >= 128) {
            stringBuilder.append(positive_128);
            n -= 128;
        }
        if (n - 64 >= 0) {
            stringBuilder.append(positive_64);
            n -= 64;
        }
        if (n - 32 >= 0) {
            stringBuilder.append(positive_32);
            n -= 32;
        }
        if (n - 16 >= 0) {
            stringBuilder.append(positive_16);
            n -= 16;
        }
        if (n - 8 >= 0) {
            stringBuilder.append(positive_8);
            n -= 8;
        }
        if (n - 4 >= 0) {
            stringBuilder.append(positive_4);
            n -= 4;
        }
        if (n - 2 >= 0) {
            stringBuilder.append(positive_2);
            n -= 2;
        }
        if (n - 1 >= 0) {
            stringBuilder.append(positive_1);
        }
        return stringBuilder.toString();
    }

    /**
     * Get offset characters for a given number. This method selects between positive and negative
     * offset characters based on the sign of the number.
     *
     * @param n The number for which to generate offset characters.
     * @return Offset characters as a string.
     */
    public static String getOffsetChars(int n) {
        if (n > 0) {
            return AdventureHelper.surroundWithMiniMessageFont(getShortestPosChars(n), font);
        } else {
            return AdventureHelper.surroundWithMiniMessageFont(getShortestNegChars(-n), font);
        }
    }
}
