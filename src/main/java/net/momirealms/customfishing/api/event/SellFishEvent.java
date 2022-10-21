package net.momirealms.customfishing.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class SellFishEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled;
    private float money;
    private static final HandlerList handlerList = new HandlerList();

    public SellFishEvent(@NotNull Player who, float money) {
        super(who);
        this.money = money;
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

    public float getMoney() {
        return money;
    }

    public void setMoney(float money) {
        this.money = money;
    }
}
