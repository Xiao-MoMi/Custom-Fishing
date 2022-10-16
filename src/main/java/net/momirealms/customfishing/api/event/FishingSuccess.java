package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.object.loot.Loot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class FishingSuccess extends Event implements Cancellable {
    private boolean cancelled;
    private Player player;
    private Loot loot;

    private static final HandlerList handlers = new HandlerList();

    private boolean loseDurability;

    public FishingSuccess(Player player, Loot loot, Boolean loseDurability) {
        this.player = player;
        this.loot = loot;
        this.loseDurability = loseDurability;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public Player getPlayer() {
        return player;
    }

    public Loot getLoot() {
        return loot;
    }

    public void setLoot(Loot loot) {
        this.loot = loot;
    }

    public boolean isLoseDurability() {
        return loseDurability;
    }

    public void setLoseDurability(boolean loseDurability) {
        this.loseDurability = loseDurability;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
