package net.momirealms.customfishing.api.data;

import com.google.gson.annotations.SerializedName;

public class InventoryData {

    @SerializedName("inventory")
    public String serialized;

    @SerializedName("size")
    public int size;

    public static InventoryData empty() {
        return new InventoryData("", 9);
    }

    public InventoryData(String serialized, int size) {
        this.serialized = serialized;
        this.size = size;
    }
}
