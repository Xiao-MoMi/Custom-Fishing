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

package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface TotemConfig {

    TotemModel[] totemModels();

    String id();

    boolean isRightPattern(Location location);

    TotemParticle[] particleSettings();

    MathValue<Player> radius();

    MathValue<Player> duration();

    TotemBlock[] totemCore();

    static Builder builder() {
        return new TotemConfigImpl.BuilderImpl();
    }

    interface Builder {

        Builder id(String id);

        Builder totemModels(TotemModel[] totemModels);

        Builder particleSettings(TotemParticle[] particleSettings);

        Builder radius(MathValue<Player> radius);

        Builder duration(MathValue<Player> duration);

        TotemConfig build();
    }
}
