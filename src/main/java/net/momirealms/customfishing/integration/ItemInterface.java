package net.momirealms.customfishing.integration;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemInterface {

    @Nullable
    ItemStack build(String id);

}
