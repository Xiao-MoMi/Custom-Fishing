package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.manager.FishingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public record MMOItemsListener(FishingManager manager) implements Listener {

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() != PlayerFishEvent.State.FISHING) return;
        manager.onMMOItemsRodCast(event);
    }
}
