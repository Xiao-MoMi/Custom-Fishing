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

package net.momirealms.customfishing.api.mechanic.totem.block.type;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import org.bukkit.block.Block;

import java.io.Serializable;

/**
 * Represents a TypeCondition that checks if a Block's type matches a specified type string.
 */
public class EqualType implements TypeCondition, Serializable {

    private final String type;

    public EqualType(String type) {
        this.type = type;
    }

    /**
     * Checks if the specified Block's type matches the configured type string.
     *
     * @param type The Block to check.
     * @return `true` if the Block's type matches the specified type string, otherwise `false`.
     */
    @Override
    public boolean isMet(Block type) {
        return this.type.equals(BukkitCustomFishingPlugin.getInstance().getBlockManager().getBlockID(type));
    }

    /**
     * Gets the raw text representation of this TypeCondition, which is the configured type string.
     *
     * @return The raw text representation of this TypeCondition.
     */
    @Override
    public String getRawText() {
        return type;
    }
}
