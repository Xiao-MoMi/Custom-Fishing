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

package net.momirealms.customfishing.api.mechanic.item;

import com.saicone.rtag.RtagItem;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

/**
 * Functional interface representing an editor for custom fishing items.
 * Implementations of this interface apply modifications to an {@link RtagItem} using the provided context.
 */
@ApiStatus.Internal
@FunctionalInterface
public interface ItemEditor {

    /**
     * Applies modifications to the given {@link RtagItem} using the provided context.
     *
     * @param item    the {@link RtagItem} to be modified
     * @param context the {@link Context} in which the modifications are applied
     */
    void apply(RtagItem item, Context<Player> context);
}
