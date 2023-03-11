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

package net.momirealms.customfishing.fishing.action;

import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record MessageActionImpl(String[] messages, String nick) implements Action {

    public MessageActionImpl(String[] messages, String nick) {
        this.messages = messages;
        this.nick = nick == null ? "" : nick;
    }

    @Override
    public void doOn(Player player, @Nullable Player anotherPlayer) {
        for (String message : messages) {
            AdventureUtil.playerMessage(player,
                    message.replace("{player}", player.getName())
                            .replace("{world}", player.getWorld().getName())
                            .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                            .replace("{y}", String.valueOf(player.getLocation().getBlockY()))
                            .replace("{z}", String.valueOf(player.getLocation().getBlockZ()))
                            .replace("{loot}", nick)
                            .replace("{activator}", anotherPlayer == null ? "" : anotherPlayer.getName())
            );
        }
    }
}