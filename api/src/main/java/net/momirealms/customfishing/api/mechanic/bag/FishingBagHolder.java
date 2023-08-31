package net.momirealms.customfishing.api.mechanic.bag;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FishingBagHolder implements InventoryHolder {

    private final UUID owner;
    private Inventory inventory;

    public FishingBagHolder(UUID owner) {
        this.owner = owner;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setItems(ItemStack[] itemStacks) {
        this.inventory.setContents(itemStacks);
    }

    public UUID getOwner() {
        return owner;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}