package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.object.loot.Loot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FishFinderEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();

    private boolean cancelled;
    private final List<Loot> loots;

    public FishFinderEvent(@NotNull Player who, List<Loot> loots) {
        super(who);
        this.cancelled = false;
        this.loots = loots;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public List<Loot> getLoots() {
        return loots;
    }
}
