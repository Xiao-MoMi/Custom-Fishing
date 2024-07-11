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

package net.momirealms.customfishing.api.mechanic.block;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

/**
 * Functional interface for modifying a {@link BlockState} based on a given context.
 */
@FunctionalInterface
public interface BlockStateModifier {

    /**
     * Applies modifications to the provided block state based on the given context.
     *
     * @param context the context containing the player information.
     * @param blockState the block state to modify.
     */
    void apply(Context<Player> context, BlockState blockState);
}