package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.condition.Condition;

public interface EffectModifier {

    void modify(FishingEffect effect, Condition condition);
}
