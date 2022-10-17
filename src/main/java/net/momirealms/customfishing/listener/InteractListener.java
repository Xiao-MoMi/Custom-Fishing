package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.object.Function;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    private final Function function;

    public InteractListener(Function function) {
        this.function = function;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        function.onInteract(event);
    }
}
