/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.item;

import net.momirealms.customfishing.utils.Enchantment;
import org.bukkit.inventory.ItemFlag;

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
