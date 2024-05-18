package net.momirealms.customfishing.api.mechanic.event;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionManager;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.ItemType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public interface EventManager {

    Map<ItemType, Map<ActionTrigger, Action<Player>[]>> GLOBAL_ACTIONS = new HashMap<>();

    Map<ItemType, Map<ActionTrigger, TreeMap<Integer, Action<Player>[]>>> GLOBAL_TIMES_ACTION = new HashMap<>();

    Optional<EventCarrier> getEventCarrier(String id);

    boolean registerEventCarrier(String id, EventCarrier carrier);

    default void trigger(Context<Player> context, String id, ActionTrigger trigger) {
        getEventCarrier(id).ifPresent(carrier -> trigger(context, carrier, trigger));
    }

    default void trigger(Context<Player> context, String id, ActionTrigger trigger, int previousTimes, int afterTimes) {
        getEventCarrier(id).ifPresent(carrier -> trigger(context, carrier, trigger, previousTimes, afterTimes));
    }

    static void trigger(Context<Player> context, EventCarrier carrier, ActionTrigger trigger) {
        if (!carrier.disableGlobalActions()) {
            triggerGlobalActions(context, carrier.type(), trigger);
        }
        carrier.trigger(context, trigger);
    }

    static void trigger(Context<Player> context, EventCarrier carrier, ActionTrigger trigger, int previousTimes, int afterTimes) {
        if (!carrier.disableGlobalActions()) {
            triggerGlobalActions(context, carrier.type(), trigger, previousTimes, afterTimes);
        }
        carrier.trigger(context, trigger, previousTimes, afterTimes);
    }

    static void triggerGlobalActions(Context<Player> context, ItemType type, ActionTrigger trigger) {
        Optional.ofNullable(GLOBAL_ACTIONS.get(type))
                .flatMap(actionTriggerMap -> Optional.ofNullable(actionTriggerMap.get(trigger)))
                .ifPresent(action -> ActionManager.trigger(context, action));
    }

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
