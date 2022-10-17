package net.momirealms.customfishing.api.event;

import net.momirealms.customfishing.object.fishing.FishResult;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FishResultEvent extends PlayerEvent implements Cancellable {

    private boolean cancelled;
    private boolean isDouble;
    private final FishResult result;
    private final ItemStack loot;
    private static final HandlerList handlerList = new HandlerList();

    public FishResultEvent(@NotNull Player who, FishResult result, boolean isDouble, ItemStack loot) {
        super(who);
        this.cancelled = false;
        this.result = result;
        this.isDouble = isDouble;
        this.loot = loot;
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

    public boolean isDouble() {
        return isDouble;
    }

    @NotNull
    public FishResult getResult() {
        return result;
    }

    @Nullable
    public ItemStack getLoot() {
        return loot;
    }

    public void setDouble(boolean willDouble) {
        isDouble = willDouble;
    }
}
