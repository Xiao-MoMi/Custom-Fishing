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

package net.momirealms.customfishing.api.mechanic;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents global settings for actions related to fishing, loot, rods, and bait.
 */
public class GlobalSettings {

    public static HashMap<ActionTrigger, Action[]> lootActions = new HashMap<>();
    public static HashMap<ActionTrigger, Action[]> rodActions = new HashMap<>();
    public static HashMap<ActionTrigger, Action[]> baitActions = new HashMap<>();
    public static HashMap<ActionTrigger, Action[]> hookActions = new HashMap<>();

    /**
     * Loads global settings from a configuration section.
     *
     * @param section The configuration section to load settings from.
     */
    public static void load(ConfigurationSection section) {
        if (section == null) return;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection inner) {
                HashMap<ActionTrigger, Action[]> map = CustomFishingPlugin.get().getActionManager().getActionMap(inner);
                switch (entry.getKey()) {
                    case "loot" -> lootActions = map;
                    case "rod" -> rodActions = map;
                    case "bait" -> baitActions = map;
                    case "hook" -> hookActions = map;
                }
            }
        }
    }

    /**
     * Unloads global settings, clearing all action maps.
     */
    public static void unload() {
        lootActions.clear();
        rodActions.clear();
        baitActions.clear();
    }

    /**
     * Triggers loot-related actions for a specific trigger and condition.
     *
     * @param trigger   The trigger to activate actions for.
     * @param condition The condition that triggered the actions.
     */
    public static void triggerLootActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = lootActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    /**
     * Triggers rod-related actions for a specific trigger and condition.
     *
     * @param trigger   The trigger to activate actions for.
     * @param condition The condition that triggered the actions.
     */
    public static void triggerRodActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = rodActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    /**
     * Triggers bait-related actions for a specific trigger and condition.
     *
     * @param trigger   The trigger to activate actions for.
     * @param condition The condition that triggered the actions.
     */
    public static void triggerBaitActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = baitActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }

    /**
     * Triggers hook-related actions for a specific trigger and condition.
     *
     * @param trigger   The trigger to activate actions for.
     * @param condition The condition that triggered the actions.
     */
    public static void triggerHookActions(ActionTrigger trigger, Condition condition) {
        Action[] actions = hookActions.get(trigger);
        if (actions != null) {
            for (Action action : actions) {
                action.trigger(condition);
            }
        }
    }
}
