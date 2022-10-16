package net.momirealms.customfishing.integration.block;

import dev.lone.itemsadder.api.CustomBlock;
import net.momirealms.customfishing.integration.BlockInterface;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class ItemsAdderBlockHook implements BlockInterface {

    @Override
    public void removeBlock(Block block) {
        CustomBlock.remove(block.getLocation());
    }

    @Override
    public void placeBlock(String id, Location location) {
        CustomBlock.place(id, location);
    }

    @Override
    @Nullable
    public String getID(Block block) {
        CustomBlock customBlock = CustomBlock.byAlreadyPlaced(block);
        return customBlock == null ? null : customBlock.getNamespacedID();
    }
}
