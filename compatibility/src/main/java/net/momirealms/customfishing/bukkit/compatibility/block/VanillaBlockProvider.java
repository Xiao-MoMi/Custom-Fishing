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

package net.momirealms.customfishing.bukkit.compatibility.block;

import net.momirealms.customfishing.api.integration.BlockProvider;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class VanillaBlockProvider implements BlockProvider {

    @Override
    public String identifier() {
        return "vanilla";
    }

    @Override
    public BlockData blockData(@NotNull Player player, @NotNull String id, List<BlockDataModifier> modifiers) {
        BlockData blockData = Material.valueOf(id.toUpperCase(Locale.ENGLISH)).createBlockData();
        for (BlockDataModifier modifier : modifiers) {
            modifier.apply(player, blockData);
        }
        return blockData;
    }

    @Override
    public @Nullable String blockID(@NotNull Block block) {
        return block.getType().name();
    }
}
