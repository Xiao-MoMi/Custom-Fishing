package net.momirealms.customfishing.api.event;

import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomPlayerFishEvent extends PlayerFishEvent {

    @ApiStatus.Internal
    public CustomPlayerFishEvent(@NotNull Player player,
                                 @Nullable Entity entity,
                                 @NotNull FishHook hookEntity,
                                 @Nullable EquipmentSlot hand,
                                 @NotNull State state) {
        super(player, entity, hookEntity, hand, state);
    }
}
