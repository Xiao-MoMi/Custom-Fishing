package net.momirealms.customfishing.item;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.utils.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class Util implements Item{

    private String name;
    private List<String> lore;
    private Map<String,Object> nbt;
    private final String material;
    private List<net.momirealms.customfishing.utils.Enchantment> enchantment;
    private List<ItemFlag> itemFlags;
    private int custommodeldata;
    private boolean unbreakable;

    public Util(String material){
        this.material = material;
    }

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        ItemStack itemStack = ConfigReader.UTILITEM.get(utilKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public void setLore(List<String> lore){this.lore = lore;}
    public void setNbt(Map<String,Object> nbt){this.nbt = nbt;}
    public void setEnchantment(List<net.momirealms.customfishing.utils.Enchantment> enchantment) {this.enchantment = enchantment;}
    public void setItemFlags(List<ItemFlag> itemFlags) {this.itemFlags = itemFlags;}
    public void setCustommodeldata(int custommodeldata){this.custommodeldata = custommodeldata;}
    public void setUnbreakable(boolean unbreakable){this.unbreakable = unbreakable;}
    public void setName(String name) {this.name = name;}

    @Override
    public boolean isUnbreakable() {return this.unbreakable;}
    @Override
    public List<String> getLore(){return this.lore;}
    @Override
    public String getMaterial(){return this.material;}
    @Override
    public String getName(){return this.name;}
    @Override
    public List<Enchantment> getEnchantments() {return this.enchantment;}
    @Override
    public List<ItemFlag> getItemFlags() {return this.itemFlags;}
    @Override
    public Map<String,Object> getNbt(){return this.nbt;}
    @Override
    public int getCustomModelData() {return this.custommodeldata;}
}
