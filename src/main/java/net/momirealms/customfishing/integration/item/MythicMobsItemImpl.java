/*
 *  Copyright (C) <2022> <XiaoMoMi>
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

package net.momirealms.customfishing.integration.item;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.ItemExecutor;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class MythicMobsItemImpl implements ItemInterface {

    private ItemExecutor itemManager;

    @Override
    @Nullable
    public ItemStack build(String material) {
        if (!material.startsWith("MythicMobs:")) return null;
        material = material.substring(11);
        if (itemManager == null) {
            this.itemManager = MythicBukkit.inst().getItemManager();
        }
        return itemManager.getItemStack(material);
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        return false;
    }
}
