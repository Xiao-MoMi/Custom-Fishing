package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.mechanic.effect.Effect;

public interface HookMechanic
{
    boolean canStart();

    boolean shouldStop();

    void preStart();

    void start(Effect finalEffect);

    boolean isHooked();

    void destroy();
}
