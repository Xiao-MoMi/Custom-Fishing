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

package net.momirealms.customfishing.mechanic.totem.block;

import net.momirealms.customfishing.mechanic.totem.block.property.TotemBlockProperty;
import net.momirealms.customfishing.mechanic.totem.block.type.TypeCondition;
import org.bukkit.Axis;
import org.bukkit.block.Block;

import java.io.Serializable;
import java.util.StringJoiner;

public class TotemBlock implements Serializable {

    private final TypeCondition typeCondition;
    private final TotemBlockProperty[] properties;

    public TotemBlock(TypeCondition typeCondition, TotemBlockProperty[] properties) {
        this.typeCondition = typeCondition;
        this.properties = properties;
    }

    public TypeCondition getTypeCondition() {
        return typeCondition;
    }

    public TotemBlockProperty[] getProperties() {
        return properties;
    }

    public boolean isRightBlock(Block block) {
        if (!typeCondition.isMet(block)) {
            return false;
        }
        for (TotemBlockProperty property : properties) {
            if (!property.isPropertyMet(block)) {
                return false;
            }
        }
        return true;
    }

    public void rotate90() {
        for (TotemBlockProperty property : properties) {
            property.rotate90();
        }
    }

    public void mirror(Axis axis) {
        for (TotemBlockProperty property : properties) {
            property.mirror(axis);
        }
    }

    @Override
    public String toString() {
        StringJoiner stringJoiner = new StringJoiner(";");
        for (TotemBlockProperty property : properties) {
            stringJoiner.add(property.getRawText());
        }
        return typeCondition.getRawText() + "{" + stringJoiner + "}";
    }
}
