package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.bukkit.entity.FishHook;

public interface HookMechanic
{
    boolean canStart();

    void preStart();

    void start(Effect finalEffect);

    boolean isHooked();

    void destroy();
}
