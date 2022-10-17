package net.momirealms.customfishing.integration;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public interface BlockInterface {

    void removeBlock(Block block);
    void placeBlock(String id, Location location);
    @Nullable
    String getID(Block block);

    default void replaceBlock(Location location, String id) {
        removeBlock(location.getBlock());
        placeBlock(id, location);
    }

    static boolean isVanillaItem(String item) {
        char[] chars = item.toCharArray();
        for (char character : chars) {
            if ((character < 65 || character > 90) && character != 95) {
                return false;
            }
        }
        return true;
    }
}
