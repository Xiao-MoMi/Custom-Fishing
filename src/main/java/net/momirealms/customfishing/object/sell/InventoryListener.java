package net.momirealms.customfishing.object.sell;

import net.momirealms.customfishing.manager.SellManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;

public class InventoryListener implements Listener {

    private final SellManager sellManager;

    public InventoryListener(SellManager sellManager) {
        this.sellManager = sellManager;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event){
        sellManager.onOpen(event);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        sellManager.onClick(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        sellManager.onClose(event);
    }
}
