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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.block.BlockDataModifierBuilder;
import net.momirealms.customfishing.api.mechanic.block.BlockLibrary;
import net.momirealms.customfishing.api.mechanic.block.BlockStateModifierBuilder;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockManager {
    boolean registerBlockLibrary(BlockLibrary library);

    boolean unregisterBlockLibrary(BlockLibrary library);

    boolean unregisterBlockLibrary(String library);

    boolean registerBlockDataModifierBuilder(String type, BlockDataModifierBuilder builder);

    boolean registerBlockStateModifierBuilder(String type, BlockStateModifierBuilder builder);

    void summonBlock(Player player, Location hookLocation, Location playerLocation, Loot loot);

    String getAnyBlockID(Block block);
}
