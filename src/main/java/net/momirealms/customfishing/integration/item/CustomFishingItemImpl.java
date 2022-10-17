package net.momirealms.customfishing.integration.item;

import net.momirealms.customfishing.integration.ItemInterface;
import net.momirealms.customfishing.manager.LootManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CustomFishingItemImpl implements ItemInterface {

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (material.contains(":")) return null;
        return LootManager.build(material);
    }
}
