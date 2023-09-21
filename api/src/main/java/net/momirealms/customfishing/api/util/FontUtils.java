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

/**
 * Utility class for working with fonts in text.
 */
public class FontUtils {

    private FontUtils() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }

    /**
     * Surrounds the given text with a specified font tag.
     *
     * @param text The text to be surrounded with the font tag.
     * @param font The font to use in the font tag.
     * @return The input text surrounded by the font tag.
     */
    public static String surroundWithFont(String text, String font) {
        return "<font:" + font + ">" + text + "</font>";
    }
}
