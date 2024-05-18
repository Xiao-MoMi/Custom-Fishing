package net.momirealms.customfishing.api.mechanic.event;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.ItemType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * The EventCarrier interface represents an object that carries events in the custom fishing system.
 * It defines methods to trigger actions based on specific triggers and contexts.
 */
public interface EventCarrier {

    /**
     * Get the type of item
     *
     * @return type
     */
    ItemType type();

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
         * Sets the map of actions associated with their triggers.
         *
         * @param actionMap the map of actions associated with their triggers.
         * @return the Builder instance.
         */
        Builder actionMap(HashMap<ActionTrigger, Action<Player>[]> actionMap);

        /**
         * Sets the map of actions associated with their triggers and occurrence times.
         *
         * @param actionTimesMap the map of actions associated with their triggers and occurrence times.
         * @return the Builder instance.
         */
        Builder actionTimesMap(HashMap<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap);

        /**
         * Set the type of the item
         *
         * @param type type
         * @return the Builder instance.
         */
        Builder type(ItemType type);

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