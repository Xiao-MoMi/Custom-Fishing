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

package net.momirealms.customfishing.api.mechanic.loot;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.BaseEffect;
import net.momirealms.customfishing.api.mechanic.statistic.StatisticsKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public interface Loot {

    /**
     * Check if this loot has an instance game.
     *
     * @return True if it's an instance game, false otherwise.
     */
    boolean instanceGame();

    /**
     * Check if the loot disables global actions
     */
    boolean disableGlobalAction();

    /**
     * Get the unique ID of this loot.
     *
     * @return The unique ID.
     */
    String getID();

    /**
     * Get the type of this loot.
     *
     * @return The loot type.
     */
    LootType getType();

    /**
     * Get the nickname of this loot.
     *
     * @return The nickname.
     */
    @NotNull
    String getNick();

    StatisticsKey getStatisticKey();

    /**
     * Check if this loot should be shown in the finder.
     *
     * @return True if it should be shown, false otherwise.
     */
    boolean showInFinder();

    /**
     * Get the score of this loot.
     *
     * @return The score.
     */
    double getScore();

    /**
     * Check if games are disabled for this loot.
     *
     * @return True if games are disabled, false otherwise.
     */
    boolean disableGame();

    /**
     * Check if statistics are disabled for this loot.
     *
     * @return True if statistics are disabled, false otherwise.
     */
    boolean disableStats();

    /**
     * Get the loot group of this loot.
     *
     * @return The loot group.
     */
    String[] getLootGroup();

    /**
     * Get the actions triggered by a specific action trigger.
     *
     * @param actionTrigger The action trigger.
     * @return The actions triggered by the given trigger.
     */
    @Nullable
    Action[] getActions(ActionTrigger actionTrigger);

    /**
     * Trigger actions associated with a specific action trigger.
     *
     * @param actionTrigger The action trigger.
     * @param condition     The condition under which the actions are triggered.
     */
    void triggerActions(ActionTrigger actionTrigger, Condition condition);

    /**
     * Get effects that bond to this loot
     *
     * @return effects
     */
    BaseEffect getBaseEffect();

    /**
     * Get the actions triggered by a specific number of successes.
     *
     * @param times The number of successes.
     * @return The actions triggered by the specified number of successes.
     */
    Action[] getSuccessTimesActions(int times);

    /**
     * Get a map of actions triggered by different numbers of successes.
     *
     * @return A map of actions triggered by success times.
     */
    HashMap<Integer, Action[]> getSuccessTimesActionMap();
}
