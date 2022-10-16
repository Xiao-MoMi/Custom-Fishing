package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.object.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class FishHookEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled;
    private final Difficulty difficulty;
    private static final HandlerList handlerList = new HandlerList();

    public FishHookEvent(@NotNull Player who, Difficulty difficulty) {
        super(who);
        this.cancelled = false;
        this.difficulty = difficulty;
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

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
