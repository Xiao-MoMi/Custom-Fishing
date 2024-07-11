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

package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.common.plugin.feature.Reloadable;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for managing fishing hooks in the custom fishing plugin.
 */
public interface FishingManager extends Reloadable {

    /**
     * Retrieves the custom fishing hook associated with the specified player.
     *
     * @param player the player.
     * @return an Optional containing the custom fishing hook if found, or an empty Optional if not found.
     */
    Optional<CustomFishingHook> getFishHook(Player player);

    /**
     * Retrieves the custom fishing hook associated with the specified player UUID.
     *
     * @param player the UUID of the player.
     * @return an Optional containing the custom fishing hook if found, or an empty Optional if not found.
     */
    Optional<CustomFishingHook> getFishHook(UUID player);

    /**
     * Retrieves the owner of the specified fish hook.
     *
     * @param hook the fish hook.
     * @return an Optional containing the owner if found, or an empty Optional if not found.
     */
    Optional<Player> getOwner(FishHook hook);

    /**
     * Destroys the custom fishing hook associated with the specified player UUID.
     *
     * @param player the UUID of the player.
     */
    void destroyHook(UUID player);
}
