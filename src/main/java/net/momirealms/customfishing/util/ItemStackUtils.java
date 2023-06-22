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
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.loot.DroppedItem;
import net.momirealms.customfishing.fishing.loot.Item;
import net.momirealms.customfishing.fishing.loot.Loot;
import net.momirealms.customfishing.manager.ConfigManager;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ItemStackUtils {

    /**
     * Build itemStack from item's config
     * @param item item
     * @return itemStack
     */
    public static ItemStack getFromItem(Item item) {
        ItemStack itemStack = new ItemStack(item.getMaterial());
        if (item.getMaterial() == Material.AIR) return itemStack;
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
                name = AdventureUtils.replaceLegacy(name);
            }
            display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<!i>" + name)));
        }
        if (item.getLore() != null) {
            NBTCompound display = nbtItem.addCompound("display");
            List<String> lore = display.getStringList("Lore");
            item.getLore().forEach(line -> {
                if (line.contains("&") || line.contains("§")){
                    line = AdventureUtils.replaceLegacy(line);
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
        if (item.getNbt() != null) NBTUtils.setTags(item.getNbt(), nbtItem);
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
        nbtItem.setString("TempOwner", name);
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
     *
     * @param itemStack itemStack
     * @param type      type
     * @param id        id
     */
    public static void addIdentifier(ItemStack itemStack, String type, String id){
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound nbtCompound = nbtItem.addCompound("CustomFishing");
        nbtCompound.setString("type", type);
        nbtCompound.setString("id", id);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    public static int givePlayerLoot(Player player, String key, int amount){
        Loot loot = CustomFishing.getInstance().getLootManager().getLoot(key);
        if (!(loot instanceof DroppedItem droppedItem)) return 0;
        ItemStack itemStack = CustomFishing.getInstance().getFishingManager().getCustomFishingLootItemStack(droppedItem, player);
        if (itemStack.getType() == Material.AIR) return 0;
        if (amount != 0) itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
        return itemStack.getAmount();
    }

    public static void givePlayerRod(Player player, String rodKey, int amount){
        Item item = CustomFishing.getInstance().getEffectManager().getRodItem(rodKey);
        if (item == null) return;
        ItemStack itemStack = getFromItem(item);
        if (amount != 0)
            for (int i = 0; i < amount; i++) {
                player.getInventory().addItem(itemStack);
        }else {
            player.getInventory().addItem(itemStack);
        }
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        Item item = CustomFishing.getInstance().getEffectManager().getBaitItem(baitKey);
        if (item == null) return;
        ItemStack itemStack = getFromItem(item);
        if (amount != 0) itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        Item item = CustomFishing.getInstance().getEffectManager().getUtilItem(utilKey);
        if (item == null) return;
        ItemStack itemStack = getFromItem(item);
        if (amount != 0) itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static boolean saveToFile(ItemStack itemStack, String key){
        if (itemStack == null || itemStack.getType() == Material.AIR || CustomFishing.getInstance().getLootManager().hasLoot(key)) return false;
        File file = new File(CustomFishing.getInstance().getDataFolder(), File.separator + "contents/loots" + File.separator + "imported.yml");
        YamlConfiguration data = ConfigUtils.readData(file);
        data.set(key + ".material", itemStack.getType().toString());
        data.set(key + ".amount", itemStack.getAmount());
        NBTItem nbtItem = new NBTItem(itemStack);
        Map<String, Object> map0 = NBTUtils.compoundToMap(nbtItem);
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

    public static void addExtraMeta(ItemStack itemStack, DroppedItem droppedItem, double sizeMultiplier, Player player) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (droppedItem.getBasicPrice() != 0) {
            NBTCompound fishMetaCompound = nbtItem.addCompound("FishMeta");
            fishMetaCompound.setFloat("base", droppedItem.getBasicPrice());
        }
        if (droppedItem.getSizeBonus() != 0) {
            NBTCompound fishMetaCompound = nbtItem.addCompound("FishMeta");
            fishMetaCompound.setFloat("bonus", droppedItem.getSizeBonus());
        }
        replaceAndSetSizeProperties(droppedItem.getSize(), nbtItem, sizeMultiplier);
        replacePlaceholderInDisplay(nbtItem, player);
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    private static void replaceAndSetSizeProperties(String[] sizes, NBTItem nbtItem, double sizeMultiplier) {
        if (sizes == null) return;
        float min = Float.parseFloat(sizes[0]);
        float max = Float.parseFloat(sizes[1]);
        if (max - min < 0) return;
        float size = (float) ((min + Math.random() * (max - min)) * sizeMultiplier);
        String sizeText = String.format("%.1f", size);
        NBTCompound nbtCompound = nbtItem.getCompound("display");
        if (nbtCompound == null || !nbtCompound.hasTag("Lore")) return;
        List<String> lore = nbtCompound.getStringList("Lore");
        lore.replaceAll(s -> s.replace("{size}", sizeText));
        NBTCompound fishMetaCompound = nbtItem.addCompound("FishMeta");
        fishMetaCompound.setFloat("size", size);
    }

    private static void replacePlaceholderInDisplay(NBTItem nbtItem, Player player) {
        NBTCompound nbtCompound = nbtItem.getCompound("display");
        if (nbtCompound == null) return;
        String name = nbtCompound.getString("Name");
        if (!name.equals("")) {
            nbtCompound.setString("Name", name
                    .replace("{player}", player.getName())
                    .replace("{date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern(ConfigManager.dateFormat)))
                    .replace("{worth}", String.format("%.2f", CustomFishing.getInstance().getSellManager().getCFFishPrice(nbtItem))));
        }
        List<String> lore = nbtCompound.getStringList("Lore");
        lore.replaceAll(s -> s
                .replace("{player}", player.getName())
                .replace("{date}", LocalDateTime.now().format(DateTimeFormatter.ofPattern(ConfigManager.dateFormat)))
                .replace("{worth}", String.format("%.2f", CustomFishing.getInstance().getSellManager().getCFFishPrice(nbtItem))));
    }
}