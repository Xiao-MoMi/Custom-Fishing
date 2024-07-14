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

package net.momirealms.customfishing.api.mechanic.totem.block.type;

import org.bukkit.block.Block;

import java.io.Serializable;

/**
 * Represents a TypeCondition that checks if a Block's type name ends with a specified string.
 */
public class EndWithType implements TypeCondition, Serializable {

    private final String end;

    public EndWithType(String end) {
        this.end = end;
    }

    /**
     * Checks if the specified Block's type name ends with the configured ending string.
     *
     * @param type The Block to check.
     * @return `true` if the Block's type name ends with the specified string, otherwise `false`.
     */
    @Override
    public boolean isMet(Block type) {
        return type.getType().name().endsWith(end);
    }

    /**
     * Gets the raw text representation of this TypeCondition.
     * The raw text includes the asterisk (*) followed by the configured ending string.
     *
     * @return The raw text representation of this TypeCondition.
     */
    @Override
    public String getRawText() {
        return "*" + end;
    }
}
