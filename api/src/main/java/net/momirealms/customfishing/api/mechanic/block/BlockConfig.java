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

package net.momirealms.customfishing.api.mechanic.block;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

import java.util.List;

public interface BlockConfig {

    String blockID();

    List<BlockDataModifier> dataModifier();

    List<BlockStateModifier> stateModifiers();

    MathValue<Player> horizontalVector();

    MathValue<Player> verticalVector();

    static Builder builder() {
        return new BlockConfigImpl.BuilderImpl();
    }

    interface Builder {

        Builder blockID(String blockID);

        Builder dataModifierList(List<BlockDataModifier> dataModifierList);

        Builder stateModifierList(List<BlockStateModifier> stateModifierList);

        Builder horizontalVector(MathValue<Player> horizontalVector);

        Builder verticalVector(MathValue<Player> verticalVector);

        BlockConfig build();
    }
}
