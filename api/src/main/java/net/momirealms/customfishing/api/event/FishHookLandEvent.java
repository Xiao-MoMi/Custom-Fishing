package net.momirealms.customfishing.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class FishHookLandEvent extends PlayerEvent {

    private static final HandlerList handlerList = new HandlerList();
    private final Target target;

    public FishHookLandEvent(@NotNull Player who, Target target) {
        super(who);
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public enum Target {
        LAVA,
        WATER
    }
}
