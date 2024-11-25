package net.momirealms.customfishing.bukkit.integration.block;

import net.momirealms.customfishing.api.integration.BlockProvider;
import net.momirealms.customfishing.api.mechanic.block.BlockDataModifier;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

public class NexoBlockProvider implements BlockProvider {

	@Override
	public String identifier() {
		return "Nexo";
	}

	@Override
	public BlockData blockData(@NotNull Context<Player> context, @NotNull String id, List<BlockDataModifier> modifiers) {
		return null;
	}

	@Override
	public @Nullable String blockID(@NotNull Block block) {
		try {
			Class<?> nexoBlocksClass = Class.forName("com.nexomc.nexo.api.NexoBlocks");
			Method customBlockMechanicMethod = nexoBlocksClass.getDeclaredMethod("customBlockMechanic", block.getBlockData().getClass());

			Object mechanic = customBlockMechanicMethod.invoke(null, block.getBlockData());
			if (mechanic == null) return null;

			Class<?> mechanicClass = Class.forName("com.nexomc.nexo.mechanics.Mechanic");
			Method getItemIDMethod = mechanicClass.getDeclaredMethod("getItemID");

			return (String) getItemIDMethod.invoke(mechanic);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}
}
