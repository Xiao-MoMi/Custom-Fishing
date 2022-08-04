package net.momirealms.customfishing.item;

import net.momirealms.customfishing.utils.Enchantment;
import org.bukkit.inventory.ItemFlag;

import java.util.List;
import java.util.Map;

public interface Item {
    String getMaterial();
    List<Enchantment> getEnchantments();
    List<ItemFlag> getItemFlags();
    String getName();
    List<String> getLore();
    Map<String,Object> getNbt();
    int getCustomModelData();
    boolean isUnbreakable();
}
