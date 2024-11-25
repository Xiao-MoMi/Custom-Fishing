/*
 *  Copyright (C) <2024> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.bukkit.integration.item;

import net.momirealms.customfishing.api.integration.ItemProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class NexoItemProvider implements ItemProvider {

	@Override
	public String identifier() {
		return "Nexo";
	}

	@Override
	public @NotNull ItemStack buildItem(@NotNull Player player, @NotNull String id) {
		try {
			Class<?> nexoItemClass = Class.forName("com.nexomc.nexo.api.NexoItems");
			Method itemFromIdMethod = nexoItemClass.getMethod("itemFromId", String.class);
			Object itemBuilder = itemFromIdMethod.invoke(null, id);

			if (itemBuilder == null) return new ItemStack(Material.AIR);

			Class<?> nexoItemBuilderClass = Class.forName("com.nexomc.nexo.items.ItemBuilder");
			Method buildMethod = nexoItemBuilderClass.getDeclaredMethod("build");

			return (ItemStack) buildMethod.invoke(itemBuilder);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return new ItemStack(Material.AIR);
		}
	}

	@Override
	public @Nullable String itemID(@NotNull ItemStack itemStack) {
		try {
			Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems");
			Method idFromItemMethod = nexoItemsClass.getMethod("idFromItem", ItemStack.class);

			return (String) idFromItemMethod.invoke(null, itemStack);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			return null;
		}
	}
}