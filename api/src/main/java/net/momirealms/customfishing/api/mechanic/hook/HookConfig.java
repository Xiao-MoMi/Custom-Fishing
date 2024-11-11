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

package net.momirealms.customfishing.api.mechanic.hook;

import java.util.List;

/**
 * Represents the configuration for a fishing hook.
 */
public interface HookConfig {

    /**
     * Gets the identifier of the hook.
     *
     * @return the identifier of the hook.
     */
    String id();

    /**
     * Gets the additional lore of the hook.
     *
     * @return a list of additional lore strings for the hook.
     */
    List<String> lore();

    /**
     * Gets the max usages of the hook
     *
     * @return the max usages
     */
    int maxUsages();

    /**
     * Creates a new builder for constructing {@link HookConfig} instances.
     *
     * @return a new {@link Builder} instance.
     */
    static Builder builder() {
        return new HookConfigImpl.BuilderImpl();
    }

    /**
     * Builder interface for constructing {@link HookConfig} instances.
     */
    interface Builder {

        /**
         * Sets the identifier for the hook configuration.
         *
         * @param id the identifier of the hook.
         * @return the current {@link Builder} instance.
         */
        Builder id(String id);

        /**
         * Sets the max usages of the hook
         */
        Builder maxUsages(int maxUsages);

        /**
         * Sets the lore for the hook configuration.
         *
         * @param lore a list of lore strings for the hook.
         * @return the current {@link Builder} instance.
         */
        Builder lore(List<String> lore);

        /**
         * Builds and returns the {@link HookConfig} instance.
         *
         * @return the constructed {@link HookConfig} instance.
         */
        HookConfig build();
    }
}
