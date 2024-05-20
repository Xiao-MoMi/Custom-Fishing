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

package net.momirealms.customfishing.api.mechanic.item;

import net.kyori.adventure.key.Key;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ItemManager extends Reloadable {

    boolean registerItem(@NotNull Key key, @NotNull CustomFishingItem item);

    @Nullable
    ItemStack buildInternal(@NotNull Context<Player> context, @NotNull Key key);

    @Nullable
    ItemStack buildAny(@NotNull Context<Player> context, @NotNull String item);

    @NotNull
    String getItemID(@NotNull ItemStack itemStack);

    @Nullable
    String getCustomFishingItemID(@NotNull ItemStack itemStack);

    @Nullable
    Item dropItemLoot(@NotNull Context<Player> context);
}
