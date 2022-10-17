package net.momirealms.customfishing.integration.block;

import net.momirealms.customfishing.integration.BlockInterface;
import net.momirealms.customfishing.manager.TotemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class VanillaBlockImpl implements BlockInterface {
    @Override
    public void removeBlock(Block block) {
        block.setType(Material.AIR);
    }

    @Override
    public void placeBlock(String id, Location location) {
        location.getBlock().setType(Material.valueOf(id));
    }

    @Nullable
    @Override
    public String getID(Block block) {
        return TotemManager.BLOCKS.get(block.getType().name());
    }

    @Override
    public void replaceBlock(Location location, String id) {
        placeBlock(id, location);
    }
}
