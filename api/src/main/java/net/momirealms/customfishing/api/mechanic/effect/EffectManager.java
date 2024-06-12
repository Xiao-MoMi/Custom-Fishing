package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;

import java.util.Optional;

public interface EffectManager extends Reloadable {

    boolean registerEffectModifier(EffectModifier effect, MechanicType type);

    Optional<EffectModifier> getEffectModifier(String id, MechanicType type);
}
