package net.momirealms.customfishing.api.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class StatisticData {

    @SerializedName("stats")
    public Map<String, Integer> statisticMap;

    public StatisticData(@NotNull Map<String, Integer> data) {
        this.statisticMap = data;
    }

    public static StatisticData empty() {
        return new StatisticData(new HashMap<>());
    }
}
