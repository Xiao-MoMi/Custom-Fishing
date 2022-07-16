package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.ConfigReader;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class UtilInstance {

    private final String key;
    private final String name;
    private List<String> lore;
    private Map<?, ?> nbt;
    private final String material;
    private List<net.momirealms.customfishing.utils.Enchantment> enchantment;
    private List<ItemFlag> itemFlags;

    public UtilInstance(String key, String name, String material){
        this.key = key;
        this.name = name;
        this.material = material;
    }

    public String getKey(){
        return this.key;
    }
    public List<String> getLore(){
        return this.lore;
    }
    public String getName(){
        return this.name;
    }
    public String getMaterial(){
        return this.material;
    }
    public Map<?, ?> getNbt(){
        return this.nbt;
    }

    public void setLore(List<String> lore){
        this.lore = lore;
    }
    public void setNbt(Map<?, ?> nbt){
        this.nbt = nbt;
    }
    public void setEnchantment(List<net.momirealms.customfishing.utils.Enchantment> enchantment) {
        this.enchantment = enchantment;
    }
    public void setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
    }
    /*
    将实例转换为缓存中的NBT物品
     */
    public void addUtil2cache(String utilKey){
        ItemStack itemStack = new ItemStack(Material.valueOf(this.material.toUpperCase()));
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (enchantment != null){
            enchantment.forEach(enchantment1 -> {
                itemMeta.addEnchant(Enchantment.getByKey(enchantment1.getKey()),enchantment1.getLevel(),true);
            });
        }
        if (itemFlags != null){
            itemFlags.forEach(itemMeta::addItemFlags);
        }
        itemStack.setItemMeta(itemMeta);

        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound display = nbtItem.addCompound("display");
        display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>" + this.name)));
        if (this.lore != null){
            List<String> lores = display.getStringList("Lore");
            this.lore.forEach(lore -> lores.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>"+lore))));
        }
        if (this.nbt != null){
            NBTUtil nbtUtil = new NBTUtil(this.nbt, nbtItem.getItem());
            nbtItem = nbtUtil.getNBTItem();
        }
        if (utilKey.equals("fishfinder")){
            nbtItem.addCompound("CustomFishing");
            NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
            nbtCompound.setString("type", "util");
            nbtCompound.setString("id", "fishfinder");
        }
        ConfigReader.UTILITEM.put(utilKey, nbtItem.getItem());
    }

    /*
    给予玩家某NBT物品
     */
    public static void givePlayerUtil(Player player, String utilKey, int amount){
        ItemStack itemStack = ConfigReader.UTILITEM.get(utilKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }
}
