package net.momirealms.customfishing.object;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public abstract class InventoryFunction extends DataFunction {

    public void onWindowTitlePacketSend(PacketContainer packet, Player receiver) {
        //empty
    }

    public void onCloseInventory(InventoryCloseEvent event) {
        //empty
    }

    public void onClickInventory(InventoryClickEvent event) {
        //empty
    }

    public void onDragInventory(InventoryDragEvent event) {

    }
}
