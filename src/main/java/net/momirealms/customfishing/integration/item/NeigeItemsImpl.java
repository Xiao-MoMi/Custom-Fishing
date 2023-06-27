package net.momirealms.customfishing.integration.item;

import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import pers.neige.neigeitems.item.ItemDurability;
import pers.neige.neigeitems.item.ItemInfo;
import pers.neige.neigeitems.manager.ItemManager;
import pers.neige.neigeitems.utils.ItemUtils;

public class NeigeItemsImpl implements ItemInterface {

    @Override
    public @Nullable ItemStack build(String material, Player player) {
        if (!material.startsWith("NeigeItems:")) return null;
        material = material.substring(11);
        return ItemManager.INSTANCE.getItemStack(material, player);
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        ItemInfo itemInfo = ItemUtils.isNiItem(itemStack);
        if (itemInfo == null) return false;
        ItemDurability.INSTANCE.damage(player, itemStack, 1, true, null);
        return true;
    }

    @Override
    public @Nullable String getID(ItemStack itemStack) {
        ItemInfo itemInfo = ItemUtils.isNiItem(itemStack);
        if (itemInfo != null) {
            return itemInfo.getId();
        }
        return null;
    }
}
