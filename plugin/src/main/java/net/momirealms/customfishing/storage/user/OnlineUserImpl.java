/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.storage.user;

import net.momirealms.customfishing.api.data.PlayerData;
import net.momirealms.customfishing.api.data.user.OnlineUser;
import org.bukkit.entity.Player;

/**
 * Implementation of the OnlineUser interface, extending OfflineUserImpl to represent online player data.
 */
public class OnlineUserImpl extends OfflineUserImpl implements OnlineUser {

    private final Player player;

    /**
     * Constructor to create an OnlineUserImpl instance.
     *
     * @param player     The online player associated with this user.
     * @param playerData The player's data, including bag contents, earnings, and statistics.
     */
    public OnlineUserImpl(Player player, PlayerData playerData) {
        super(player.getUniqueId(), player.getName(), playerData);
        this.player = player;
    }

    @Override
    public Player getPlayer() {
        return player;
    }
}
