package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.jetbrains.annotations.Nullable;

public interface EffectManager {
    boolean registerEffect(Key key, Effect effect);

    boolean unregisterEffect(Key key);

    @Nullable Effect getEffect(String namespace, String id);

    Effect getInitialEffect();
}
