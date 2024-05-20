package net.momirealms.customfishing.api.mechanic.block;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public class EmptyBlockStateModifier implements BlockStateModifier {

    public static final EmptyBlockStateModifier INSTANCE = new EmptyBlockStateModifier();

    @Override
    public void apply(Context<Player> context, BlockState blockState) {
    }
}
