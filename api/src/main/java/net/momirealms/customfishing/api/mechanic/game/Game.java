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

package net.momirealms.customfishing.api.mechanic.game;

import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.fishing.CustomFishingHook;

/**
 * Represents a mini-game
 */
public interface Game {

    /**
     * Gets the identifier of the game.
     *
     * @return the identifier of the game.
     */
    String id();

    /**
     * Starts the game with the provided fishing hook and effect.
     *
     * @param hook the custom fishing hook.
     * @param effect the effect to apply.
     * @return the gaming player instance.
     */
    GamingPlayer start(CustomFishingHook hook, Effect effect);
}
