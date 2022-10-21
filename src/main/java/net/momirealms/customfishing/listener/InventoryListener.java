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

package net.momirealms.customfishing.listener;

import net.momirealms.customfishing.object.Function;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryListener implements Listener {

    private final Function function;

    public InventoryListener(Function function) {
        this.function = function;
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event){
        function.onOpenInventory(event);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        function.onClickInventory(event);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event){
        function.onCloseInventory(event);
    }
}
