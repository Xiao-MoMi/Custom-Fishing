package net.momirealms.customfishing.api.util;

import net.momirealms.customfishing.api.common.Pair;

import java.util.*;

public class WeightUtils {

    public static <T> T getRandom(List<Pair<T, Double>> pairs) {
        List<T> available = new ArrayList<>();
        double[] weights = new double[pairs.size()];
        int index = 0;
        for (Pair<T, Double> pair : pairs){
            double weight = pair.right();
            T key = pair.left();
            if (weight <= 0) continue;
            available.add(key);
            weights[index++] = weight;
        }
        return getRandom(weights, available, index);
    }

    public static <T> T getRandom(HashMap<T, Double> map) {
        List<T> available = new ArrayList<>();
        double[] weights = new double[map.size()];
        int index = 0;
        for (Map.Entry<T, Double> entry : map.entrySet()){
            double weight = entry.getValue();
            T key = entry.getKey();
            if (weight <= 0) continue;
            available.add(key);
            weights[index++] = weight;
        }
        return getRandom(weights, available, index);
    }

    private static <T> T getRandom(double[] weights, List<T> available, int effectiveSize) {
        double total = Arrays.stream(weights).sum();
        double[] weightRatios = new double[effectiveSize];
        for (int i = 0; i < effectiveSize; i++){
            weightRatios[i] = weights[i]/total;
        }
        double[] weightRange = new double[effectiveSize];
        double startPos = 0;
        for (int i = 0; i < effectiveSize; i++) {
            weightRange[i] = startPos + weightRatios[i];
            startPos += weightRatios[i];
        }
        double random = Math.random();
        int pos = Arrays.binarySearch(weightRange, random);

        if (pos < 0) {
            pos = -pos - 1;
        }
        if (pos < weightRange.length && random < weightRange[pos]) {
            return available.get(pos);
        }
        return null;
    }
}
