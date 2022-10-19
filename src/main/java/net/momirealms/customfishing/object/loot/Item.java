package net.momirealms.customfishing.object.loot;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Item {

    private final Material material;
    private String name;
    private List<String> lore;
    private List<ItemFlag> itemFlags;
    private int customModelData;
    private boolean unbreakable;
    private List<LeveledEnchantment> enchantment;
    private Map<String, Object> nbt;

    public Item(Material material) {
        this.material = material;
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

    public Item cloneWithPrice(double price){
        Item newItem = new Item(this.material);
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