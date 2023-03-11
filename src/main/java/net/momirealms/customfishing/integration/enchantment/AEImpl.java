package net.momirealms.customfishing.integration.enchantment;

import net.advancedplugins.ae.api.AEAPI;
import net.momirealms.customfishing.integration.EnchantmentInterface;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AEImpl implements EnchantmentInterface {

    @Override
    public List<String> getEnchants(ItemStack itemStack) {
        List<String> enchants = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : AEAPI.getEnchantmentsOnItem(itemStack).entrySet()) {
            enchants.add("AE:" + entry.getKey() + ":" + entry.getValue());
        }
        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        for (Map.Entry<Enchantment, Integer> en : enchantments.entrySet()) {
            String key = en.getKey().getKey() + ":" + en.getValue();
            enchants.add(key);
        }
        return enchants;
    }
}
