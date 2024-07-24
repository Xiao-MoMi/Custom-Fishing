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

package net.momirealms.customfishing.api.mechanic.block;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for managing custom block loots.
 */
public interface BlockManager extends Reloadable {

    /**
     * Get the {@link BlockDataModifierFactory} by ID
     *
     * @param id the id of the factory
     * @return the factory instance
     */
    @Nullable
    BlockDataModifierFactory getBlockDataModifierFactory(@NotNull String id);

    /**
     * Get the {@link BlockStateModifierFactory} by ID
     *
     * @param id the id of the factory
     * @return the factory instance
     */
    @Nullable
    BlockStateModifierFactory getBlockStateModifierFactory(@NotNull String id);

    /**
     * Registers a block loot.
     *
     * @param block the block configuration to register.
     * @return true if registration is successful, false otherwise.
     */
    boolean registerBlock(@NotNull BlockConfig block);

    /**
     * Summons block loot based on the context.
     *
     * @param context the context of the player.
     * @return the summoned falling block.
     */
    @NotNull
    FallingBlock summonBlockLoot(@NotNull Context<Player> context);

    /**
     * Retrieves the ID of a block.
     *
     * @param block the block to get the ID from.
     * @return the block ID.
     */
    @NotNull
    String getBlockID(@NotNull Block block);
}
