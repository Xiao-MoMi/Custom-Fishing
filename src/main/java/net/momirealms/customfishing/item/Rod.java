package net.momirealms.customfishing.item;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.utils.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rod implements Item{

    private String name;
    private List<String> lore;
    private Map<String,Object> nbt;
    private HashMap<String, Double> weightMQ;
    private HashMap<String, Integer> weightPM;
    private double time;
    private int difficulty;
    private double doubleLoot;
    private List<net.momirealms.customfishing.utils.Enchantment> enchantment;
    private List<ItemFlag> itemFlags;
    private int custommodeldata;
    private boolean unbreakable;

    public static void givePlayerRod(Player player, String rodKey, int amount){
        ItemStack itemStack = ConfigReader.RODITEM.get(rodKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public void setDifficulty(int difficulty) {this.difficulty = difficulty;}
    public void setDoubleLoot(double doubleLoot) {this.doubleLoot = doubleLoot;}
    public void setNbt(Map<String,Object> nbt) {this.nbt = nbt;}
    public void setLore(List<String> lore) {this.lore = lore;}
    public void setEnchantment(List<net.momirealms.customfishing.utils.Enchantment> enchantment) {this.enchantment = enchantment;}
    public void setItemFlags(List<ItemFlag> itemFlags) {this.itemFlags = itemFlags;}
    public void setTime(double time) {this.time = time;}
    public void setWeightMQ(HashMap<String, Double> weightMQ) {this.weightMQ = weightMQ;}
    public void setWeightPM(HashMap<String, Integer> weightPM) {this.weightPM = weightPM;}
    public void setCustommodeldata(int custommodeldata){this.custommodeldata = custommodeldata;}
    public void setUnbreakable(boolean unbreakable){this.unbreakable = unbreakable;}
    public void setName(String name) {this.name = name;}

    public double getTime() {return time;}
    public HashMap<String, Double> getWeightMQ() {return weightMQ;}
    public HashMap<String, Integer> getWeightPM() {return weightPM;}
    public int getDifficulty() {return difficulty;}
    public double getDoubleLoot() {return this.doubleLoot;}

    @Override
    public boolean isUnbreakable() {return this.unbreakable;}
    @Override
    public Map<String,Object> getNbt() {return nbt;}
    @Override
    public int getCustomModelData() {return this.custommodeldata;}
    @Override
    public String getMaterial() {return "fishing_rod";}
    @Override
    public List<Enchantment> getEnchantments() {return this.enchantment;}
    @Override
    public List<ItemFlag> getItemFlags() {return this.itemFlags;}
    @Override
    public String getName() {return this.name;}
    @Override
    public List<String> getLore() {return this.lore;}
}
