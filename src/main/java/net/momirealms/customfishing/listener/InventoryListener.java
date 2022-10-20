package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.object.Function;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryListener implements Listener {

    private final Function function;

    public InventoryListener(Function function) {
        this.function = function;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event){
        function.onOpenInventory(event);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        function.onClickInventory(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        function.onCloseInventory(event);
    }
}
