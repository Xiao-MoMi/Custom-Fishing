package net.momirealms.customfishing.hook;

import io.th0rgal.oraxen.items.OraxenItems;
import org.bukkit.inventory.ItemStack;

public class OraxenItem {

    public static ItemStack getItemStack(String namespacedID){
        return OraxenItems.getItemById(namespacedID).build();
    }
}
