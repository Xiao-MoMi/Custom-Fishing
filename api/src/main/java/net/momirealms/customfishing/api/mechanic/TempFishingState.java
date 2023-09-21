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

/**
 * Represents a temporary state during fishing that includes an effect, preparation, and loot.
 */
public class TempFishingState {

    private final Effect effect;
    private final FishingPreparation preparation;
    private Loot loot;

    /**
     * Creates a new instance of TempFishingState.
     *
     * @param effect       The effect associated with this state.
     * @param preparation  The fishing preparation associated with this state.
     * @param loot         The loot associated with this state.
     */
    public TempFishingState(Effect effect, FishingPreparation preparation, Loot loot) {
        this.effect = effect;
        this.preparation = preparation;
        this.loot = loot;
    }

    /**
     * Gets the effect associated with this fishing state.
     *
     * @return The effect.
     */
    public Effect getEffect() {
        return effect;
    }

    /**
     * Gets the fishing preparation associated with this fishing state.
     *
     * @return The fishing preparation.
     */
    public FishingPreparation getPreparation() {
        return preparation;
    }

    /**
     * Gets the loot associated with this fishing state.
     *
     * @return The loot.
     */
    public Loot getLoot() {
        return loot;
    }

    /**
     * Set the loot associated with this fishing state.
     *
     */
    public void setLoot(Loot loot) {
        this.loot = loot;
    }
}
