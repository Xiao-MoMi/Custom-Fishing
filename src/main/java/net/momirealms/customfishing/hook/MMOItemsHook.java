package net.momirealms.customfishing.hook;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;

public class MMOItemsHook {

    public static ItemStack getItemStack(String name){
        String[] split = StringUtils.split(name, ":");
        return MMOItems.plugin.getMMOItem(Type.get(split[0]), split[1]).newBuilder().getItemStack();
    }
}
