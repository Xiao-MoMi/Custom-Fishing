package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.mechanic.effect.Effect;

public class VanillaMechanic implements HookMechanic {

    @Override
    public boolean canStart() {
        return false;
    }

    @Override
    public void preStart() {

    }

    @Override
    public void start(Effect finalEffect) {

    }

    @Override
    public boolean isHooked() {
        return false;
    }

    @Override
    public void destroy() {

    }
}
