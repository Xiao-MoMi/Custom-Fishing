package net.momirealms.customfishing.integration.block;

import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicListener;
import net.momirealms.customfishing.integration.BlockInterface;
import net.momirealms.customfishing.manager.TotemManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class OraxenBlockImpl implements BlockInterface {

    @Override
    public void removeBlock(Block block) {
        block.setType(Material.AIR);
    }

    @Override
    public void placeBlock(String id, Location location) {
        if (BlockInterface.isVanillaItem(id)) {
            location.getBlock().setType(Material.valueOf(id));
        }
        else {
            NoteBlockMechanicFactory.setBlockModel(location.getBlock(), id);
        }
    }

    @Override
    public void replaceBlock(Location location, String id) {
        placeBlock(id, location);
    }

    @Nullable
    @Override
    public String getID(Block block) {
        NoteBlockMechanic mechanic = NoteBlockMechanicListener.getNoteBlockMechanic(block);
        String id;
        if (mechanic == null) {
            id = block.getType().name();
        }
        else {
            id = mechanic.getItemID();
        }
        return TotemManager.BLOCKS.get(id);
    }
}
