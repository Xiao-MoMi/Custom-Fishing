package net.momirealms.customfishing.integration.item;

import dev.lone.itemsadder.api.CustomStack;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderItemImpl implements ItemInterface {

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (!material.startsWith("ItemsAdder:")) return null;
        material = material.substring(11);
        CustomStack customStack = CustomStack.getInstance(material);
        return customStack == null ? null : customStack.getItemStack();
    }
}
