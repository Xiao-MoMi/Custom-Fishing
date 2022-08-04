package net.momirealms.customfishing.item;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.requirements.Requirement;
import net.momirealms.customfishing.bar.Difficulty;
import net.momirealms.customfishing.utils.VectorUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Loot implements Item {

    private final String key;
    private String name;
    private String nick;
    private List<String> lore;
    private Map<String,Object> nbt;
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
    private int exp;
    private List<String> commands;
    private String group;
    private List<net.momirealms.customfishing.utils.Enchantment> enchantment;
    private List<ItemFlag> itemFlags;
    private boolean showInFinder;
    private int custommodeldata;
    private boolean unbreakable;

    public Loot(String key, Difficulty difficulty, int weight, int time){
        this.key = key;
        this.difficulty = difficulty;
        this.weight = weight;
        this.time = time;
    }

    public String getKey(){return this.key;}
    public String getNick(){return this.nick;}
    public String getMsg(){return this.msg;}
    public String getLayout(){return this.layout;}
    public String getMm(){return this.mm;}
    public boolean isShowInFinder() {return this.showInFinder;}
    public List<String> getCommands(){return this.commands;}
    public Difficulty getDifficulty(){return this.difficulty;}
    public int getWeight(){return this.weight;}
    public List<Requirement> getRequirements() { return this.requirements; }
    public int getTime(){ return this.time; }
    public int getMmLevel(){ return this.mmLevel; }
    public VectorUtil getVectorUtil(){ return this.vectorUtil; }
    public String getGroup() {return group;}
    public int getExp() {return exp;}

    @Override
    public List<String> getLore(){return this.lore;}
    @Override
    public String getName(){return this.name;}
    @Override
    public String getMaterial(){return this.material;}
    @Override
    public List<net.momirealms.customfishing.utils.Enchantment> getEnchantments() {return this.enchantment;}
    @Override
    public List<ItemFlag> getItemFlags() {return this.itemFlags;}
    @Override
    public Map<String,Object> getNbt(){return this.nbt;}
    @Override
    public int getCustomModelData() {return this.custommodeldata;}
    @Override
    public boolean isUnbreakable() {return this.unbreakable;}

    public void setName(String name) {this.name = name;}
    public void setShowInFinder(boolean showInFinder) {this.showInFinder = showInFinder;}
    public void setLore(List<String> lore){this.lore = lore;}
    public void setNbt(Map<String,Object> nbt){this.nbt = nbt;}
    public void setRequirements(List<Requirement> requirements) {this.requirements = requirements;}
    public void setMaterial(String material){this.material = material;}
    public void setNick(String nick){this.nick = nick;}
    public void setMsg(String msg){this.msg = msg;}
    public void setMm(String mm){this.mm = mm;}
    public void setLayout(String layout){this.layout = layout;}
    public void setVectorUtil(VectorUtil vectorUtil){this.vectorUtil = vectorUtil;}
    public void setCommands(List<String> commands){this.commands = commands;}
    public void setMmLevel(int mmLevel){this.mmLevel = mmLevel;}
    public void setGroup(String group) {this.group = group;}
    public void setExp(int exp) {this.exp = exp;}
    public void setItemFlags(List<ItemFlag> itemFlags) {this.itemFlags = itemFlags;}
    public void setEnchantment(List<net.momirealms.customfishing.utils.Enchantment> enchantment) {this.enchantment = enchantment;}
    public void setCustommodeldata(int custommodeldata){this.custommodeldata = custommodeldata;}
    public void setUnbreakable(boolean unbreakable){this.unbreakable = unbreakable;}

    public static void givePlayerLoot(Player player, String lootKey, int amount){
        ItemStack itemStack = ConfigReader.LOOTITEM.get(lootKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }
}
