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

package net.momirealms.customfishing.mechanic.totem.block.type;

import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

public class OrType implements TypeCondition, Serializable {

    private final TypeCondition[] typeConditions;

    public OrType(TypeCondition[] typeConditions) {
        this.typeConditions = typeConditions;
    }

    @Override
    public boolean isMet(Block block) {
        for (TypeCondition typeCondition : typeConditions) {
            if (typeCondition.isMet(block)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getRawTexts() {
        HashSet<String> strings = new HashSet<>();
        for (TypeCondition condition : typeConditions) {
            strings.addAll(List.of(condition.getRawTexts()));
        }
        return strings.toArray(new String[0]);
    }
}
