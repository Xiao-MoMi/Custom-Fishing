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
