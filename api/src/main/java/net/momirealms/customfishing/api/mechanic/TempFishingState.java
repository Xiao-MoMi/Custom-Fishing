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
