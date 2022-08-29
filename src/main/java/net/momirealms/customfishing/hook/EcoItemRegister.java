package net.momirealms.customfishing.hook;

import com.willfp.eco.core.items.CustomItem;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class EcoItemRegister {
    public static void registerItems() {
        // Rods
        for (Map.Entry<String, ItemStack> entry : ConfigReader.RodItem.entrySet()) {
            new CustomItem(
                    new NamespacedKey(CustomFishing.instance, "rod_" + entry.getKey()),
                    itemStack -> {
                        try {
                            return itemStack.getItemMeta().getPersistentDataContainer()
                                    .get(new NamespacedKey(CustomFishing.instance, "type"),
                                            PersistentDataType.STRING).equalsIgnoreCase("rod")
                                    && itemStack.getItemMeta().getPersistentDataContainer()
                                    .get(new NamespacedKey(CustomFishing.instance, "id"),
                                            PersistentDataType.STRING).equalsIgnoreCase(entry.getKey());
                        } catch (Exception e) {
                            return false;
                        }
                    },
                    entry.getValue()
            ).register();
        }
        // Baits
        for (Map.Entry<String, ItemStack> entry : ConfigReader.BaitItem.entrySet()) {
            new CustomItem(
                    new NamespacedKey(CustomFishing.instance, "bait_" + entry.getKey()),
                    itemStack -> {
                        try {
                            return itemStack.getItemMeta().getPersistentDataContainer()
                                    .get(new NamespacedKey(CustomFishing.instance, "type"),
                                            PersistentDataType.STRING).equalsIgnoreCase("bait")
                                    && itemStack.getItemMeta().getPersistentDataContainer()
                                    .get(new NamespacedKey(CustomFishing.instance, "id"),
                                            PersistentDataType.STRING).equalsIgnoreCase(entry.getKey());
                        } catch (Exception e) {
                            return false;
                        }
                    },
                    entry.getValue()
            ).register();
        }
        // Utils
        for (Map.Entry<String, ItemStack> entry : ConfigReader.UtilItem.entrySet()) {
            new CustomItem(
                    new NamespacedKey(CustomFishing.instance, "util_" + entry.getKey()),
                    itemStack -> {
                        try {
                            return itemStack.getItemMeta().getPersistentDataContainer()
                                    .get(new NamespacedKey(CustomFishing.instance, "type"),
                                            PersistentDataType.STRING).equalsIgnoreCase("util")
                                    && itemStack.getItemMeta().getPersistentDataContainer()
                                    .get(new NamespacedKey(CustomFishing.instance, "id"),
                                            PersistentDataType.STRING).equalsIgnoreCase(entry.getKey());
                        } catch (Exception e) {
                            return false;
                        }
                    },
                    entry.getValue()
            ).register();
        }
    }
}
