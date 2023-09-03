package net.momirealms.customfishing.api.data.user;

import org.bukkit.entity.Player;

public interface OnlineUser extends OfflineUser {
    Player getPlayer();
}
