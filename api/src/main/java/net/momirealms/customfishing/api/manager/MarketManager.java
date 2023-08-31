package net.momirealms.customfishing.api.manager;

import org.bukkit.inventory.ItemStack;

public interface MarketManager {
    int getDate();

    double getItemPrice(ItemStack itemStack);

    String getFormula();

    double getPrice(float base, float bonus, float size);
}
