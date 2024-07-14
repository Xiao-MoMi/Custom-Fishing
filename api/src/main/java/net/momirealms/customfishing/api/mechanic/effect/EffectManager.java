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

package net.momirealms.customfishing.api.mechanic.effect;

import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.common.plugin.feature.Reloadable;

import java.util.Optional;

/**
 * Interface for managing effect modifiers.
 */
public interface EffectManager extends Reloadable {

    /**
     * Registers an effect modifier for a specific mechanic type.
     *
     * @param effect the effect modifier to register.
     * @param type the type of mechanic.
     * @return true if registration is successful, false otherwise.
     */
    boolean registerEffectModifier(EffectModifier effect, MechanicType type);

    /**
     * Retrieves an effect modifier by its ID and mechanic type.
     *
     * @param id the ID of the effect modifier.
     * @param type the type of mechanic.
     * @return an Optional containing the effect modifier if found, otherwise empty.
     */
    Optional<EffectModifier> getEffectModifier(String id, MechanicType type);
}
