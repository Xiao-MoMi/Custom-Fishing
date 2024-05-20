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

package net.momirealms.customfishing.bukkit.integration.block;

import dev.lone.itemsadder.api.CustomBlock;
import net.momirealms.customfishing.api.integration.BlockProvider;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemsAdderBlockProvider implements BlockProvider {

    @Override
    public String identifier() {
        return "ItemsAdder";
    }

    @Override
    public BlockData blockData(@NotNull Context<Player> context, @NotNull String id, List<BlockDataModifier> modifiers) {
        BlockData blockData = CustomBlock.getBaseBlockData(id);
        for (BlockDataModifier modifier : modifiers) {
            modifier.apply(context, blockData);
        }
        return blockData;
    }

    @Override
    public String blockID(@NotNull Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock == null ? null : customBlock.getId();
    }
}
