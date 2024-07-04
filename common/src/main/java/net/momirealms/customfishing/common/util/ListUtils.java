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
