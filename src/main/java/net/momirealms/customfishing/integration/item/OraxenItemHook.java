package net.momirealms.customfishing.integration.item;

import io.th0rgal.oraxen.items.ItemBuilder;
import io.th0rgal.oraxen.items.OraxenItems;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class OraxenItemHook implements ItemInterface {

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (!material.startsWith("Oraxen:")) return null;
        material = material.substring(7);
        ItemBuilder itemBuilder = OraxenItems.getItemById(material);
        return itemBuilder == null ? null : itemBuilder.build();
    }
}
