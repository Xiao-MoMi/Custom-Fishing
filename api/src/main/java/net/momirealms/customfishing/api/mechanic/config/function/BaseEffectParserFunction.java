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

package net.momirealms.customfishing.api.mechanic.config.function;

import net.momirealms.customfishing.api.mechanic.effect.LootBaseEffect;

import java.util.function.Consumer;
import java.util.function.Function;

public class BaseEffectParserFunction implements ConfigParserFunction {

    private final Function<Object, Consumer<LootBaseEffect.Builder>> function;

    public BaseEffectParserFunction(Function<Object, Consumer<LootBaseEffect.Builder>> function) {
        this.function = function;
    }

    public Consumer<LootBaseEffect.Builder> accept(Object object) {
        return function.apply(object);
    }

    @Override
    public ParserType type() {
        return ParserType.BASE_EFFECT;
    }
}
