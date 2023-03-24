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

package net.momirealms.customfishing.util;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTListCompound;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.loot.DroppedItem;
import net.momirealms.customfishing.fishing.loot.Item;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.object.LeveledEnchantment;
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

    /**
     * Build itemStack from item's config
     * @param item item
     * @return itemStack
     */
    public static ItemStack getFromItem(Item item) {
        ItemStack itemStack = new ItemStack(item.getMaterial());
        itemStack.setAmount(item.getAmount());
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (item.getCustomModelData() != 0) itemMeta.setCustomModelData(item.getCustomModelData());
        if (item.isUnbreakable()) itemMeta.setUnbreakable(true);
        if (item.getItemFlags() != null) item.getItemFlags().forEach(itemMeta::addItemFlags);
        if (item.getEnchantment() != null) {
            if (itemStack.getType() == Material.ENCHANTED_BOOK){
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) itemMeta;
                item.getEnchantment().forEach(enchantment -> meta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.key())),enchantment.level(),true));
                itemStack.setItemMeta(meta);
            }
            else {
                item.getEnchantment().forEach(enchantment -> itemMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.key())),enchantment.level(),true));
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
                name = AdventureUtil.replaceLegacy(name);
            }
            display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<!i>" + name)));
        }
        if (item.getLore() != null) {
            NBTCompound display = nbtItem.addCompound("display");
            List<String> lore = display.getStringList("Lore");
            item.getLore().forEach(line -> {
                if (line.contains("&") || line.contains("§")){
                    line = AdventureUtil.replaceLegacy(line);
                }
                lore.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<!i>" + line)));
            });
        }
        if (item.getCfTag() != null) {
            NBTCompound cfCompound = nbtItem.addCompound("CustomFishing");
            cfCompound.setString("type", item.getCfTag()[0]);
            cfCompound.setString("id", item.getCfTag()[1]);
        }
        if (item.getHead64() != null) {
            NBTCompound nbtCompound = nbtItem.addCompound("SkullOwner");
            nbtCompound.setUUID("Id", item.isHeadStackable() ? UUID.nameUUIDFromBytes(item.getKey().getBytes()) : UUID.randomUUID());
            NBTListCompound texture = nbtCompound.addCompound("Properties").getCompoundList("textures").addCompound();
            texture.setString("Value", item.getHead64());
        }
        if (item.getTotem() != null) {
            nbtItem.setString("Totem", item.getTotem());
        }
        if (item.getNbt() != null) NBTUtil.setTags(item.getNbt(), nbtItem);
        return nbtItem.getItem();
    }

    /**
     * Get an itemStack with random durability
     * @param itemStack itemStack
     */
    public static void addRandomDamage(ItemStack itemStack){
        if (itemStack.getItemMeta() instanceof Damageable damageable){
            damageable.setDamage((int) (itemStack.getType().getMaxDurability() * Math.random()));
            itemStack.setItemMeta(damageable);
        }
    }

    /**
     * Adds owner tag
     * @param itemStack itemStack
     * @param name owner
     */
    public static void addOwner(ItemStack itemStack, String name){
        NBTItem nbtItem = new NBTItem(itemStack);
        nbtItem.setString("M_Owner", name);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Add random enchantments
     * @param itemStack itemStack
     * @param enchantments enchantments
     */
    public static void addRandomEnchants(ItemStack itemStack, LeveledEnchantment[] enchantments){
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemStack.getType() == Material.ENCHANTED_BOOK){
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta)itemMeta;
            for (LeveledEnchantment enchantment : enchantments) {
                if (enchantment.chance() > Math.random()) meta.addStoredEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.key())),enchantment.level(),true);
            }
            itemStack.setItemMeta(meta);
        }
        else {
            for (LeveledEnchantment enchantment : enchantments) {
                if (enchantment.chance() > Math.random()) itemMeta.addEnchant(Objects.requireNonNull(Enchantment.getByKey(enchantment.key())),enchantment.level(),true);
            }
            itemStack.setItemMeta(itemMeta);
        }
    }

    /**
     * Add customFishing tags
     * @param itemStack itemStack
     * @param type type
     * @param id id
     * @return itemStack
     */
    public static ItemStack addIdentifier(ItemStack itemStack, String type, String id){
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.addCompound("CustomFishing");
        nbtCompound.setString("type", type);
        nbtCompound.setString("id", id);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        return itemStack;
    }

    public static void givePlayerLoot(Player player, String key, int amount){
        Loot loot = CustomFishing.getInstance().getLootManager().getLoot(key);
        if (!(loot instanceof DroppedItem droppedItem)) return;
        ItemStack itemStack = CustomFishing.getInstance().getFishingManager().getCustomFishingLootItemStack(droppedItem, player);
        if (itemStack.getType() == Material.AIR) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerRod(Player player, String rodKey, int amount){
        Item item = CustomFishing.getInstance().getEffectManager().getRodItem(rodKey);
        if (item == null) return;
        ItemStack itemStack = getFromItem(item);
        for (int i = 0; i < amount; i++) {
            player.getInventory().addItem(itemStack);
        }
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        Item item = CustomFishing.getInstance().getEffectManager().getBaitItem(baitKey);
        if (item == null) return;
        ItemStack itemStack = getFromItem(item);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        Item item = CustomFishing.getInstance().getEffectManager().getUtilItem(utilKey);
        if (item == null) return;
        ItemStack itemStack = getFromItem(item);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static boolean saveToFile(ItemStack itemStack, String key){
        if (itemStack == null || itemStack.getType() == Material.AIR || CustomFishing.getInstance().getLootManager().hasLoot(key)) return false;
        File file = new File(CustomFishing.getInstance().getDataFolder(), File.separator + "loots" + File.separator + "imported.yml");
        YamlConfiguration data = ConfigUtil.readData(file);
        data.set(key + ".material", itemStack.getType().toString());
        data.set(key + ".amount", itemStack.getAmount());
        NBTItem nbtItem = new NBTItem(itemStack);
        Map<String, Object> map0 = compoundToMap(nbtItem);
        if (map0.size() != 0) {
            data.createSection(key + ".nbt", map0);
        }
        try {
            data.save(file);
            CustomFishing.getInstance().getLootManager().unload();
            CustomFishing.getInstance().getLootManager().load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void addExtraMeta(ItemStack itemStack, DroppedItem droppedItem, double sizeMultiplier) {
        NBTItem nbtItem = new NBTItem(itemStack);
        boolean changed = replaceSizeLore(droppedItem.getSize(), nbtItem, sizeMultiplier);
        if (droppedItem.getBasicPrice() != 0) {
            NBTCompound fishMetaCompound = nbtItem.addCompound("FishMeta");
            fishMetaCompound.setFloat("base", droppedItem.getBasicPrice());
            changed = true;
        }
        if (droppedItem.getSizeBonus() != 0) {
            NBTCompound fishMetaCompound = nbtItem.addCompound("FishMeta");
            fishMetaCompound.setFloat("bonus", droppedItem.getSizeBonus());
            changed = true;
        }
        if (changed) {
            itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        }
    }

    private static boolean replaceSizeLore(String[] sizes, NBTItem nbtItem, double sizeMultiplier) {
        if (sizes == null) return false;
        float min = Float.parseFloat(sizes[0]);
        float max = Float.parseFloat(sizes[1]);
        if (max - min < 0) return false;
        float size = (float) ((min + Math.random() * (max - min)) * sizeMultiplier);
        String sizeText = String.format("%.1f", size);
        NBTCompound nbtCompound = nbtItem.getCompound("display");
        if (nbtCompound == null || !nbtCompound.hasTag("Lore")) return false;
        List<String> lore = nbtCompound.getStringList("Lore");
        lore.replaceAll(s -> s.replace("{size}", sizeText));
        NBTCompound fishMetaCompound = nbtItem.addCompound("FishMeta");
        fishMetaCompound.setFloat("size", size);
        return true;
    }

    public static Map<String, Object> compoundToMap(ReadWriteNBT nbtCompound){
        Map<String, Object> map = new HashMap<>();
        for (String key : nbtCompound.getKeys()) {
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
                    switch (nbtCompound.getListType(key)) {
                        case NBTTagCompound -> nbtCompound.getCompoundList(key).forEach(a -> list.add(compoundToMap(a)));
                        case NBTTagInt -> nbtCompound.getIntegerList(key).forEach(a -> list.add("(Int) " + a));
                        case NBTTagDouble -> nbtCompound.getDoubleList(key).forEach(a -> list.add("(Double) " + a));
                        case NBTTagString -> nbtCompound.getStringList(key).forEach(a -> list.add("(String) " + a));
                        case NBTTagFloat -> nbtCompound.getFloatList(key).forEach(a -> list.add("(Float) " + a));
                        case NBTTagLong -> nbtCompound.getLongList(key).forEach(a -> list.add("(Long) " + a));
                        case NBTTagIntArray -> nbtCompound.getIntArrayList(key).forEach(a -> list.add("(IntArray) " + Arrays.toString(a)));
                    }
                    if (list.size() != 0) map.put(key, list);
                }
            }
        }
        return map;
    }
}
