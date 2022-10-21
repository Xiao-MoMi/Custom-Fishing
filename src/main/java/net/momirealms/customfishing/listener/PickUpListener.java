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

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class PickUpListener implements Listener {

    @EventHandler
    public void onPickUp(PlayerAttemptPickupItemEvent event){
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("M_Owner")) return;
        if (!Objects.equals(nbtItem.getString("M_Owner"), event.getPlayer().getName())){
            event.setCancelled(true);
        }
        else {
            nbtItem.removeKey("M_Owner");
            itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        }
    }

    @EventHandler
    public void onMove(InventoryPickupItemEvent event){
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("M_Owner")) return;
        nbtItem.removeKey("M_Owner");
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }
}