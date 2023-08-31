package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.mechanic.loot.Modifier;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;

import java.util.List;

public class FishingEffect extends AbstractEffect {

    public static class Builder {

        private final FishingEffect effect;

        public Builder() {
            this.effect = new FishingEffect();
        }

        public Builder lootWeightModifier(List<Pair<String, Modifier>> modifier) {
            effect.lootWeightModifier = modifier;
            return this;
        }

        public Builder multipleLootChance(double multipleLootChance) {
            effect.multipleLootChance = multipleLootChance;
            return this;
        }

        public Builder difficultyModifier(double difficultyModifier) {
            effect.difficultyModifier = difficultyModifier;
            return this;
        }

        public Builder sizeMultiplier(double sizeMultiplier) {
            effect.sizeMultiplier = sizeMultiplier;
            return this;
        }

        public Builder timeModifier(double timeModifier) {
            effect.timeModifier = timeModifier;
            return this;
        }

        public Builder scoreMultiplier(double scoreMultiplier) {
            effect.scoreMultiplier = scoreMultiplier;
            return this;
        }

        public Builder gameTimeModifier(double gameTimeModifier) {
            effect.gameTimeModifier = gameTimeModifier;
            return this;
        }

        public Builder lavaFishing(boolean lavaFishing) {
            effect.lavaFishing = lavaFishing;
            return this;
        }

        public Builder requirements(Requirement[] requirements) {
            effect.requirements = requirements;
            return this;
        }

        public FishingEffect build() {
            return effect;
        }
    }
}
