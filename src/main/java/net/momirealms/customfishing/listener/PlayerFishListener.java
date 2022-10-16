package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.manager.ConfigManager;
import net.momirealms.customfishing.manager.FishingManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;

public record PlayerFishListener(
        FishingManager manager) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFishMONITOR(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("MONITOR")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFishHIGHEST(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("HIGHEST")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onFishHIGH(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("HIGH")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFishNORMAL(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("NORMAL")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onFishLOW(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("LOW")) return;
        selectState(event);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFishLOWEST(PlayerFishEvent event) {
        if (!ConfigManager.priority.equals("LOWEST")) return;
        selectState(event);
    }

    public void selectState(PlayerFishEvent event) {
        if (event.isCancelled()) return;
        if (!ConfigManager.getWorldsList().contains(event.getHook().getLocation().getWorld())) return;
        switch (event.getState()) {
            case FISHING -> manager.onFishing(event);
            case REEL_IN -> manager.onReelIn(event);
            case CAUGHT_ENTITY -> manager.onCaughtEntity(event);
            case CAUGHT_FISH -> manager.onCaughtFish(event);
            case FAILED_ATTEMPT -> manager.onFailedAttempt(event);
            case BITE -> manager.onBite(event);
            case IN_GROUND -> manager.onInGround(event);
        }
    }
}
