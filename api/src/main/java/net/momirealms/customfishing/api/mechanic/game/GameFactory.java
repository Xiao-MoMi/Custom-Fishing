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

import dev.dejvokep.boostedyaml.block.implementation.Section;

/**
 * Factory interface for creating game instances.
 */
public interface GameFactory {

    /**
     * Creates a new game instance with the specified identifier and configuration section.
     *
     * @param id the identifier of the game.
     * @param section the configuration section for the game.
     * @return the created game instance.
     */
    Game create(String id, Section section);
}