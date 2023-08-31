package net.momirealms.customfishing.api.manager;

import org.bukkit.inventory.Inventory;

import java.util.UUID;

public interface BagManager {
    boolean isBagEnabled();

    Inventory getOnlineBagInventory(UUID uuid);
}
