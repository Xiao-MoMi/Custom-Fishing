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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.hook.HookSetting;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface HookManager {

    /**
     * Get the hook setting by its ID.
     *
     * @param id The ID of the hook setting to retrieve.
     * @return The hook setting with the given ID, or null if not found.
     */
    @Nullable HookSetting getHookSetting(String id);

    /**
     * Decreases the durability of a fishing hook by a specified amount and optionally updates its lore.
     * The hook would be removed if its durability is lower than 0
     *
     * @param rod         The fishing rod ItemStack to modify.
     * @param amount      The amount by which to decrease the durability.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    void decreaseHookDurability(ItemStack rod, int amount, boolean updateLore);

    /**
     * Increases the durability of a hook by a specified amount and optionally updates its lore.
     *
     * @param rod         The fishing rod ItemStack to modify.
     * @param amount      The amount by which to increase the durability.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    void increaseHookDurability(ItemStack rod, int amount, boolean updateLore);

    /**
     * Sets the durability of a fishing hook to a specific amount and optionally updates its lore.
     *
     * @param rod         The fishing rod ItemStack to modify.
     * @param amount      The new durability value to set.
     * @param updateLore  Whether to update the lore of the fishing rod.
     */
    void setHookDurability(ItemStack rod, int amount, boolean updateLore);

    /**
     * Equips a fishing hook on a fishing rod.
     *
     * @param rod  The fishing rod ItemStack.
     * @param hook The fishing hook ItemStack.
     * @return True if the hook was successfully equipped, false otherwise.
     */
    boolean equipHookOnRod(ItemStack rod, ItemStack hook);

    /**
     * Removes the fishing hook from a fishing rod.
     *
     * @param rod The fishing rod ItemStack.
     * @return The removed fishing hook ItemStack, or null if no hook was found.
     */
    @Nullable ItemStack removeHookFromRod(ItemStack rod);
}
