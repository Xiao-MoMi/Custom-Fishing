package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;

import java.util.Optional;

public interface EffectManager extends Reloadable {

    boolean registerEffectModifier(String id, EffectModifier effect);

    Optional<EffectModifier> getEffectModifier(String id);
}
