package net.momirealms.customfishing.compatibility.block;

import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import net.momirealms.customfishing.api.mechanic.block.BlockLibrary;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VanillaBlockImpl implements BlockLibrary {

    @Override
    public String identification() {
        return "vanilla";
    }

    @Override
    public BlockData getBlockData(Player player, String id, List<BlockDataModifier> modifiers) {
        BlockData blockData = Material.valueOf(id.toUpperCase(Locale.ENGLISH)).createBlockData();
        for (BlockDataModifier modifier : modifiers) {
            modifier.apply(player, blockData);
        }
        return blockData;
    }
}
