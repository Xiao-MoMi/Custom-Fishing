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

package net.momirealms.customfishing.api.mechanic.requirement;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.momirealms.customfishing.api.mechanic.context.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RequirementManager<T> {

    /**
     * Registers a custom requirement type with its corresponding factory.
     *
     * @param type               The type identifier of the requirement.
     * @param requirementFactory The factory responsible for creating instances of the requirement.
     * @return True if registration was successful, false if the type is already registered.
     */
    boolean registerRequirement(@NotNull String type, @NotNull RequirementFactory<T> requirementFactory);

    /**
     * Unregisters a custom requirement type.
     *
     * @param type The type identifier of the requirement to unregister.
     * @return True if unregistration was successful, false if the type is not registered.
     */
    boolean unregisterRequirement(@NotNull String type);

    /**
     * Retrieves an array of requirements based on a configuration section.
     *
     * @param section The configuration section containing requirement definitions.
     * @param runActions A flag indicating whether to use advanced requirements.
     * @return An array of Requirement objects based on the configuration section
     */
    @Nullable
    Requirement<T>[] getRequirements(@NotNull Section section, boolean runActions);

    /**
     * If a requirement type exists
     *
     * @param type type
     * @return exists or not
     */
    boolean hasRequirement(@NotNull String type);

    /**
     * Retrieves a Requirement object based on a configuration section and advanced flag.
     * <p>
     * requirement_1:  <- section
     *   type: xxx
     *   value: xxx
     *
     * @param section  The configuration section containing requirement definitions.
     * @param runActions A flag indicating whether to use advanced requirements.
     * @return A Requirement object based on the configuration section, or an EmptyRequirement if the section is null or invalid.
     */
    @NotNull
    Requirement<T> getRequirement(@NotNull Section section, boolean runActions);

    /**
     * Gets a requirement based on the provided type and value.
     * If a valid RequirementFactory is found for the type, it is used to create the requirement.
     * If no factory is found, a warning is logged, and an empty requirement instance is returned.
     * <p>
     * world:     <- type
     *   - world  <- value
     *
     * @param type   The type representing the requirement type.
     * @param value The value associated with the requirement.
     * @return A Requirement instance based on the type and value, or an EmptyRequirement if the type is invalid.
     */
    @NotNull
    Requirement<T> getRequirement(@NotNull String type, @NotNull Object value);

    /**
     * Retrieves a RequirementFactory based on the specified requirement type.
     *
     * @param type The requirement type for which to retrieve a factory.
     * @return A RequirementFactory for the specified type, or null if no factory is found.
     */
    @Nullable
    RequirementFactory<T> getRequirementFactory(@NotNull String type);

    static <T> boolean isSatisfied(Context<T> context, @Nullable Requirement<T>... requirements) {
        if (requirements == null) return true;
        for (Requirement<T> requirement : requirements) {
            if (!requirement.isSatisfied(context)) {
                return false;
            }
        }
        return true;
    }
}
