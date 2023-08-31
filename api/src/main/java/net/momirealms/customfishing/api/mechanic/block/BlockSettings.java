package net.momirealms.customfishing.api.mechanic.block;

import java.util.List;

public interface BlockSettings {
    String getBlockID();

    List<BlockDataModifier> getDataModifier();

    List<BlockStateModifier> getStateModifierList();

    boolean isPersist();

    double getHorizontalVector();

    double getVerticalVector();
}
