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

/**
 * Represents an expansion for a mini-game.
 */
public abstract class GameExpansion {

    /**
     * Gets the version of the game expansion.
     *
     * @return the version of the game expansion.
     */
    public abstract String getVersion();

    /**
     * Gets the author of the game expansion.
     *
     * @return the author of the game expansion.
     */
    public abstract String getAuthor();

    /**
     * Gets the type of the game expansion.
     *
     * @return the type of the game expansion.
     */
    public abstract String getGameType();

    /**
     * Gets the game factory for creating instances of the game.
     *
     * @return the game factory.
     */
    public abstract GameFactory getGameFactory();
}
