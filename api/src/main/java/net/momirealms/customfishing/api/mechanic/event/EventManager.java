package net.momirealms.customfishing.api.mechanic.event;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.ItemType;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * EventManager interface for managing events and their associated actions within the custom fishing plugin.
 * It provides methods to register, retrieve, and trigger events based on different conditions.
 */
public interface EventManager extends Reloadable {

    /**
     * A map storing global actions for different item types and triggers.
     */
    Map<ItemType, Map<ActionTrigger, Action<Player>[]>> GLOBAL_ACTIONS = new HashMap<>();

    /**
     * A map storing global timed actions for different item types and triggers.
     */
    Map<ItemType, Map<ActionTrigger, TreeMap<Integer, Action<Player>[]>>> GLOBAL_TIMES_ACTION = new HashMap<>();

    /**
     * Retrieves an EventCarrier by its identifier.
     *
     * @param id The unique identifier of the event carrier.
     * @return An Optional containing the EventCarrier if found, or an empty Optional if not found.
     */
    Optional<EventCarrier> getEventCarrier(String id);

    /**
     * Registers a new EventCarrier with a specified identifier.
     *
     * @param carrier The EventCarrier to be registered.
     * @return True if the registration was successful, false otherwise.
     */
    boolean registerEventCarrier(EventCarrier carrier);

    /**
     * Triggers an event for a given context, identifier, and trigger.
     *
     * @param context The context in which the event is triggered.
     * @param id      The unique identifier of the event carrier.
     * @param trigger The trigger that initiates the event.
     */
    default void trigger(Context<Player> context, String id, ActionTrigger trigger) {
        getEventCarrier(id).ifPresent(carrier -> trigger(context, carrier, trigger));
    }

    /**
     * Triggers an event for a given context, identifier, trigger, and a range of times.
     *
     * @param context       The context in which the event is triggered.
     * @param id            The unique identifier of the event carrier.
     * @param trigger       The trigger that initiates the event.
     * @param previousTimes The previous times count for the event.
     * @param afterTimes    The after times count for the event.
     */
    default void trigger(Context<Player> context, String id, ActionTrigger trigger, int previousTimes, int afterTimes) {
        getEventCarrier(id).ifPresent(carrier -> trigger(context, carrier, trigger, previousTimes, afterTimes));
    }

    /**
     * Triggers the event actions for a given context and trigger on a specified carrier.
     *
     * @param context The context in which the event is triggered.
     * @param carrier The event carrier.
     * @param trigger The trigger that initiates the event.
     */
    static void trigger(Context<Player> context, EventCarrier carrier, ActionTrigger trigger) {
        if (!carrier.disableGlobalActions()) {
            triggerGlobalActions(context, carrier.type(), trigger);
        }
        carrier.trigger(context, trigger);
    }

    /**
     * Triggers the event actions for a given context, trigger, and times range on a specified carrier.
     *
     * @param context       The context in which the event is triggered.
     * @param carrier       The event carrier.
     * @param trigger       The trigger that initiates the event.
     * @param previousTimes The previous times count for the event.
     * @param afterTimes    The after times count for the event.
     */
    static void trigger(Context<Player> context, EventCarrier carrier, ActionTrigger trigger, int previousTimes, int afterTimes) {
        if (!carrier.disableGlobalActions()) {
            triggerGlobalActions(context, carrier.type(), trigger, previousTimes, afterTimes);
        }
        carrier.trigger(context, trigger, previousTimes, afterTimes);
    }

    /**
     * Triggers global actions for a given context, item type, and trigger.
     *
     * @param context The context in which the event is triggered.
     * @param type    The type of item that triggered the event.
     * @param trigger The trigger that initiates the event.
     */
    static void triggerGlobalActions(Context<Player> context, ItemType type, ActionTrigger trigger) {
        Optional.ofNullable(GLOBAL_ACTIONS.get(type))
                .flatMap(actionTriggerMap -> Optional.ofNullable(actionTriggerMap.get(trigger)))
                .ifPresent(action -> ActionManager.trigger(context, action));
    }

    /**
     * Triggers global timed actions for a given context, item type, trigger, and times range.
     *
     * @param context       The context in which the event is triggered.
     * @param type          The type of item that triggered the event.
     * @param trigger       The trigger that initiates the event.
     * @param previousTimes The previous times count for the event.
     * @param afterTimes    The after times count for the event.
     */
    static void triggerGlobalActions(Context<Player> context, ItemType type, ActionTrigger trigger, int previousTimes, int afterTimes) {
        Optional.ofNullable(GLOBAL_TIMES_ACTION.get(type))
                .flatMap(actionTriggerMap -> Optional.ofNullable(actionTriggerMap.get(trigger)))
                .ifPresent(integerTreeMap -> {
                    for (Map.Entry<Integer, Action<Player>[]> entry : integerTreeMap.entrySet()) {
                        if (entry.getKey() <= previousTimes)
                            continue;
                        if (entry.getKey() > afterTimes)
                            return;
                        ActionManager.trigger(context, entry.getValue());
                    }
                });
    }
}
