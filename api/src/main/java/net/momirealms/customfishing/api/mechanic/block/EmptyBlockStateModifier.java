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
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

/**
 * A no-operation implementation of the {@link BlockStateModifier} interface.
 * This modifier does nothing when applied.
 */
public class EmptyBlockStateModifier implements BlockStateModifier {

    public static final EmptyBlockStateModifier INSTANCE = new EmptyBlockStateModifier();

    @Override
    public void apply(Context<Player> context, BlockState blockState) {
    }
}
