package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.ConfigReader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class UtilInstance {

    private final String key;
    private final String name;
    private List<String> lore;
    private Map<?, ?> nbt;
    private final String material;

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

    /*
    将实例转换为缓存中的NBT物品
     */
    public static void addUtil2cache(String utilKey){
        //从缓存中请求物品Item
        UtilInstance util = ConfigReader.UTIL.get(utilKey);
        ItemStack itemStack = new ItemStack(Material.valueOf(util.material.toUpperCase()));
        NBTItem nbtItem = new NBTItem(itemStack);
        //设置Name和Lore
        NBTCompound display = nbtItem.addCompound("display");
        display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>"+util.name)));
        if (util.lore != null){
            List<String> lores = display.getStringList("Lore");
            util.lore.forEach(lore -> lores.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>"+lore))));
        }
        //设置NBT
        //添加物品进入缓存
        if (util.nbt != null){
            NBTUtil nbtUtil = new NBTUtil(util.nbt, nbtItem.getItem());
            ConfigReader.UTILITEM.put(utilKey, nbtUtil.getNBTItem().getItem());
        }else {
            ConfigReader.UTILITEM.put(utilKey, nbtItem.getItem());
        }
    }

    /*
    给予玩家某NBT物品
     */
    public static void givePlayerUtil(Player player, String UtilKey, int amount){
        ItemStack itemStack = ConfigReader.UTILITEM.get(UtilKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }
}
