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

package net.momirealms.customfishing.integration;

import net.momirealms.customfishing.manager.TotemManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface BlockInterface {

    void removeBlock(Block block);
    void placeBlock(String id, Location location);
    @Nullable
    String getID(Block block);

    default void replaceBlock(Location location, String id) {
        removeBlock(location.getBlock());
        placeBlock(id, location);
    }

    static boolean isVanillaItem(String item) {
        char[] chars = item.toCharArray();
        for (char character : chars) {
            if ((character < 65 || character > 90) && character != 95) {
                return false;
            }
        }
        return true;
    }

    static void placeVanillaBlock(String id, Location location) {
        location.getBlock().setType(Material.valueOf(id));
    }
}
