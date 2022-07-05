package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.requirements.Requirement;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LootInstance {

    private final String key;
    private final String name;
    private String nick;
    private List<String> lore;
    private Map<?, ?> nbt;
    private String material;
    private String msg;
    private String mm;
    private String layout;
    private VectorUtil vectorUtil;
    private final Difficulty difficulty;
    private final int weight;
    private List<Requirement> requirements;
    private final int time;
    private int mmLevel;
    private List<String> commands;

    public LootInstance(String key, String name, Difficulty difficulty, int weight, int time){
        this.key = key;
        this.name = name;
        this.difficulty = difficulty;
        this.weight = weight;
        this.time = time;
    }

    public String getKey(){
        return this.key;
    }
    public String getNick(){ return this.nick; }
    public String getMsg(){ return this.msg; }
    public String getLayout(){ return this.layout; }
    public String getMm(){ return this.mm; }
    public List<String> getLore(){
        return this.lore;
    }
    public List<String> getCommands(){return this.commands;}
    public Difficulty getDifficulty(){
        return this.difficulty;
    }
    public int getWeight(){
        return this.weight;
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
    public List<Requirement> getRequirements() { return this.requirements; }
    public int getTime(){ return this.time; }
    public int getMmLevel(){ return this.mmLevel; }
    public VectorUtil getVectorUtil(){ return this.vectorUtil; }

    public void setLore(List<String> lore){
        this.lore = lore;
    }
    public void setNbt(Map<?, ?> nbt){
        this.nbt = nbt;
    }
    public void setRequirements(List<Requirement> requirements) { this.requirements = requirements; }
    public void setMaterial(String material){ this.material = material; }
    public void setNick(String nick){ this.nick = nick; }
    public void setMsg(String msg){ this.msg = msg; }
    public void setMm(String mm){ this.mm = mm; }
    public void setLayout(String layout){ this.layout = layout; }
    public void setVectorUtil(VectorUtil vectorUtil){ this.vectorUtil = vectorUtil; }
    public void setCommands(List<String> commands){ this.commands = commands; }
    public void setMmLevel(int mmLevel){ this.mmLevel = mmLevel; }

    /*
    将实例转换为缓存中的NBT物品
     */
    public static void addLoot2cache(String lootKey){
        //从缓存中请求物品Item
        LootInstance loot = ConfigReader.LOOT.get(lootKey);
        ItemStack itemStack = new ItemStack(Material.valueOf(loot.material.toUpperCase()));
        NBTItem nbtItem = new NBTItem(itemStack);
        //设置Name和Lore
        NBTCompound display = nbtItem.addCompound("display");
        display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>"+loot.name)));
        if(loot.lore != null){
            List<String> lores = display.getStringList("Lore");
            loot.lore.forEach(lore -> lores.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>"+lore))));
        }
        //设置NBT
        //添加物品进入缓存
        if (loot.nbt != null){
            NBTUtil nbtUtil = new NBTUtil(loot.nbt, nbtItem.getItem());
            ConfigReader.LOOTITEM.put(lootKey, nbtUtil.getNBTItem().getItem());
        }else {
            ConfigReader.LOOTITEM.put(lootKey, nbtItem.getItem());
        }
    }

    /*
    给予玩家某NBT物品
     */
    public static void givePlayerLoot(Player player, String lootKey, int amount){
        ItemStack itemStack = ConfigReader.LOOTITEM.get(lootKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }
}
