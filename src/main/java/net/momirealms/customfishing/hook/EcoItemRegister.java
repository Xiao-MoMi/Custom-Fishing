package net.momirealms.customfishing.hook;

import com.willfp.eco.core.items.CustomItem;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
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
                            NBTItem nbtItem = new NBTItem(itemStack);
                            NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                            return  nbtCompound != null
                                    && nbtCompound.getString("type").equalsIgnoreCase("rod")
                                    && nbtCompound.getString("id").equalsIgnoreCase(entry.getKey());
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
                            NBTItem nbtItem = new NBTItem(itemStack);
                            NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                            return  nbtCompound != null
                                    && nbtCompound.getString("type").equalsIgnoreCase("bait")
                                    && nbtCompound.getString("id").equalsIgnoreCase(entry.getKey());
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
                            NBTItem nbtItem = new NBTItem(itemStack);
                            NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
                            return  nbtCompound != null
                                    && nbtCompound.getString("type").equalsIgnoreCase("util")
                                    && nbtCompound.getString("id").equalsIgnoreCase(entry.getKey());
                        } catch (Exception e) {
                            return false;
                        }
                    },
                    entry.getValue()
            ).register();
        }
    }
}
