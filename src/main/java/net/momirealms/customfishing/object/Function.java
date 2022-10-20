package net.momirealms.customfishing.object;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;

public class Function {

    public void load() {
        //empty
    }

    public void unload() {
        //empty
    }

    public void onQuit(Player player) {
        //empty
    }

    public void onJoin(Player player) {
        //empty
    }

    public void onInteract(PlayerInteractEvent event) {
        //empty
    }

    public void onWindowTitlePacketSend(PacketContainer packet, Player receiver) {

    }

    public void onCloseInventory(InventoryCloseEvent event) {
    }

    public void onClickInventory(InventoryClickEvent event) {
    }

    public void onOpenInventory(InventoryOpenEvent event) {
    }

    public void onDragInventory(InventoryDragEvent event) {
    }

    public void onMoveItemInventory(InventoryMoveItemEvent event) {
    }
}
