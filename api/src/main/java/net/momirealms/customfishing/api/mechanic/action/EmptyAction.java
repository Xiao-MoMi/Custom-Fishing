package net.momirealms.customfishing.api.mechanic.action;

import net.momirealms.customfishing.api.mechanic.context.Context;
import org.bukkit.entity.Player;

/**
 * An implementation of the Action interface that represents an empty action with no behavior.
 * This class serves as a default action to prevent NPE.
 */
public class EmptyAction implements Action<Player> {

    public static final EmptyAction INSTANCE = new EmptyAction();

    @Override
    public void trigger(Context<Player> context) {
    }
}
