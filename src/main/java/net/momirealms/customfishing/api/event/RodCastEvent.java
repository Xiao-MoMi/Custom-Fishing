package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.object.fishing.Bonus;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class RodCastEvent extends PlayerEvent implements Cancellable {

    private final Bonus bonus;
    private boolean isCancelled;
    private static final HandlerList handlerList = new HandlerList();

    public RodCastEvent(@NotNull Player who, @NotNull Bonus bonus) {
        super(who);
        this.isCancelled = false;
        this.bonus = bonus;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public Bonus getBonus() {
        return bonus;
    }
}
