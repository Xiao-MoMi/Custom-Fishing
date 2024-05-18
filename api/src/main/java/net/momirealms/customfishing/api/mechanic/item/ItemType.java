package net.momirealms.customfishing.api.mechanic.item;

import java.util.Objects;

public class ItemType {

    public static ItemType LOOT = of("loot");

    private final String type;

    public ItemType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ItemType of(String type) {
        return new ItemType(type);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ItemType itemType = (ItemType) object;
        return Objects.equals(type, itemType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }
}
