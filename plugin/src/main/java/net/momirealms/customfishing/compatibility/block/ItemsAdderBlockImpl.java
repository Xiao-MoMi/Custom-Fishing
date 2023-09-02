package net.momirealms.customfishing.compatibility.block;

import dev.lone.itemsadder.api.CustomBlock;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import net.momirealms.customfishing.api.mechanic.block.BlockLibrary;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.List;

public class ItemsAdderBlockImpl implements BlockLibrary {

    @Override
    public String identification() {
        return "ItemsAdder";
    }

    @Override
    public BlockData getBlockData(Player player, String id, List<BlockDataModifier> modifiers) {
        BlockData blockData = CustomBlock.getBaseBlockData(id);
        for (BlockDataModifier modifier : modifiers) {
            modifier.apply(player, blockData);
        }
        return blockData;
    }
}
