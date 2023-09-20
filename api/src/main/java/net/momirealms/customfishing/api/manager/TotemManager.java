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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import org.bukkit.Location;

public interface TotemManager {

    /**
     * Get the EffectCarrier associated with an activated totem located near the specified location.
     *
     * @param location The location to search for activated totems.
     * @return The EffectCarrier associated with the nearest activated totem or null if none are found.
     */
    EffectCarrier getTotemEffect(Location location);
}
