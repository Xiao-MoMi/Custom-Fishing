package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.manager.FishingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private final FishingManager fishingManager;

    public InteractListener(FishingManager fishingManager) {
        this.fishingManager = fishingManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        fishingManager.onInteract(event);
    }
}
