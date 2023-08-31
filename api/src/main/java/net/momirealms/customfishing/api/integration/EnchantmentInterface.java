package net.momirealms.customfishing.api.integration;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface EnchantmentInterface {
    List<String> getEnchants(ItemStack itemStack);
}
