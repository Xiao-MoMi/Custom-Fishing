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

import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

public class MMOItemsConverter implements Listener {

    private final HashMap<Player, Long> coolDown = new HashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event){

        if (event.getState() == PlayerFishEvent.State.FISHING){

            Player player = event.getPlayer();

            long time = System.currentTimeMillis();
            if (time - (coolDown.getOrDefault(player, time - 5000)) < 5000) {
                return;
            }
            coolDown.put(player, time);

            PlayerInventory inventory = player.getInventory();

            ItemStack mainHand = inventory.getItemInMainHand();
            if(mainHand.getType() == Material.FISHING_ROD){
                NBTItem nbtItem = new NBTItem(mainHand);
                if (nbtItem.getCompound("CustomFishing") == null) {
                    if (nbtItem.getString("MMOITEMS_ITEM_ID") != null){
                        NBTCompound nbtCompound = nbtItem.addCompound("CustomFishing");
                        nbtCompound.setString("type","rod");
                        nbtCompound.setString("id",nbtItem.getString("MMOITEMS_ITEM_ID"));
                        mainHand.setItemMeta(nbtItem.getItem().getItemMeta());
                    }
                }
            }

            ItemStack offHand = inventory.getItemInOffHand();
            if(offHand.getType() == Material.FISHING_ROD){
                NBTItem nbtItem = new NBTItem(offHand);
                if (nbtItem.getCompound("CustomFishing") == null) {
                    if (nbtItem.getString("MMOITEMS_ITEM_ID") != null){
                        NBTCompound nbtCompound = nbtItem.addCompound("CustomFishing");
                        nbtCompound.setString("type", "rod");
                        nbtCompound.setString("id", nbtItem.getString("MMOITEMS_ITEM_ID"));
                        offHand.setItemMeta(nbtItem.getItem().getItemMeta());
                    }
                }
            }
        }
    }
}
