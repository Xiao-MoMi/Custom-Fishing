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

import org.bukkit.block.Block;

import java.io.Serializable;

/**
 * Represents a TypeCondition that checks if a Block's type starts with a specified prefix.
 */
public class StartWithType implements TypeCondition, Serializable {

    private final String start;

    public StartWithType(String start) {
        this.start = start;
    }

    /**
     * Checks if the specified Block's type starts with the configured prefix.
     *
     * @param type The Block to check.
     * @return `true` if the Block's type starts with the specified prefix, otherwise `false`.
     */
    @Override
    public boolean isMet(Block type) {
        return type.getType().name().startsWith(start);
    }

    /**
     * Gets the raw text representation of this TypeCondition, which is the configured prefix followed by '*'.
     *
     * @return The raw text representation of this TypeCondition.
     */
    @Override
    public String getRawText() {
        return start + "*";
    }
}
