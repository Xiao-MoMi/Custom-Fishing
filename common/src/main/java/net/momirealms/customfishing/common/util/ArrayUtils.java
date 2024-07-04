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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayUtils {

    private ArrayUtils() {}

    public static <T> T[] subArray(T[] array, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index should be a value no lower than 0");
        }
        if (array.length <= index) {
            @SuppressWarnings("unchecked")
            T[] emptyArray = (T[]) Array.newInstance(array.getClass().getComponentType(), 0);
            return emptyArray;
        }
        @SuppressWarnings("unchecked")
        T[] subArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - index);
        System.arraycopy(array, index, subArray, 0, array.length - index);
        return subArray;
    }

    public static <T> List<T[]> splitArray(T[] array, int chunkSize) {
        List<T[]> result = new ArrayList<>();
        for (int i = 0; i < array.length; i += chunkSize) {
            int end = Math.min(array.length, i + chunkSize);
            @SuppressWarnings("unchecked")
            T[] chunk = (T[]) Array.newInstance(array.getClass().getComponentType(), end - i);
            System.arraycopy(array, i, chunk, 0, end - i);
            result.add(chunk);
        }
        return result;
    }
}
