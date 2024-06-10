package net.momirealms.customfishing.api.mechanic.item;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

public class ItemType {

    private static final HashMap<String, ItemType> types = new HashMap<>();

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

    public static void register(String id, ItemType type) {
        ItemType previous = types.put(id, type);
        if (previous != null) {
            BukkitCustomFishingPlugin.getInstance().getPluginLogger().warn(
                    "Attempted to register item type " + id + " twice, this is not a safe behavior. ["
                            + type.getType() + "," + previous.getType() + "]"
                    );
        }
    }

    @Nullable
    public static ItemType getTypeByID(String id) {
        return types.get(id);
    }

    public static void reset() {
        types.clear();
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
