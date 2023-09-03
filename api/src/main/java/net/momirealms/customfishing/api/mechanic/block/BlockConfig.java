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

import java.util.List;

public class BlockConfig implements BlockSettings {

    private String blockID;
    private List<BlockDataModifier> dataModifierList;
    private List<BlockStateModifier> stateModifierList;
    private boolean persist;
    private double horizontalVector;
    private double verticalVector;

    @Override
    public String getBlockID() {
        return blockID;
    }

    @Override
    public List<BlockDataModifier> getDataModifier() {
        return dataModifierList;
    }

    @Override
    public List<BlockStateModifier> getStateModifierList() {
        return stateModifierList;
    }

    @Override
    public boolean isPersist() {
        return persist;
    }

    @Override
    public double getHorizontalVector() {
        return horizontalVector;
    }

    @Override
    public double getVerticalVector() {
        return verticalVector;
    }

    public static class Builder {

        private final BlockConfig config;

        public Builder() {
            this.config = new BlockConfig();
        }

        public Builder persist(boolean value) {
            config.persist = value;
            return this;
        }

        public Builder horizontalVector(double value) {
            config.horizontalVector = value;
            return this;
        }

        public Builder verticalVector(double value) {
            config.verticalVector = value;
            return this;
        }

        public Builder blockID(String value) {
            config.blockID = value;
            return this;
        }

        public Builder dataModifiers(List<BlockDataModifier> value) {
            config.dataModifierList = value;
            return this;
        }

        public Builder stateModifiers(List<BlockStateModifier> value) {
            config.stateModifierList = value;
            return this;
        }

        public BlockConfig build() {
            return config;
        }
    }
}
