package net.momirealms.customfishing.integration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface BlockInterface {

    void removeBlock(Block block);
    void placeBlock(String id, Location location);
    @Nullable
    String getID(Block block);
}
