package net.momirealms.customfishing.storage.user;

import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import org.bukkit.entity.Player;

public class OnlineUserImpl extends OfflineUserImpl implements OnlineUser {

    private final Player player;

    public OnlineUserImpl(Player player, PlayerData playerData) {
        super(player.getUniqueId(), player.getName(), playerData);
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
