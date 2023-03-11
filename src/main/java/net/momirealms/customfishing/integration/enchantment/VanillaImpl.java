package net.momirealms.customfishing.integration.enchantment;

import net.momirealms.customfishing.integration.EnchantmentInterface;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VanillaImpl implements EnchantmentInterface {

    @Override
    public List<String> getEnchants(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        List<String> enchants = new ArrayList<>();
        for (Map.Entry<Enchantment, Integer> en : enchantments.entrySet()) {
            String key = en.getKey().getKey() + ":" + en.getValue();
            enchants.add(key);
        }
        return enchants;
    }
}