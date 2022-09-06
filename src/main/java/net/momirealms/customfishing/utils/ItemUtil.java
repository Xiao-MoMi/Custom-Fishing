/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.hook.ItemsAdderItem;
import net.momirealms.customfishing.hook.MMOItemsHook;
import net.momirealms.customfishing.hook.MythicItems;
import net.momirealms.customfishing.hook.OraxenItem;
import net.momirealms.customfishing.listener.PapiUnregister;
import net.momirealms.customfishing.object.loot.DroppedItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemUtil {

    public static void givePlayerLoot(Player player, String lootKey, int amount){
        ItemStack itemStack = ConfigReader.LootItem.get(lootKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerRod(Player player, String rodKey, int amount){
        ItemStack itemStack = ConfigReader.RodItem.get(rodKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        ItemStack itemStack = ConfigReader.BaitItem.get(baitKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        ItemStack itemStack = ConfigReader.UtilItem.get(utilKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static ItemStack getItemStackFromOtherPlugins(String key){
        DroppedItem droppedItem = (DroppedItem) ConfigReader.LOOT.get(key);
        ItemStack itemStack = null;
        switch (droppedItem.getType()){
            case "ia" -> itemStack = ItemsAdderItem.getItemStack(droppedItem.getId()).clone();
            case "oraxen" -> itemStack = OraxenItem.getItemStack(droppedItem.getId()).clone();
            case "mm" -> itemStack = MythicItems.getItemStack(droppedItem.getId()).clone();
            case "mmoitems" -> itemStack = MMOItemsHook.getItemStack(droppedItem.getId()).clone();
        }
        return itemStack;
    }

    public static void saveToFile(ItemStack itemStack, String fileName){

        if (itemStack == null || itemStack.getType() == Material.AIR) return;

        YamlConfiguration yamlConfiguration = new YamlConfiguration();

        yamlConfiguration.set(fileName + ".material", itemStack.getType().toString());

        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta.hasDisplayName()){
            yamlConfiguration.set(fileName + ".display.name", replaceLegacy(itemMeta.getDisplayName()));
        }
        if (itemMeta.hasLore()){
            List<String> lore = new ArrayList<>();
            itemMeta.getLore().forEach(line -> {
                lore.add(replaceLegacy(line));
            });
            yamlConfiguration.set(fileName + ".display.lore", lore);
        }
        if (itemMeta.hasCustomModelData()) {
            yamlConfiguration.set(fileName + ".custom-model-data", itemMeta.getCustomModelData());
        }
        if (itemMeta.isUnbreakable()) {
            yamlConfiguration.set(fileName + ".unbreakable", itemMeta.isUnbreakable());
        }
        if (itemMeta.hasEnchants()) {
            Map<String, Integer> map = new HashMap<>();
            itemMeta.getEnchants().forEach((enchantment, level) -> {
                map.put(String.valueOf(enchantment.getKey()), level);
            });
            yamlConfiguration.createSection(fileName + ".enchantments", map);
        }
        if (itemMeta instanceof EnchantmentStorageMeta enchantmentStorageMeta){
            Map<String, Integer> map = new HashMap<>();
            enchantmentStorageMeta.getStoredEnchants().forEach(((enchantment, level) -> {
                map.put(String.valueOf(enchantment.getKey()), level);
            }));
            yamlConfiguration.createSection(fileName + ".enchantments", map);
        }
        if (itemMeta.getItemFlags().size() > 0){
            ArrayList<String> itemFlags = new ArrayList<>();
            itemStack.getItemFlags().forEach(itemFlag -> {
                itemFlags.add(itemFlag.name());
            });
            yamlConfiguration.set(fileName + ".item_flags", itemFlags);
        }

        NBTItem nbtItem = new NBTItem(itemStack);

        Map<String, Object> map0 = compoundToMap(nbtItem);
        if (map0.size() != 0){
            yamlConfiguration.createSection(fileName + ".nbt", map0);
        }

        File file = new File(CustomFishing.instance.getDataFolder(), File.separator + "loots" + File.separator + fileName + ".yml");

        try {
            yamlConfiguration.save(file);
            ConfigReader.loadLoot();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static Map<String, Object> compoundToMap(NBTCompound nbtCompound){
        Map<String, Object> map = new HashMap<>();
        nbtCompound.getKeys().forEach(key -> {
            if (key.equals("Enchantments")
                    || key.equals("Name")
                    || key.equals("Lore")
                    || key.equals("HideFlags")
                    || key.equals("CustomModelData")
                    || key.equals("StoredEnchantments")
                    || key.equals("Unbreakable")) return;
            switch (nbtCompound.getType(key)){
                case NBTTagByte -> map.put(key, "(Byte) " + nbtCompound.getByte(key));
                case NBTTagInt -> map.put(key, "(Int) " + nbtCompound.getInteger(key));
                case NBTTagDouble -> map.put(key, "(Double) " + nbtCompound.getDouble(key));
                case NBTTagLong -> map.put(key, "(Long) " + nbtCompound.getLong(key));
                case NBTTagFloat -> map.put(key, "(Float) " + nbtCompound.getFloat(key));
                case NBTTagShort -> map.put(key, "(Short) " + nbtCompound.getShort(key));
                case NBTTagString -> map.put(key, "(String) " + nbtCompound.getString(key));
                case NBTTagByteArray -> map.put(key, "(ByteArray) " + Arrays.toString(nbtCompound.getByteArray(key)));
                case NBTTagIntArray -> map.put(key, "(IntArray) " + Arrays.toString(nbtCompound.getIntArray(key)));
                case NBTTagCompound -> {
                    Map<String, Object> map1 = compoundToMap(nbtCompound.getCompound(key));
                    if (map1.size() != 0) map.put(key, map1);
                }
                case NBTTagList -> {
                    List<Object> list = new ArrayList<>();
                    switch (nbtCompound.getListType(key)){
                        case NBTTagInt -> nbtCompound.getIntegerList(key).forEach(a -> list.add("(Int) " + a));
                        case NBTTagDouble -> nbtCompound.getDoubleList(key).forEach(a -> list.add("(Double) " + a));
                        case NBTTagString -> nbtCompound.getStringList(key).forEach(a -> list.add("(String) " + a));
                        case NBTTagCompound -> nbtCompound.getCompoundList(key).forEach(a -> list.add(compoundToMap(a)));
                        case NBTTagFloat -> nbtCompound.getFloatList(key).forEach(a -> list.add("(Float) " + a));
                        case NBTTagLong -> nbtCompound.getLongList(key).forEach(a -> list.add("(Long) " + a));
                        case NBTTagIntArray -> nbtCompound.getIntArrayList(key).forEach(a -> list.add("(IntArray) " + Arrays.toString(a)));
                        default -> nbtCompound.getUUIDList(key).forEach(a -> list.add("(UUID) " + a));
                    }
                    map.put(key, list);
                }
            }
        });
        return map;
    }

    public static Component getDisplayName(ItemStack itemStack){
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.getCompound("display");
        if (nbtCompound != null){
            String name = nbtCompound.getString("Name");
            if (!name.equals("")){
                return GsonComponentSerializer.gson().deserialize(name);
            }
        }
        String type = itemStack.getType().toString().toLowerCase();
        if (itemStack.getType().isBlock()) return GsonComponentSerializer.gson().deserialize("{\"translate\":\"block.minecraft."+ type + "\"}");
        else return GsonComponentSerializer.gson().deserialize("{\"translate\":\"item.minecraft."+ type + "\"}");
    }

    public static String replaceLegacy(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '§') {
                if (i + 1 < chars.length) {
                    switch (chars[i+1]){
                        case '0' -> {
                            i++;
                            stringBuilder.append("<black>");
                        }
                        case '1' -> {
                            i++;
                            stringBuilder.append("<dark_blue>");
                        }
                        case '2' -> {
                            i++;
                            stringBuilder.append("<dark_green>");
                        }
                        case '3' -> {
                            i++;
                            stringBuilder.append("<dark_aqua>");
                        }
                        case '4' -> {
                            i++;
                            stringBuilder.append("<dark_red>");
                        }
                        case '5' -> {
                            i++;
                            stringBuilder.append("<dark_purple>");
                        }
                        case '6' -> {
                            i++;
                            stringBuilder.append("<gold>");
                        }
                        case '7' -> {
                            i++;
                            stringBuilder.append("<gray>");
                        }
                        case '8' -> {
                            i++;
                            stringBuilder.append("<dark_gray>");
                        }
                        case '9' -> {
                            i++;
                            stringBuilder.append("<blue>");
                        }
                        case 'a' -> {
                            i++;
                            stringBuilder.append("<green>");
                        }
                        case 'b' -> {
                            i++;
                            stringBuilder.append("<aqua>");
                        }
                        case 'c' -> {
                            i++;
                            stringBuilder.append("<red>");
                        }
                        case 'd' -> {
                            i++;
                            stringBuilder.append("<light_purple>");
                        }
                        case 'e' -> {
                            i++;
                            stringBuilder.append("<yellow>");
                        }
                        case 'f' -> {
                            i++;
                            stringBuilder.append("<white>");
                        }
                        case 'r' -> {
                            i++;
                            stringBuilder.append("<reset><!italic>");
                        }
                        case 'l' -> {
                            i++;
                            stringBuilder.append("<bold>");
                        }
                        case 'm' -> {
                            i++;
                            stringBuilder.append("<strikethrough>");
                        }
                        case 'o' -> {
                            i++;
                            stringBuilder.append("<italic>");
                        }
                        case 'n' -> {
                            i++;
                            stringBuilder.append("<underlined>");
                        }
                        case 'x' -> {
                            stringBuilder.append("<#").append(chars[i+3]).append(chars[i+5]).append(chars[i+7]).append(chars[i+9]).append(chars[i+11]).append(chars[i+13]).append(">");
                            i += 13;
                        }
                        case 'k' -> {
                            i++;
                            stringBuilder.append("<obfuscated>");
                        }
                    }
                }
            }
            else {
                stringBuilder.append(chars[i]);
            }
        }
        return stringBuilder.toString();
    }
}