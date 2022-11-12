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

import dev.lone.itemsadder.api.CustomBlock;
import net.momirealms.customfishing.integration.BlockInterface;
import net.momirealms.customfishing.manager.TotemManager;
import net.momirealms.customfishing.util.AdventureUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderBlockImpl implements BlockInterface {

    @Override
    public void removeBlock(Block block) {
        if (CustomBlock.byAlreadyPlaced(block) != null) {
            CustomBlock.remove(block.getLocation());
        }
        else {
            block.setType(Material.AIR);
        }
    }

    @Override
    public void placeBlock(String id, Location location) {
        String blockID = TotemManager.INVERTED.get(id);
        if (blockID == null) {
            AdventureUtil.consoleMessage(id + " does not exist in totem-blocks.yml");
            return;
        }
        if (BlockInterface.isVanillaItem(blockID)) {
            BlockInterface.placeVanillaBlock(blockID, location);
        }
        else {
            CustomBlock.place(blockID, location);
        }
    }

    @Override
    @Nullable
    public String getID(Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        String id;
        if (customBlock == null) {
            id = block.getType().name();
        }
        else {
            id = customBlock.getNamespacedID();
        }
        return TotemManager.BLOCKS.get(id);
    }
}