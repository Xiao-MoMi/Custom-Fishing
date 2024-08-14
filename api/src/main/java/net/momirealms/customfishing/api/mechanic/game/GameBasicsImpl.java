/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record GameBasicsImpl(MathValue<Player> time, MathValue<Player> difficulty) implements GameBasics {

    public static class BuilderImpl implements Builder {
        private MathValue<Player> time;
        private MathValue<Player> difficulty;

        @Override
        public Builder difficulty(MathValue<Player> value) {
            this.difficulty = value;
            return this;
        }

        @Override
        public Builder time(MathValue<Player> value) {
            this.time = value;
            return this;
        }

        @Override
        public GameBasics build() {
            return new GameBasicsImpl(time, difficulty);
        }
    }

    @NotNull
    @Override
    public GameSetting toGameSetting(Context<Player> context, Effect effect) {
        return new GameSetting(
                time.evaluate(context) * effect.gameTimeMultiplier() + effect.gameTimeAdder(),
                Math.min(100, Math.max(1, difficulty.evaluate(context) * effect.difficultyMultiplier() + effect.difficultyAdder()))
        );
    }
}