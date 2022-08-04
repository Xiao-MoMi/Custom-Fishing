package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.momirealms.customfishing.item.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemStackGenerator {

    public static ItemStack fromItem(Item item){

        ItemStack itemStack = new ItemStack(Material.valueOf(item.getMaterial().toUpperCase()));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (item.getCustomModelData() != 0){
            itemMeta.setCustomModelData(item.getCustomModelData());
        }
        if (item.isUnbreakable()){
            itemMeta.setUnbreakable(true);
        }
        if (item.getItemFlags() != null){
            item.getItemFlags().forEach(itemMeta::addItemFlags);
        }
        if (item.getEnchantments() != null) {
            if (itemStack.getType() == Material.ENCHANTED_BOOK){
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemMeta;
                item.getEnchantments().forEach(enchantment1 -> {
                    meta.addStoredEnchant(org.bukkit.enchantments.Enchantment.getByKey(enchantment1.getKey()),enchantment1.getLevel(),true);
                });
                itemStack.setItemMeta(meta);
            }else {
                item.getEnchantments().forEach(enchantment1 -> {
                    itemMeta.addEnchant(Enchantment.getByKey(enchantment1.getKey()),enchantment1.getLevel(),true);
                });
                itemStack.setItemMeta(itemMeta);
            }
        }else {
            itemStack.setItemMeta(itemMeta);
        }

        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound display = nbtItem.addCompound("display");
        if (item.getName() != null){
            String name  = item.getName();
            if (name.contains("&") || name.contains("§")){
                name = name.replaceAll("&","§");
                display.setString("Name", GsonComponentSerializer.gson().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(name)));
            }else {
                display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>" + name)));
            }
        }
        if(item.getLore() != null){
            List<String> lore = display.getStringList("Lore");
            item.getLore().forEach(line -> {
                if (line.contains("&") || line.contains("§")){
                    line = line.replaceAll("&","§");
                    lore.add(GsonComponentSerializer.gson().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(line)));
                }else {
                    lore.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>" + line)));
                }
            });
        }
        if (item.getNbt() != null){
            NBTUtil nbtUtil = new NBTUtil(item.getNbt(), nbtItem.getItem());
            return nbtUtil.getNBTItem().getItem();
        }
        return nbtItem.getItem();
    }
}
