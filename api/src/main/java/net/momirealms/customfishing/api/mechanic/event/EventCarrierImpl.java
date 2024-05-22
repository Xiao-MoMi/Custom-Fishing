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

import static java.util.Objects.requireNonNull;

public class EventCarrierImpl implements EventCarrier {

    private final HashMap<ActionTrigger, Action<Player>[]> actionMap;
    private final HashMap<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap;
    private final ItemType type;
    private final boolean disableGlobalActions;
    private final String id;

    public EventCarrierImpl(String id, ItemType type, boolean disableGlobalActions, HashMap<ActionTrigger, Action<Player>[]> actionMap, HashMap<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap) {
        this.actionMap = actionMap;
        this.actionTimesMap = actionTimesMap;
        this.type = type;
        this.disableGlobalActions = disableGlobalActions;
        this.id = id;
    }

    @Override
    public ItemType type() {
        return type;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean disableGlobalActions() {
        return disableGlobalActions;
    }

    @Override
    public void trigger(Context<Player> context, ActionTrigger trigger) {
        Optional.ofNullable(actionMap.get(trigger)).ifPresent(actions -> {
            ActionManager.trigger(context, actions);
        });
    }

    @Override
    public void trigger(Context<Player> context, ActionTrigger trigger, int previousTimes, int afterTimes) {
        Optional.ofNullable(actionTimesMap.get(trigger)).ifPresent(integerTreeMap -> {
            for (Map.Entry<Integer, Action<Player>[]> entry : integerTreeMap.entrySet()) {
                if (entry.getKey() <= previousTimes)
                    continue;
                if (entry.getKey() > afterTimes)
                    return;
                ActionManager.trigger(context, entry.getValue());
            }
        });
    }

    public static class BuilderImpl implements Builder {
        private final HashMap<ActionTrigger, Action<Player>[]> actionMap = new HashMap<>();
        private final HashMap<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap = new HashMap<>();
        private ItemType type = null;
        private boolean disableGlobalActions = false;
        private String id;
        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        @Override
        public Builder actionMap(HashMap<ActionTrigger, Action<Player>[]> actionMap) {
            this.actionMap.putAll(actionMap);
            return this;
        }
        @Override
        public Builder action(ActionTrigger trigger, Action<Player>[] actions) {
            this.actionMap.put(trigger, actions);
            return this;
        }
        @Override
        public Builder actionTimesMap(HashMap<ActionTrigger, TreeMap<Integer, Action<Player>[]>> actionTimesMap) {
            this.actionTimesMap.putAll(actionTimesMap);
            return this;
        }
        @Override
        public Builder actionTimes(ActionTrigger trigger, TreeMap<Integer, Action<Player>[]> actions) {
            this.actionTimesMap.put(trigger, actions);
            return this;
        }
        @Override
        public Builder type(ItemType type) {
            this.type = type;
            return this;
        }
        @Override
        public Builder disableGlobalActions(boolean value) {
            this.disableGlobalActions = value;
            return this;
        }
        @Override
        public EventCarrier build() {
            return new EventCarrierImpl(requireNonNull(id), requireNonNull(type), disableGlobalActions, actionMap, actionTimesMap);
        }
    }
}
