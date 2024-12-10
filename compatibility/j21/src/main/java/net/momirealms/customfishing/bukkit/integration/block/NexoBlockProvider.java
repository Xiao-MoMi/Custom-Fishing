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

package net.momirealms.customfishing.bukkit.integration.block;

import com.nexomc.nexo.api.NexoBlocks;
import net.momirealms.customfishing.api.integration.BlockProvider;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NexoBlockProvider implements BlockProvider {

    @Override
    public BlockData blockData(@NotNull Context<Player> context, @NotNull String id, List<BlockDataModifier> modifiers) {
        BlockData blockData = NexoBlocks.blockData(id);
        for (BlockDataModifier modifier : modifiers) {
            modifier.apply(context, blockData);
        }
        return blockData;
    }

    @Override
    public @Nullable String blockID(@NotNull Block block) {
        return NexoBlocks.isCustomBlock(block) ? NexoBlocks.customBlockMechanic(block.getBlockData()).getItemID() : null;
    }

    @Override
    public String identifier() {
        return "Nexo";
    }
}
