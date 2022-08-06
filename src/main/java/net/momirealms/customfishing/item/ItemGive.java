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

package net.momirealms.customfishing.item;

import net.momirealms.customfishing.ConfigReader;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemGive {

    public static void givePlayerLoot(Player player, String lootKey, int amount){
        ItemStack itemStack = ConfigReader.LOOTITEM.get(lootKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerRod(Player player, String rodKey, int amount){
        ItemStack itemStack = ConfigReader.RODITEM.get(rodKey);
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerBait(Player player, String baitKey, int amount){
        ItemStack itemStack = ConfigReader.BAITITEM.get(baitKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }

    public static void givePlayerUtil(Player player, String utilKey, int amount){
        ItemStack itemStack = ConfigReader.UTILITEM.get(utilKey);
        if (itemStack == null) return;
        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
    }
}
