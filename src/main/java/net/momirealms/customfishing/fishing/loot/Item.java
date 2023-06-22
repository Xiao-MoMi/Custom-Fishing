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

package net.momirealms.customfishing.fishing.loot;

import net.momirealms.customfishing.object.LeveledEnchantment;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class Item {

    private final String key;
    private final int amount;
    private final Material material;
    private String name;
    private List<String> lore;
    private List<ItemFlag> itemFlags;
    private int customModelData;
    private boolean unbreakable;
    private String head64;
    private List<LeveledEnchantment> enchantment;
    private Map<String, Object> nbt;
    private String totem;
    private boolean headStackable;
    private String[] cfTag;

    public Item(Material material, String key) {
        this.material = material;
        this.key = key;
        this.amount = 1;
    }

    public Item(ConfigurationSection section, String key) {
        this.key = key;
        this.material = Material.valueOf(section.getString("material", "cod").toUpperCase(Locale.ENGLISH));
        this.amount = section.getInt("amount", 1);
        this.setUnbreakable(section.getBoolean("unbreakable", false));
        if (section.contains("display.lore")) this.setLore(section.getStringList("display.lore"));
        if (section.contains("display.name")) this.setName(section.getString("display.name"));
        if (section.contains("custom-model-data")) this.setCustomModelData(section.getInt("custom-model-data"));
        if (section.contains("enchantments")) {
            List<LeveledEnchantment> enchantmentList = new ArrayList<>();
            Objects.requireNonNull(section.getConfigurationSection("enchantments")).getKeys(false).forEach(enchant -> {
                LeveledEnchantment leveledEnchantment = new LeveledEnchantment(
                        NamespacedKey.fromString(enchant),
                        section.getInt("enchantments." + enchant),
                        1
                );
                enchantmentList.add(leveledEnchantment);
            });
            this.setEnchantment(enchantmentList);
        }
        if (section.contains("item_flags")) {
            ArrayList<ItemFlag> itemFlags = new ArrayList<>();
            section.getStringList("item_flags").forEach(flag -> itemFlags.add(ItemFlag.valueOf(flag)));
            this.setItemFlags(itemFlags);
        }
        if (section.contains("nbt")) {
            ConfigurationSection nbtSection = section.getConfigurationSection(".nbt");
            if (nbtSection == null) return;
            this.setNbt(nbtSection.getValues(false));
        }
        if (section.contains("head64")) {
            this.setHead64(section.getString("head64"));
            this.setHeadStackable(section.getBoolean("head-stackable", false));
        }
        if (section.contains("totem")) {
            this.setTotem(section.getString("totem"));
        }
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public List<ItemFlag> getItemFlags() {
        return itemFlags;
    }

    public void setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public boolean isUnbreakable() {
        return unbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

    public List<LeveledEnchantment> getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(List<LeveledEnchantment> enchantment) {
        this.enchantment = enchantment;
    }

    public Map<String, Object> getNbt() {
        return nbt;
    }

    public void setNbt(Map<String, Object> nbt) {
        this.nbt = nbt;
    }

    public String getHead64() {
        return head64;
    }

    public void setHead64(String head64) {
        this.head64 = head64;
    }

    public String getKey() {
        return key;
    }

    public int getAmount() {
        return amount;
    }

    public String getTotem() {
        return totem;
    }

    public void setTotem(String totem) {
        this.totem = totem;
    }

    public boolean isHeadStackable() {
        return headStackable;
    }

    public void setHeadStackable(boolean headStackable) {
        this.headStackable = headStackable;
    }

    public String[] getCfTag() {
        return cfTag;
    }

    public void setCfTag(String[] cfTag) {
        this.cfTag = cfTag;
    }

    public Item cloneWithPrice(double price){
        Item newItem = new Item(this.material, this.key);
        if (this.lore != null){
            List<String> lore = new ArrayList<>();
            for (String text : this.lore) {
                lore.add(text.replace("{money}", String.format("%.2f", price)));
            }
            newItem.setLore(lore);
        }
        if (this.name != null){
            newItem.setName(this.name.replace("{money}", String.format("%.2f", price)));
        }
        newItem.setCustomModelData(this.customModelData);
        return newItem;
    }
}