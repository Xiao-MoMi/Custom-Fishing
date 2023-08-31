package net.momirealms.customfishing.api.mechanic.block;

import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public interface BlockDataModifier {
    void apply(Player player, BlockData blockData);
}