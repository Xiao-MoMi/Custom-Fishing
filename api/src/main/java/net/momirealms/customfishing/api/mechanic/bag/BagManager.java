package net.momirealms.customfishing.api.mechanic.bag;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BagManager extends Reloadable {

    default int getBagInventoryRows(Player player) {
        int size = 1;
        for (int i = 6; i > 1; i--) {
            if (player.hasPermission("fishingbag.rows." + i)) {
                size = i;
                break;
            }
        }
        return size;
    }

    CompletableFuture<Boolean> openBag(Player viewer, UUID owner);
}
