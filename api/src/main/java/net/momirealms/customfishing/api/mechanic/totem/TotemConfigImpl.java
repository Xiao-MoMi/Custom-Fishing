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

package net.momirealms.customfishing.api.mechanic.totem;

import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static java.util.Objects.requireNonNull;

public class TotemConfigImpl implements TotemConfig {

    private final String id;
    private final TotemModel[] totemModels;
    private final TotemParticle[] particleSettings;
    private final MathValue<Player> radius;
    private final MathValue<Player> duration;

    public TotemConfigImpl(String id, TotemModel[] totemModels, TotemParticle[] particleSettings, MathValue<Player> radius, MathValue<Player> duration) {
        this.id = id;
        this.totemModels = totemModels;
        this.particleSettings = particleSettings;
        this.radius = radius;
        this.duration = duration;
    }

    @Override
    public TotemModel[] totemModels() {
        return totemModels;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean isRightPattern(Location location) {
        for (TotemModel totemModel : totemModels) {
            if (totemModel.isPatternSatisfied(location)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TotemParticle[] particleSettings() {
        return particleSettings;
    }

    @Override
    public MathValue<Player> radius() {
        return radius;
    }

    @Override
    public MathValue<Player> duration() {
        return duration;
    }

    @Override
    public TotemBlock[] totemCore() {
        return totemModels[0].getTotemCore();
    }

    public static class BuilderImpl implements Builder {
        private String id;
        private TotemModel[] totemModels;
        private TotemParticle[] particleSettings;
        private MathValue<Player> radius;
        private MathValue<Player> duration;
        @Override
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        @Override
        public Builder totemModels(TotemModel[] totemModels) {
            this.totemModels = totemModels;
            return this;
        }
        @Override
        public Builder particleSettings(TotemParticle[] particleSettings) {
            this.particleSettings = particleSettings;
            return this;
        }
        @Override
        public Builder radius(MathValue<Player> radius) {
            this.radius = radius;
            return this;
        }
        @Override
        public Builder duration(MathValue<Player> duration) {
            this.duration = duration;
            return this;
        }
        @Override
        public TotemConfig build() {
            return new TotemConfigImpl(requireNonNull(id), requireNonNull(totemModels), particleSettings, radius, duration);
        }
    }
}
