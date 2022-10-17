package net.momirealms.customfishing.integration.block;

import dev.lone.itemsadder.api.CustomBlock;
import net.momirealms.customfishing.integration.BlockInterface;
import net.momirealms.customfishing.manager.TotemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderBlockImpl implements BlockInterface {

    @Override
    public void removeBlock(Block block) {
        if (CustomBlock.byAlreadyPlaced(block) != null) {
            CustomBlock.remove(block.getLocation());
        }
        else {
            block.setType(Material.AIR);
        }
    }

    @Override
    public void placeBlock(String id, Location location) {
        if (BlockInterface.isVanillaItem(id)) {
            location.getBlock().setType(Material.valueOf(id));
        }
        else {
            CustomBlock.place(id, location);
        }
    }

    @Override
    @Nullable
    public String getID(Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        String id;
        if (customBlock == null) {
            id = block.getType().name();
        }
        else {
            id = customBlock.getNamespacedID();
        }
        return TotemManager.BLOCKS.get(id);
    }
}