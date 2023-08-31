package net.momirealms.customfishing.api.mechanic.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public interface BuildableItem {

    default ItemStack build() {
        return build(null, new HashMap<>());
    }

    default ItemStack build(Player player) {
        return build(player, new HashMap<>());
    }

    ItemStack build(Player player, Map<String, String> placeholders);

    /**
     * Whether the item would be removed from cache when reloading
     */
    boolean persist();
}
