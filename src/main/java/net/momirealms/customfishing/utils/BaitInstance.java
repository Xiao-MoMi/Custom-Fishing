package net.momirealms.customfishing.utils;

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.momirealms.customfishing.ConfigReader;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaitInstance {

    private final String name;
    private List<String> lore;
    private Map<?, ?> nbt;
    private HashMap<String, Double> weightMQ;
    private HashMap<String, Integer> weightPM;
    private double time;
    private double doubleLoot;
    private int difficulty;
    private final String material;

    public BaitInstance(String name, String material) {
        this.name = name;
        this.material = material;
    }

    public void addBait2Cache(String baitKey){
        NBTItem nbtItem = new NBTItem(new ItemStack(Material.valueOf(this.material.toUpperCase())));
        NBTCompound display = nbtItem.addCompound("display");
        display.setString("Name", GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>" + this.name)));
        if(this.lore != null){
            List<String> lores = display.getStringList("Lore");
            this.lore.forEach(lore -> lores.add(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize("<italic:false>" + lore))));
        }
        if (this.nbt != null){
            NBTUtil nbtUtil = new NBTUtil(this.nbt, nbtItem.getItem());
            nbtItem = nbtUtil.getNBTItem();
        }
        nbtItem.addCompound("CustomFishing");
        NBTCompound nbtCompound = nbtItem.getCompound("CustomFishing");
        nbtCompound.setString("type", "bait");
        nbtCompound.setString("id", baitKey);
        ConfigReader.BAITITEM.put(baitKey, nbtItem.getItem());
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        ItemStack itemStack = ConfigReader.BAITITEM.get(baitKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }
    public void setNbt(Map<?, ?> nbt) {
        this.nbt = nbt;
    }
    public void setLore(List<String> lore) {
        this.lore = lore;
    }
    public void setTime(double time) {
        this.time = time;
    }
    public void setWeightMQ(HashMap<String, Double> weightMQ) {
        this.weightMQ = weightMQ;
    }
    public void setWeightPM(HashMap<String, Integer> weightPM) {
        this.weightPM = weightPM;
    }
    public int getDifficulty() { return difficulty; }

    public double getDoubleLoot() {
        return this.doubleLoot;
    }

    public Map<?, ?> getNbt() {
        return nbt;
    }
    public List<String> getLore() {
        return lore;
    }
    public double getTime() {
        return time;
    }
    public HashMap<String, Double> getWeightMQ() {
        return weightMQ;
    }
    public HashMap<String, Integer> getWeightPM() {
        return weightPM;
    }

    public void setDoubleLoot(double doubleLoot) {
        this.doubleLoot = doubleLoot;
    }
}
