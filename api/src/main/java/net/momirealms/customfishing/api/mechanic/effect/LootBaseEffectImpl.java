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

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;

public class LootBaseEffectImpl implements LootBaseEffect {

    private final MathValue<Player> waitTimeAdder;
    private final MathValue<Player> waitTimeMultiplier;
    private final MathValue<Player> difficultyAdder;
    private final MathValue<Player> difficultyMultiplier;
    private final MathValue<Player> gameTimeAdder;
    private final MathValue<Player> gameTimeMultiplier;

    public LootBaseEffectImpl(
            MathValue<Player> waitTimeAdder,
            MathValue<Player> waitTimeMultiplier,
            MathValue<Player> difficultyAdder,
            MathValue<Player> difficultyMultiplier,
            MathValue<Player> gameTimeAdder,
            MathValue<Player> gameTimeMultiplier
    ) {
        this.waitTimeAdder = waitTimeAdder;
        this.waitTimeMultiplier = waitTimeMultiplier;
        this.difficultyAdder = difficultyAdder;
        this.difficultyMultiplier = difficultyMultiplier;
        this.gameTimeAdder = gameTimeAdder;
        this.gameTimeMultiplier = gameTimeMultiplier;
    }

    @Override
    public MathValue<Player> waitTimeAdder() {
        return waitTimeAdder;
    }

    @Override
    public MathValue<Player> waitTimeMultiplier() {
        return waitTimeMultiplier;
    }

    @Override
    public MathValue<Player> difficultyAdder() {
        return difficultyAdder;
    }

    @Override
    public MathValue<Player> difficultyMultiplier() {
        return difficultyMultiplier;
    }

    @Override
    public MathValue<Player> gameTimeAdder() {
        return gameTimeAdder;
    }

    @Override
    public MathValue<Player> gameTimeMultiplier() {
        return gameTimeMultiplier;
    }

    @Override
    public Effect toEffect(Context<Player> context) {
        Effect effect = Effect.newInstance();
        effect.waitTimeAdder(waitTimeAdder.evaluate(context));
        effect.waitTimeMultiplier(waitTimeMultiplier.evaluate(context));
        effect.difficultyAdder(difficultyAdder.evaluate(context));
        effect.difficultyMultiplier(difficultyMultiplier.evaluate(context));
        effect.gameTimeAdder(gameTimeAdder.evaluate(context));
        effect.gameTimeMultiplier(gameTimeMultiplier.evaluate(context));
        return effect;
    }

    public static class BuilderImpl implements Builder {
        private MathValue<Player> waitTimeAdder = DEFAULT_WAIT_TIME_ADDER;
        private MathValue<Player> waitTimeMultiplier = DEFAULT_WAIT_TIME_MULTIPLIER;
        private MathValue<Player> difficultyAdder = DEFAULT_DIFFICULTY_ADDER;
        private MathValue<Player> difficultyMultiplier = DEFAULT_DIFFICULTY_MULTIPLIER;
        private MathValue<Player> gameTimeAdder = DEFAULT_GAME_TIME_ADDER;
        private MathValue<Player> gameTimeMultiplier = DEFAULT_GAME_TIME_MULTIPLIER;
        @Override
        public Builder waitTimeAdder(MathValue<Player> waitTimeAdder) {
            this.waitTimeAdder = waitTimeAdder;
            return this;
        }
        @Override
        public Builder waitTimeMultiplier(MathValue<Player> waitTimeMultiplier) {
            this.waitTimeMultiplier = waitTimeMultiplier;
            return this;
        }
        @Override
        public Builder difficultyAdder(MathValue<Player> difficultyAdder) {
            this.difficultyAdder = difficultyAdder;
            return this;
        }
        @Override
        public Builder difficultyMultiplier(MathValue<Player> difficultyMultiplier) {
            this.difficultyMultiplier = difficultyMultiplier;
            return this;
        }
        @Override
        public Builder gameTimeAdder(MathValue<Player> gameTimeAdder) {
            this.gameTimeAdder = gameTimeAdder;
            return this;
        }
        @Override
        public Builder gameTimeMultiplier(MathValue<Player> gameTimeMultiplier) {
            this.gameTimeMultiplier = gameTimeMultiplier;
            return this;
        }
        @Override
        public LootBaseEffect build() {
            return new LootBaseEffectImpl(waitTimeAdder, waitTimeMultiplier, difficultyAdder, difficultyMultiplier, gameTimeAdder, gameTimeMultiplier);
        }
    }
}
