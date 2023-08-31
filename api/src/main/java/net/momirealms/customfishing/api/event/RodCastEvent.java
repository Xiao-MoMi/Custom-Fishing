package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.jetbrains.annotations.NotNull;

public class RodCastEvent extends PlayerEvent implements Cancellable {

    private final Effect effect;
    private boolean isCancelled;
    private final PlayerFishEvent event;
    private static final HandlerList handlerList = new HandlerList();

    public RodCastEvent(PlayerFishEvent event, Effect effect) {
        super(event.getPlayer());
        this.effect = effect;
        this.event = event;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public Effect getEffect() {
        return effect;
    }

    public PlayerFishEvent getBukkitPlayerFishEvent() {
        return event;
    }
}
