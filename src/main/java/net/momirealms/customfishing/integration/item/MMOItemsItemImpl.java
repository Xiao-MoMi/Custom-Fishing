package net.momirealms.customfishing.integration.item;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.momirealms.customfishing.integration.ItemInterface;
import org.apache.commons.lang.StringUtils;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MMOItemsItemImpl implements ItemInterface {

    @Nullable
    @Override
    public ItemStack build(String material) {
        if (!material.startsWith("MMOItems:")) return null;
        material = material.substring(9);
        String[] split = StringUtils.split(material, ":");
        MMOItem mmoItem = MMOItems.plugin.getMMOItem(Type.get(split[0]), split[1]);
        return mmoItem == null ? null : mmoItem.newBuilder().getItemStack();
    }
}
