package net.momirealms.customfishing.api.mechanic.block;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class EmptyBlockDataModifier implements BlockDataModifier {

    public static final BlockDataModifier INSTANCE = new EmptyBlockDataModifier();

    @Override
    public void apply(Context<Player> context, BlockData blockData) {
    }
}
