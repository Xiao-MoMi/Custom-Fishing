package net.momirealms.customfishing.compatibility.item;

import ink.ptms.zaphkiel.ZapAPI;
import ink.ptms.zaphkiel.Zaphkiel;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ZaphkielItemImpl implements ItemLibrary {

    private final ZapAPI zapAPI;

    public ZaphkielItemImpl() {
        this.zapAPI = Zaphkiel.INSTANCE.api();
    }

    @Override
    public String identification() {
        return "Zaphkiel";
    }

    @Override
    public ItemStack buildItem(Player player, String id) {
        return zapAPI.getItemManager().generateItemStack(id, player);
    }

    @Override
    public String getItemID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        return zapAPI.getItemHandler().getItemId(itemStack);
    }
}
