package net.momirealms.customfishing.api.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public interface MarketManager {
    void openMarketGUI(Player player);

    int getDate();

    double getItemPrice(ItemStack itemStack);

    String getFormula();

    double getPrice(float base, float bonus, float size);
}
