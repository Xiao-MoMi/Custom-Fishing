package net.momirealms.customfishing.integration.block;

import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanic;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicListener;
import net.momirealms.customfishing.integration.BlockInterface;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

public class OraxenBlockHook implements BlockInterface {

    @Override
    public void removeBlock(Block block) {
        block.setType(Material.AIR);
    }

    @Override
    public void placeBlock(String id, Location location) {
        NoteBlockMechanicFactory.setBlockModel(location.getBlock(), id);
    }

    @Nullable
    @Override
    public String getID(Block block) {
        NoteBlockMechanic mechanic = NoteBlockMechanicListener.getNoteBlockMechanic(block);
        if (mechanic == null) return null;
        else return mechanic.getItemID();
    }
}
