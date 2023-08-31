package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.block.BlockLibrary;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifierBuilder;
import net.momirealms.customfishing.api.mechanic.block.BlockStateModifierBuilder;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface BlockManager {
    boolean registerBlockLibrary(BlockLibrary library);

    boolean unregisterBlockLibrary(BlockLibrary library);

    boolean unregisterBlockLibrary(String library);

    boolean registerBlockDataModifierBuilder(String type, BlockDataModifierBuilder builder);

    boolean registerBlockStateModifierBuilder(String type, BlockStateModifierBuilder builder);

    void summonBlock(Player player, Location hookLocation, Location playerLocation, Loot loot);
}
