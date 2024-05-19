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

import java.util.ArrayList;
import java.util.List;

public class BlockConfigImpl implements BlockConfig {

    private final String blockID;
    private final List<BlockDataModifier> dataModifierList;
    private final List<BlockStateModifier> stateModifierList;
    private final MathValue<Player> horizontalVector;
    private final MathValue<Player> verticalVector;

    public BlockConfigImpl(String blockID, List<BlockDataModifier> dataModifierList, List<BlockStateModifier> stateModifierList, MathValue<Player> horizontalVector, MathValue<Player> verticalVector) {
        this.blockID = blockID;
        this.dataModifierList = dataModifierList;
        this.stateModifierList = stateModifierList;
        this.horizontalVector = horizontalVector;
        this.verticalVector = verticalVector;
    }

    @Override
    public String blockID() {
        return blockID;
    }

    @Override
    public List<BlockDataModifier> dataModifier() {
        return dataModifierList;
    }

    @Override
    public List<BlockStateModifier> stateModifiers() {
        return stateModifierList;
    }

    @Override
    public MathValue<Player> horizontalVector() {
        return horizontalVector;
    }

    @Override
    public MathValue<Player> verticalVector() {
        return verticalVector;
    }

    public static class BuilderImpl implements Builder {
        private String blockID;
        private final List<BlockDataModifier> dataModifierList = new ArrayList<>();
        private final List<BlockStateModifier> stateModifierList = new ArrayList<>();
        private MathValue<Player> horizontalVector;
        private MathValue<Player> verticalVector;
        @Override
        public Builder blockID(String blockID) {
            this.blockID = blockID;
            return this;
        }
        @Override
        public Builder dataModifierList(List<BlockDataModifier> dataModifierList) {
            this.dataModifierList.addAll(dataModifierList);
            return this;
        }
        @Override
        public Builder stateModifierList(List<BlockStateModifier> stateModifierList) {
            this.stateModifierList.addAll(stateModifierList);
            return this;
        }
        @Override
        public Builder horizontalVector(MathValue<Player> horizontalVector) {
            this.horizontalVector = horizontalVector;
            return this;
        }
        @Override
        public Builder verticalVector(MathValue<Player> verticalVector) {
            this.verticalVector = verticalVector;
            return this;
        }
        @Override
        public BlockConfig build() {
            return new BlockConfigImpl(blockID, dataModifierList, stateModifierList, horizontalVector, verticalVector);
        }
    }
}
