package net.momirealms.customfishing.api.mechanic;

import net.momirealms.customfishing.api.mechanic.condition.FishingPreparation;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.loot.Loot;

public class TempFishingState {

    private final Effect effect;
    private final FishingPreparation preparation;
    private final Loot loot;

    public TempFishingState(Effect effect, FishingPreparation preparation, Loot loot) {
        this.effect = effect;
        this.preparation = preparation;
        this.loot = loot;
    }

    public Effect getEffect() {
        return effect;
    }

    public FishingPreparation getPreparation() {
        return preparation;
    }

    public Loot getLoot() {
        return loot;
    }
}
