package net.momirealms.customfishing.hook;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderItem {

    public static ItemStack getItemStack(String namespacedID){
        return CustomStack.getInstance(namespacedID).getItemStack();
    }
}
