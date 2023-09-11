package net.momirealms.customfishing.api.mechanic;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public class GlobalSettings {

    public static HashMap<ActionTrigger, Action[]> lootActions = new HashMap<>();
    public static HashMap<ActionTrigger, Action[]> rodActions = new HashMap<>();
    public static HashMap<ActionTrigger, Action[]> baitActions = new HashMap<>();

    public static void load(ConfigurationSection section) {
        if (section == null) return;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection inner) {
                HashMap<ActionTrigger, Action[]> map = CustomFishingPlugin.get().getActionManager().getActionMap(inner);
                switch (entry.getKey()) {
                    case "loot" -> lootActions = map;
                    case "rod" -> rodActions = map;
                    case "bait" -> baitActions = map;
                }
            }
        }
    }

    public static void unload() {
        lootActions.clear();
        rodActions.clear();
        baitActions.clear();
    }

    public static void triggerLootActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = lootActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    public static void triggerRodActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = rodActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    public static void triggerBaitActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = baitActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }
}
