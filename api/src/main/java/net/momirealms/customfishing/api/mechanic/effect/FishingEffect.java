/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
