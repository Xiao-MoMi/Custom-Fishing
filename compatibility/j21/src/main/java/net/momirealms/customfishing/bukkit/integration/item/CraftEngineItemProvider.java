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

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.customfishing.api.integration.ItemProvider;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class CraftEngineItemProvider implements ItemProvider {

    @Override
    public String identifier() {
        return "CraftEngine";
    }

    @NotNull
    @Override
    public ItemStack buildItem(@NotNull Player player, @NotNull String id) {
        ItemStack itemStack = BukkitCraftEngine.instance().itemManager().buildItemStack(Key.of(id), BukkitCraftEngine.instance().adapt(player));
        return requireNonNull(itemStack, "Item not found in CraftEngine: " + id);
    }

    @Override
    public String itemID(@NotNull ItemStack itemStack) {
        return Optional.ofNullable(BukkitCraftEngine.instance().itemManager().itemId(itemStack)).map(Key::toString).orElse(null);
    }
}
