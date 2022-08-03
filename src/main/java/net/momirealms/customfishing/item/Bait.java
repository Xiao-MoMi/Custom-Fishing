package net.momirealms.customfishing.item;

import net.momirealms.customfishing.ConfigReader;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bait implements Item{

    private final String name;
    private List<String> lore;
    private Map<?, ?> nbt;
    private HashMap<String, Double> weightMQ;
    private HashMap<String, Integer> weightPM;
    private double time;
    private double doubleLoot;
    private int difficulty;
    private final String material;
    private List<net.momirealms.customfishing.utils.Enchantment> enchantment;
    private List<ItemFlag> itemFlags;

    public Bait(String name, String material) {
        this.name = name;
        this.material = material;
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        ItemStack itemStack = ConfigReader.BAITITEM.get(baitKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public void setItemFlags(List<ItemFlag> itemFlags) {this.itemFlags = itemFlags;}
    public void setDifficulty(int difficulty) {this.difficulty = difficulty;}
    public void setNbt(Map<?, ?> nbt) {this.nbt = nbt;}
    public void setLore(List<String> lore) {this.lore = lore;}
    public void setTime(double time) {this.time = time;}
    public void setWeightMQ(HashMap<String, Double> weightMQ) {this.weightMQ = weightMQ;}
    public void setWeightPM(HashMap<String, Integer> weightPM) {this.weightPM = weightPM;}
    public void setDoubleLoot(double doubleLoot) {this.doubleLoot = doubleLoot;}
    public void setEnchantment(List<net.momirealms.customfishing.utils.Enchantment> enchantment) {this.enchantment = enchantment;}

    public double getDoubleLoot() {return this.doubleLoot;}
    public int getDifficulty() {return difficulty;}
    public double getTime() {return time;}
    public HashMap<String, Double> getWeightMQ() {return weightMQ;}
    public HashMap<String, Integer> getWeightPM() {return weightPM;}

    @Override
    public List<String> getLore() {return lore;}
    @Override
    public Map<?, ?> getNbt() {return nbt;}
    @Override
    public String getMaterial() {return this.material;}
    @Override
    public List<net.momirealms.customfishing.utils.Enchantment> getEnchantments() {return this.enchantment;}
    @Override
    public List<ItemFlag> getItemFlags() {return this.itemFlags;}
    @Override
    public String getName() {return this.name;}
}
