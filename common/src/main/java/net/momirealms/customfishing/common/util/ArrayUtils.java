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
