package net.momirealms.customfishing.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.manager.BonusManager;
import net.momirealms.customfishing.manager.LootManager;
import net.momirealms.customfishing.object.Item;
import net.momirealms.customfishing.object.LeveledEnchantment;
import net.momirealms.customfishing.object.loot.DroppedItem;
import net.momirealms.customfishing.object.loot.Loot;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ItemStackUtil {

    public static ItemStack getFromItem(Item item) {
        ItemStack itemStack = new ItemStack(item.getMaterial());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (item.getCustomModelData() != 0) itemMeta.setCustomModelData(item.getCustomModelData());
        if (item.isUnbreakable()) itemMeta.setUnbreakable(true);
        if (item.getItemFlags() != null) item.getItemFlags().forEach(itemMeta::addItemFlags);
        if (item.getEnchantment() != null) {
            if (itemStack.getType() == Material.ENCHANTED_BOOK){
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemMeta;
                item.getEnchantment().forEach(enchantment -> meta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.getKey())),enchantment.getLevel(),true));
                itemStack.setItemMeta(meta);
            }
            else {
                item.getEnchantment().forEach(enchantment -> itemMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.getKey())),enchantment.getLevel(),true));
                itemStack.setItemMeta(itemMeta);
            }
        }
        else {
            itemStack.setItemMeta(itemMeta);
        }
        NBTItem nbtItem = new NBTItem(itemStack);
        if (item.getName() != null) {
            NBTCompound display = nbtItem.addCompound("display");
            String name  = item.getName();
            if (name.contains("&") || name.contains("§")){
                name = name.replaceAll("&","§");
                name = replaceLegacy(name);
            }
            display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<!i>" + name)));
        }
        if (item.getLore() != null) {
            NBTCompound display = nbtItem.addCompound("display");
            List<String> lore = display.getStringList("Lore");
            item.getLore().forEach(line -> {
                if (line.contains("&") || line.contains("§")){
                    line = line.replaceAll("&","§");
                    line = replaceLegacy(line);
                }
                lore.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<!i>" + line)));
            });
        }
        if (item.getNbt() != null) NBTUtil.setTags(item.getNbt(), nbtItem);
        return nbtItem.getItem();
    }

    public static void addRandomDamage(ItemStack itemStack){
        if (itemStack.getItemMeta() instanceof Damageable damageable){
            damageable.setDamage((int) (itemStack.getType().getMaxDurability() * Math.random()));
            itemStack.setItemMeta(damageable);
        }
    }

    public static void addOwner(ItemStack itemStack, String name){
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("M_Owner", name);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    public static void addRandomEnchants(ItemStack itemStack, LeveledEnchantment[] enchantments){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemStack.getType() == Material.ENCHANTED_BOOK){
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemMeta;
            for (LeveledEnchantment enchantment : enchantments) {
                if (enchantment.getChance() > Math.random()) meta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.getKey())),enchantment.getLevel(),true);
            }
            itemStack.setItemMeta(meta);
        }
        else {
            for (LeveledEnchantment enchantment : enchantments) {
                if (enchantment.getChance() > Math.random()) itemMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.getKey())),enchantment.getLevel(),true);
            }
            itemStack.setItemMeta(itemMeta);
        }
    }

    public static ItemStack addIdentifier(ItemStack itemStack, String type, String id){
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.addCompound("CustomFishing");
        nbtCompound.setString("type", type);
        nbtCompound.setString("id", id);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        return itemStack;
    }

    public static void givePlayerLoot(Player player, String lootKey, int amount){
        Loot loot = LootManager.WATERLOOTS.get(lootKey);
        if (loot == null) {
            loot = LootManager.LAVALOOTS.get(lootKey);
            if (loot == null) return;
        }
        if (!(loot instanceof DroppedItem droppedItem)) return;
        String key = droppedItem.getMaterial();
        ItemStack itemStack = CustomFishing.plugin.getIntegrationManager().build(key);
        if (itemStack.getType() == Material.AIR) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerRod(Player player, String rodKey, int amount){
        ItemStack itemStack = BonusManager.RODITEMS.get(rodKey);
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(itemStack);
        }
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        ItemStack itemStack = BonusManager.BAITITEMS.get(baitKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        ItemStack itemStack = BonusManager.UTILITEMS.get(utilKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static boolean saveToFile(ItemStack itemStack, String fileName){

        if (itemStack == null || itemStack.getType() == Material.AIR) return false;

        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        yamlConfiguration.set(fileName + ".material", itemStack.getType().toString());
        ItemMeta itemMeta = itemStack.getItemMeta();
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
        if (map0.size() != 0) {
            yamlConfiguration.createSection(fileName + ".nbt", map0);
        }

        File file = new File(CustomFishing.plugin.getDataFolder(), File.separator + "loots" + File.separator + fileName + ".yml");

        try {
            yamlConfiguration.save(file);
            CustomFishing.plugin.getLootManager().unload();
            CustomFishing.plugin.getLootManager().load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public static Map<String, Object> compoundToMap(NBTCompound nbtCompound){
        Map<String, Object> map = new HashMap<>();
        nbtCompound.getKeys().forEach(key -> {
            if (key.equals("Enchantments")
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
