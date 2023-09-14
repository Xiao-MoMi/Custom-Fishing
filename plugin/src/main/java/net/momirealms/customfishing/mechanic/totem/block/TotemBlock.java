package net.momirealms.customfishing.mechanic.totem.block;

import net.momirealms.customfishing.mechanic.totem.block.type.TypeCondition;
import org.bukkit.block.Block;

import java.io.Serializable;

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
}
