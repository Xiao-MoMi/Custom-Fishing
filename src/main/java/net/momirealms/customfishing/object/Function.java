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

package net.momirealms.customfishing.object;

import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;

public class Function {

    public void load() {
        //empty
    }

    public void unload() {
        //empty
    }

    public void onQuit(Player player) {
        //empty
    }

    public void onJoin(Player player) {
        //empty
    }

    public void onInteract(PlayerInteractEvent event) {
        //empty
    }

    public void onWindowTitlePacketSend(PacketContainer packet, Player receiver) {

    }

    public void onCloseInventory(InventoryCloseEvent event) {
    }

    public void onClickInventory(InventoryClickEvent event) {
    }

    public void onOpenInventory(InventoryOpenEvent event) {
    }
}
