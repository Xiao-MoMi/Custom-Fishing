/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.integration.item;

import net.momirealms.customfishing.api.integration.ItemProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class McMMOTreasureProvider implements ItemProvider {

    private final Method getMcMMOPlayerMethod;
    private final Method getFishingManagerMethod;
    private final Method getFishingTreasureMethod;
    private final Method getItemStackMethod;

    public McMMOTreasureProvider() throws ClassNotFoundException, NoSuchMethodException {
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
    public String identifier() {
        return "mcMMO";
    }

    @NotNull
    @Override
    public ItemStack buildItem(@NotNull Player player, @NotNull String id) {
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
        return itemStack == null ? (Math.random() > 0.5 ? new ItemStack(Material.COD) : (Math.random() > 0.2) ? new ItemStack(Material.SALMON) : new ItemStack(Material.PUFFERFISH)) : itemStack;
    }

    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        return null;
    }
}
