package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import net.momirealms.customfishing.common.util.Key;

import java.util.Optional;

public interface EffectManager extends Reloadable {

    boolean registerEffectModifier(Key key, EffectModifier effect);

    Optional<EffectModifier> getEffectModifier(Key key);
}
