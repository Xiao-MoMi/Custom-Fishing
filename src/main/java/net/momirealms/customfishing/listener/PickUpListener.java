package net.momirealms.customfishing.listener;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PickUpListener implements Listener {

    @EventHandler
    public void onPickUp(PlayerAttemptPickupItemEvent event){
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("M_Owner")) return;
        if (!Objects.equals(nbtItem.getString("M_Owner"), event.getPlayer().getName())){
            event.setCancelled(true);
        }
        else {
            nbtItem.removeKey("M_Owner");
            itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        }
    }

    @EventHandler
    public void onMove(InventoryPickupItemEvent event){
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("M_Owner")) return;
        nbtItem.removeKey("M_Owner");
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }
}