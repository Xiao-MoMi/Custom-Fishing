package net.momirealms.customfishing.item;

import net.momirealms.customfishing.ConfigReader;
import net.momirealms.customfishing.utils.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class Util implements Item{

    private final String key;
    private final String name;
    private List<String> lore;
    private Map<?, ?> nbt;
    private final String material;
    private List<net.momirealms.customfishing.utils.Enchantment> enchantment;
    private List<ItemFlag> itemFlags;

    public Util(String key, String name, String material){
        this.key = key;
        this.name = name;
        this.material = material;
    }

    public String getKey(){return this.key;}
    public List<String> getLore(){return this.lore;}
    public String getMaterial(){return this.material;}

    @Override
    public String getName(){return this.name;}
    @Override
    public List<Enchantment> getEnchantments() {return this.enchantment;}
    @Override
    public List<ItemFlag> getItemFlags() {return this.itemFlags;}
    @Override
    public Map<?, ?> getNbt(){return this.nbt;}

    public void setLore(List<String> lore){this.lore = lore;}
    public void setNbt(Map<?, ?> nbt){this.nbt = nbt;}
    public void setEnchantment(List<net.momirealms.customfishing.utils.Enchantment> enchantment) {this.enchantment = enchantment;}
    public void setItemFlags(List<ItemFlag> itemFlags) {this.itemFlags = itemFlags;}

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        ItemStack itemStack = ConfigReader.UTILITEM.get(utilKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }
}
