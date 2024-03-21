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

package net.momirealms.customfishing.api.mechanic.condition;

import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.effect.FishingEffect;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FishingPreparation extends Condition {

    public FishingPreparation(Player player) {
        super(player);
    }

    /**
     * Retrieves the ItemStack representing the fishing rod.
     *
     * @return The ItemStack representing the fishing rod.
     */
    @NotNull
    public abstract ItemStack getRodItemStack();

    /**
     * Retrieves the ItemStack representing the bait (if available).
     *
     * @return The ItemStack representing the bait, or null if no bait is set.
     */
    @Nullable
    public abstract ItemStack getBaitItemStack();

    /**
     * Checks if player meet the requirements for fishing gears
     *
     * @return True if can fish, false otherwise.
     */
    public abstract boolean canFish();

    /**
     * Merges a FishingEffect into this fishing rod, applying effect modifiers.
     *
     * @param effect The FishingEffect to merge into this rod.
     */
    public abstract void mergeEffect(FishingEffect effect);

    /**
     * Triggers actions associated with a specific action trigger.
     *
     * @param actionTrigger The action trigger that initiates the actions.
     */
    public abstract void triggerActions(ActionTrigger actionTrigger);
}
