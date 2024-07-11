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

import java.util.function.BiFunction;

/**
 * Represents an abstract game which provides the basic structure and functionalities for a game.
 */
public abstract class AbstractGame implements Game {

    private final GameBasics basics;
    private final String id;

    /**
     * Constructs an AbstractGame instance.
     *
     * @param id the identifier of the game.
     * @param basics the basic settings of the game.
     */
    public AbstractGame(String id, GameBasics basics) {
        this.basics = basics;
        this.id = id;
    }

    /**
     * Gets the identifier of the game.
     *
     * @return the identifier of the game.
     */
    @Override
    public String id() {
        return id;
    }

    /**
     * Starts the game with the provided fishing hook and effect.
     *
     * @param hook the custom fishing hook.
     * @param effect the effect to apply.
     * @return the gaming player instance.
     */
    @Override
    public GamingPlayer start(CustomFishingHook hook, Effect effect) {
        return gamingPlayerProvider().apply(hook, basics.toGameSetting(effect));
    }

    /**
     * Provides the gaming player provider function.
     *
     * @return the BiFunction providing the gaming player.
     */
    public abstract BiFunction<CustomFishingHook, GameSetting, AbstractGamingPlayer> gamingPlayerProvider();
}
