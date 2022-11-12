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

package net.momirealms.customfishing.integration.block;

import net.momirealms.customfishing.integration.BlockInterface;
import net.momirealms.customfishing.manager.TotemManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class VanillaBlockImpl implements BlockInterface {
    @Override
    public void removeBlock(Block block) {
        block.setType(Material.AIR);
    }

    @Override
    public void placeBlock(String id, Location location) {
        String blockID = TotemManager.INVERTED.get(id);
        if (blockID == null) {
            AdventureUtil.consoleMessage(id + " does not exist in totem-blocks.yml");
            return;
        }
        BlockInterface.placeVanillaBlock(blockID, location);
    }

    @Nullable
    @Override
    public String getID(Block block) {
        return TotemManager.BLOCKS.get(block.getType().name());
    }

    @Override
    public void replaceBlock(Location location, String id) {
        placeBlock(id, location);
    }
}
