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

package net.momirealms.customfishing.api.integration;

import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for providing custom block data and retrieving block IDs within the CustomFishing plugin.
 * Extends the ExternalProvider interface.
 */
public interface BlockProvider extends ExternalProvider {

    /**
     * Generates BlockData for a given player based on a block ID and a list of modifiers.
     *
     * @param player The player for whom the block data is generated.
     * @param id The unique identifier for the block.
     * @param modifiers A list of {@link BlockDataModifier} objects to apply to the block data.
     * @return The generated {@link BlockData} for the specified block ID and modifiers.
     */
    BlockData blockData(@NotNull Player player, @NotNull String id, List<BlockDataModifier> modifiers);

    /**
     * Retrieves the unique block ID associated with a given block.
     *
     * @param block The block for which the ID is to be retrieved.
     * @return The unique block ID as a string, or null if no ID is associated with the block.
     */
    @Nullable
    String blockID(@NotNull Block block);
}
