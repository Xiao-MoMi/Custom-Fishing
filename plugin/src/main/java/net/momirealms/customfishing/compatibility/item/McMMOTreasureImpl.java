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

package net.momirealms.customfishing.compatibility.item;

import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class McMMOTreasureImpl implements ItemLibrary {

    private final Method getMcMMOPlayerMethod;
    private final Method getFishingManagerMethod;
    private final Method getFishingTreasureMethod;
    private final Method getItemStackMethod;

    public McMMOTreasureImpl() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> userClass = Class.forName("com.gmail.nossr50.util.player.UserManager");
        getMcMMOPlayerMethod = userClass.getMethod("getPlayer", Player.class);
        Class<?> mcMMOPlayerClass = Class.forName("com.gmail.nossr50.datatypes.player.McMMOPlayer");
        getFishingManagerMethod = mcMMOPlayerClass.getMethod("getFishingManager");
        Class<?> fishingManagerClass = Class.forName("com.gmail.nossr50.skills.fishing.FishingManager");
        getFishingTreasureMethod = fishingManagerClass.getDeclaredMethod("getFishingTreasure");
        getFishingTreasureMethod.setAccessible(true);
        Class<?> treasureClass = Class.forName("com.gmail.nossr50.datatypes.treasure.Treasure");
        getItemStackMethod = treasureClass.getMethod("getDrop");
    }

    @Override
    public String identification() {
        return "mcMMO";
    }

    @Override
    public ItemStack buildItem(Player player, String id) {
        if (!id.equals("treasure")) return new ItemStack(Material.AIR);
        ItemStack itemStack = null;
        int times = 0;
        while (itemStack == null && times < 5) {
            try {
                Object mcMMOPlayer = getMcMMOPlayerMethod.invoke(null, player);
                Object fishingManager = getFishingManagerMethod.invoke(mcMMOPlayer);
                Object treasure = getFishingTreasureMethod.invoke(fishingManager);
                if (treasure != null) {
                    itemStack = (ItemStack) getItemStackMethod.invoke(treasure);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            } finally {
                times++;
            }
        }
        return itemStack == null ? new ItemStack(Material.COD) : itemStack;
    }

    @Override
    public String getItemID(ItemStack itemStack) {
        return null;
    }
}
