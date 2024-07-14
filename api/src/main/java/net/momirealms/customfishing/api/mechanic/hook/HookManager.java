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

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface for managing hooks.
 */
public interface HookManager extends Reloadable {

    /**
     * Registers a new hook configuration.
     *
     * @param hook the {@link HookConfig} to be registered
     * @return true if the hook was successfully registered, false otherwise
     */
    boolean registerHook(HookConfig hook);

    /**
     * Retrieves a hook configuration by its ID.
     *
     * @param id the ID of the hook
     * @return an {@link Optional} containing the {@link HookConfig} if found, or an empty {@link Optional} if not
     */
    @NotNull
    Optional<HookConfig> getHook(String id);

    /**
     * Retrieves the hook ID associated with a given fishing rod.
     *
     * @param rod the {@link ItemStack} representing the fishing rod
     * @return an {@link Optional} containing the hook ID if found, or an empty {@link Optional} if not
     */
    Optional<String> getHookID(ItemStack rod);
}
