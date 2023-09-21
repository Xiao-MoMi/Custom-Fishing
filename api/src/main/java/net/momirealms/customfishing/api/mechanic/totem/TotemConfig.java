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

/**
 * This class represents the configuration for a totem.
 * It defines various settings and properties for the totem.
 */
public class TotemConfig {

    private String key;
    private TotemModel[] totemModels;
    private TotemParticle[] particleSettings;
    private Requirement[] requirements;
    private double radius;
    private int duration;

    /**
     * Get the array of totem models that define the totem's pattern.
     *
     * @return An array of TotemModel objects.
     */
    public TotemModel[] getTotemModels() {
        return totemModels;
    }

    /**
     * Get the array of requirements for totem activation.
     *
     * @return An array of Requirement objects.
     */
    public Requirement[] getRequirements() {
        return requirements;
    }

    /**
     * Get the unique key associated with this totem configuration.
     *
     * @return The unique key as a string.
     */
    public String getKey() {
        return key;
    }

    /**
     * Check if the provided location matches any of the totem model patterns.
     *
     * @param location The location to check.
     * @return True if the location matches a totem model pattern, false otherwise.
     */
    public boolean isRightPattern(Location location) {
        for (TotemModel totemModel : totemModels) {
            if (totemModel.isPatternSatisfied(location)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the array of particle settings for the totem's visual effects.
     *
     * @return An array of TotemParticle objects.
     */
    public TotemParticle[] getParticleSettings() {
        return particleSettings;
    }

    /**
     * Get the activation radius of the totem.
     *
     * @return The activation radius as a double.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * Get the duration of the totem's effect when activated.
     *
     * @return The duration in seconds as an integer.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Get the totem core associated with the first totem model.
     * This is used for some internal functionality.
     *
     * @return An array of TotemBlock objects representing the totem core.
     */
    public TotemBlock[] getTotemCore() {
        return totemModels[0].getTotemCore();
    }

    public static Builder builder(String key) {
        return new Builder(key);
    }

    /**
     * This class represents a builder for creating instances of TotemConfig.
     * It allows for the convenient construction of TotemConfig objects with various settings.
     */
    public static class Builder {

        private final TotemConfig config;

        public Builder(String key) {
            this.config = new TotemConfig();
            this.config.key = key;
        }

        /**
         * Sets the totem models for the TotemConfig being built.
         *
         * @param totemModels An array of TotemModel objects representing different totem models.
         * @return The builder instance to allow for method chaining.
         */
        public Builder setTotemModels(TotemModel[] totemModels) {
            config.totemModels = totemModels;
            return this;
        }

        /**
         * Sets the particle settings for the TotemConfig being built.
         *
         * @param particleSettings An array of TotemParticle objects representing particle settings.
         * @return The builder instance to allow for method chaining.
         */
        public Builder setParticleSettings(TotemParticle[] particleSettings) {
            config.particleSettings = particleSettings;
            return this;
        }

        /**
         * Sets the requirements for the TotemConfig being built.
         *
         * @param requirements An array of Requirement objects representing activation requirements.
         * @return The builder instance to allow for method chaining.
         */
        public Builder setRequirements(Requirement[] requirements) {
            config.requirements = requirements;
            return this;
        }

        /**
         * Sets the radius for the TotemConfig being built.
         *
         * @param radius The activation radius for the totem.
         * @return The builder instance to allow for method chaining.
         */
        public Builder setRadius(double radius) {
            config.radius = radius;
            return this;
        }

        /**
         * Sets the duration for the TotemConfig being built.
         *
         * @param duration The duration of the totem's effect.
         * @return The builder instance to allow for method chaining.
         */
        public Builder setDuration(int duration) {
            config.duration = duration;
            return this;
        }

        /**
         * Builds and returns the finalized TotemConfig object.
         *
         * @return The constructed TotemConfig object.
         */
        public TotemConfig build() {
            return config;
        }
    }
}
