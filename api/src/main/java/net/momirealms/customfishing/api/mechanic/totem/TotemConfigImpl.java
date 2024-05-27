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

import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.totem.block.TotemBlock;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import static java.util.Objects.requireNonNull;

public class TotemConfigImpl implements TotemConfig {

    private String id;
    private TotemModel[] totemModels;
    private TotemParticle[] particleSettings;
    private Requirement<Player>[] activateRequirements;
    private double radius;
    private int duration;

    public TotemConfigImpl(String id, TotemModel[] totemModels, TotemParticle[] particleSettings, Requirement<Player>[] activateRequirements, double radius, int duration) {
        this.id = id;
        this.totemModels = totemModels;
        this.particleSettings = particleSettings;
        this.activateRequirements = activateRequirements;
        this.radius = radius;
        this.duration = duration;
    }

    @Override
    public TotemModel[] totemModels() {
        return totemModels;
    }

    @Override
    public Requirement<Player>[] activateRequirements() {
        return activateRequirements;
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
    public double radius() {
        return radius;
    }

    @Override
    public int duration() {
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
        private Requirement<Player>[] activateRequirements;
        private double radius;
        private int duration;
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        public Builder totemModels(TotemModel[] totemModels) {
            this.totemModels = totemModels;
            return this;
        }
        public Builder particleSettings(TotemParticle[] particleSettings) {
            this.particleSettings = particleSettings;
            return this;
        }
        public Builder radius(double radius) {
            this.radius = radius;
            return this;
        }
        public Builder duration(int duration) {
            this.duration = duration;
            return this;
        }
        public Builder activateRequirements(Requirement<Player>[] activateRequirements) {
            this.activateRequirements = activateRequirements;
            return this;
        }
        public TotemConfig build() {
            return new TotemConfigImpl(requireNonNull(id), requireNonNull(totemModels), particleSettings, activateRequirements, radius, duration);
        }
    }
}
