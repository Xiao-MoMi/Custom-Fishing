package net.momirealms.customfishing.api.mechanic.block;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.List;

public interface BlockLibrary {

    String identification();

    BlockData getBlockData(Player player, String id, List<BlockDataModifier> modifiers);
}
