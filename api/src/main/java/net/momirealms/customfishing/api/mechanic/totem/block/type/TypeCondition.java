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

/**
 * Represents a condition used to check the type of a Block.
 */
public interface TypeCondition  {

    /**
     * Checks if the specified Block meets the condition.
     *
     * @param block The Block to check.
     * @return `true` if the condition is met, otherwise `false`.
     */
    boolean isMet(Block block);

    /**
     * Gets the raw text representation of this TypeCondition.
     *
     * @return The raw text representation of this TypeCondition.
     */
    String getRawText();

    /**
     * Gets a TypeCondition based on its raw text representation.
     *
     * @param raw The raw text representation of the TypeCondition.
     * @return A TypeCondition instance corresponding to the raw text.
     */
    static TypeCondition getTypeCondition(String raw) {
        if (raw.startsWith("*")) {
            return new EndWithType(raw.substring(1));
        } else if (raw.endsWith("*")) {
            return new StartWithType(raw.substring(0, raw.length() -1));
        } else {
            return new EqualType(raw);
        }
    }
}
