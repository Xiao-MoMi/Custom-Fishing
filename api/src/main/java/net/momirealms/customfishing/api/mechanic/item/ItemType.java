package net.momirealms.customfishing.api.mechanic.item;

import java.util.Objects;

public class ItemType {

    public static final ItemType LOOT = of("loot");
    public static final ItemType ROD = of("rod");
    public static final ItemType UTIL = of("util");
    public static final ItemType BAIT = of("bait");
    public static final ItemType HOOK = of("hook");

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
