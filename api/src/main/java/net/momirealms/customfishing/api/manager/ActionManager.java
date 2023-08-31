package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionBuilder;
import org.bukkit.configuration.ConfigurationSection;

public interface ActionManager {

    boolean registerAction(String type, ActionBuilder actionBuilder);

    boolean unregisterAction(String type);

    Action getAction(ConfigurationSection section);

    Action[] getActions(ConfigurationSection section);

    ActionBuilder getActionBuilder(String type);
}
