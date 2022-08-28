package net.momirealms.customfishing.object;

import org.bukkit.inventory.ItemStack;

public class VanillaLoot {

    private final ItemStack itemStack;
    private final int xp;

    public VanillaLoot(ItemStack itemStack, int xp) {
        this.itemStack = itemStack;
        this.xp = xp;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public int getXp() {
        return xp;
    }
}
