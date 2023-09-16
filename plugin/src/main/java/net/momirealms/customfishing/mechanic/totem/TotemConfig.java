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

package net.momirealms.customfishing.mechanic.totem;

import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.mechanic.totem.block.TotemBlock;
import net.momirealms.customfishing.mechanic.totem.particle.ParticleSetting;
import org.bukkit.Location;

public class TotemConfig {

    private String key;
    private TotemModel[] totemModels;
    private ParticleSetting[] particleSettings;
    private Requirement[] requirements;
    private double radius;
    private int duration;

    public TotemModel[] getTotemModels() {
        return totemModels;
    }

    public Requirement[] getRequirements() {
        return requirements;
    }

    public String getKey() {
        return key;
    }

    public boolean isRightPattern(Location location) {
        for (TotemModel totemModel : totemModels) {
            if (totemModel.isPatternSatisfied(location)) {
                return true;
            }
        }
        return false;
    }

    public ParticleSetting[] getParticleSettings() {
        return particleSettings;
    }

    public double getRadius() {
        return radius;
    }

    public int getDuration() {
        return duration;
    }

    public TotemBlock[] getTotemCore() {
        return totemModels[0].getTotemCore();
    }

    public static class Builder {

        private final TotemConfig config;

        public Builder(String key) {
            this.config = new TotemConfig();
            this.config.key = key;
        }

        public Builder setTotemModels(TotemModel[] totemModels) {
            config.totemModels = totemModels;
            return this;
        }

        public Builder setParticleSettings(ParticleSetting[] particleSettings) {
            config.particleSettings = particleSettings;
            return this;
        }

        public Builder setRequirements(Requirement[] requirements) {
            config.requirements = requirements;
            return this;
        }

        public Builder setRadius(double radius) {
            config.radius = radius;
            return this;
        }

        public Builder setDuration(int duration) {
            config.duration = duration;
            return this;
        }

        public TotemConfig build() {
            return config;
        }
    }
}
