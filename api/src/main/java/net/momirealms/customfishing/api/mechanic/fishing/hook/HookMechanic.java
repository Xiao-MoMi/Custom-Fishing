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

package net.momirealms.customfishing.api.mechanic.fishing.hook;

import net.momirealms.customfishing.api.mechanic.effect.Effect;

/**
 * Interface for managing the mechanics of a fishing hook.
 */
public interface HookMechanic {

    /**
     * Determines if the mechanic can start.
     *
     * @return true if the mechanic can start, false otherwise.
     */
    boolean canStart();

    /**
     * Determines if the mechanic should stop.
     *
     * @return true if the mechanic should stop, false otherwise.
     */
    boolean shouldStop();

    /**
     * Performs pre-start operations for the fishing hook.
     */
    void preStart();

    /**
     * Starts the fishing hook mechanic with a given effect.
     *
     * @param finalEffect the effect to apply when starting the hook.
     */
    void start(Effect finalEffect);

    /**
     * Checks if the fishing hook is hooked.
     *
     * @return true if the fishing hook is hooked, false otherwise.
     */
    boolean isHooked();

    /**
     * Destroys the mechanic.
     */
    void destroy();

    /**
     * Freezes the mechanic.
     */
    void freeze();

    /**
     * Unfreezes the mechanic.
     *
     * @param finalEffect the effect to apply when unfreezing
     */
    void unfreeze(Effect finalEffect);
}
