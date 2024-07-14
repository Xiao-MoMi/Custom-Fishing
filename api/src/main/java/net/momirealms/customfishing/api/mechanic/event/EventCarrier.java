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

package net.momirealms.customfishing.api.mechanic.event;

import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * The EventCarrier interface represents an object that carries events.
 * It defines methods to trigger actions based on specific triggers and contexts.
 */
public interface EventCarrier {

    /**
     * Get the type of item
     *
     * @return type
     */
    MechanicType type();

    /**
     * Gets the ID
     *
     * @return the ID.
     */
    String id();

    /**
     * Whether to disable global actions
     *
     * @return disable global actions or not
     */
    boolean disableGlobalActions();

    /**
     * Triggers actions based on the given context and trigger.
     *
     * @param context the context of the event, typically containing information about the player involved.
     * @param trigger the trigger that activates the actions.
     */
    void trigger(Context<Player> context, ActionTrigger trigger);

    /**
     * Triggers actions based on the given context, trigger, and action occurrence times.
     *
     * @param context       the context of the event, typically containing information about the player involved.
     * @param trigger       the trigger that activates the actions.
     * @param previousTimes the number of times the action has been triggered before.
     * @param afterTimes    the number of times the action will be triggered after.
     */
    void trigger(Context<Player> context, ActionTrigger trigger, int previousTimes, int afterTimes);

    /**
     * Creates a new Builder instance for constructing EventCarrier objects.
     *
     * @return a new Builder instance.
     */
    static Builder builder() {
        return new EventCarrierImpl.BuilderImpl();
    }

    /**
     * The Builder interface provides a fluent API for constructing EventCarrier instances.
     */
    interface Builder {

        /**
         * Sets the ID
         *
         * @return the current Builder instance
         */
        Builder id(String id);

        /**
         * Sets the map of actions associated with their triggers.
         *
         * @param actionMap the map of actions associated with their triggers.
         * @return the Builder instance.
         */
        Builder actionMap(HashMap<ActionTrigger, Action<Player>[]> actionMap);

        /**
         * Associates an array of actions with a specific trigger.
         *
         * @param trigger the trigger that activates the actions.
         * @param actions the array of actions to be triggered.
         * @return the Builder instance.
         */
        Builder action(ActionTrigger trigger, Action<Player>[] actions);

        /**
         * Sets the map of actions associated with their triggers and occurrence times.
         *
         * @param actionTimesMap the map of actions associated with their triggers and occurrence times.
         * @return the Builder instance.
         */
        Builder actionTimesMap(HashMap<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap);

        /**
         * Associates a TreeMap of actions with a specific trigger and occurrence times.
         *
         * @param trigger the trigger that activates the actions.
         * @param actions the TreeMap of actions to be triggered at specific occurrence times.
         * @return the Builder instance.
         */
        Builder actionTimes(ActionTrigger trigger, TreeMap<Integer, Action<Player>[]> actions);

        /**
         * Set the type of the item
         *
         * @param type type
         * @return the Builder instance.
         */
        Builder type(MechanicType type);

        /**
         * Set whether to disable global events
         *
         * @param value disable or not
         * @return the Builder instance.
         */
        Builder disableGlobalActions(boolean value);

        /**
         * Builds and returns the EventCarrier instance.
         *
         * @return the constructed EventCarrier instance.
         */
        EventCarrier build();
    }
}