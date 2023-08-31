package net.momirealms.customfishing.api.event;

import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class LavaFishingEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final State state;
    private boolean isCancelled;
    private final FishHook hook;

    public LavaFishingEvent(@NotNull Player who, State state, FishHook hook) {
        super(who);
        this.state = state;
        this.isCancelled = false;
        this.hook = hook;
    }

    public State getState() {
        return state;
    }

    public FishHook getHook() {
        return hook;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    public enum State {
        REEL_IN,
        CAUGHT_FISH, BITE
    }
}
