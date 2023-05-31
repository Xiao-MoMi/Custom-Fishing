package net.momirealms.customfishing.integration.block;

import com.mineinabyss.blocky.BlockyPlugin;
import com.mineinabyss.blocky.BlockyPluginKt;
import com.mineinabyss.blocky.api.BlockyBlocks;
import com.mineinabyss.geary.prefabs.PrefabKey;
import net.momirealms.customfishing.integration.BlockInterface;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class BlockyBlockImpl implements BlockInterface {

    @Override
    public void removeBlock(Block block) {
        BlockyBlocks.INSTANCE.removeBlockyBlock(block.getLocation());
    }

    @Override
    public void placeBlock(String id, Location location) {
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(id);
        if (prefabKey == null) return;
        BlockyBlocks.INSTANCE.placeBlockyBlock(location, prefabKey);
    }

    @Override
    public @Nullable String getID(Block block) {
        return BlockyPluginKt.getPrefabMap().get(block.getBlockData()).getFull();
    }
}
