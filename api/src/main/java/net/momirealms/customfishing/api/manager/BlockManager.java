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

package net.momirealms.customfishing.api.manager;

import net.momirealms.customfishing.api.mechanic.block.BlockDataModifierBuilder;
import net.momirealms.customfishing.api.mechanic.block.BlockLibrary;
import net.momirealms.customfishing.api.mechanic.block.BlockStateModifierBuilder;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface BlockManager {

    /**
     * Registers a BlockLibrary instance.
     * This method associates a BlockLibrary with its unique identification and adds it to the registry.
     *
     * @param blockLibrary The BlockLibrary instance to register.
     * @return True if the registration was successful (the identification is not already registered), false otherwise.
     */
    boolean registerBlockLibrary(BlockLibrary blockLibrary);

    /**
     * Unregisters a BlockLibrary instance by its identification.
     * This method removes a BlockLibrary from the registry based on its unique identification.
     *
     * @param identification The unique identification of the BlockLibrary to unregister.
     * @return True if the BlockLibrary was successfully unregistered, false if it was not found.
     */
    boolean unregisterBlockLibrary(String identification);

    /**
     * Registers a BlockDataModifierBuilder for a specific type.
     * This method associates a BlockDataModifierBuilder with its type and adds it to the registry.
     *
     * @param type    The type of the BlockDataModifierBuilder to register.
     * @param builder The BlockDataModifierBuilder instance to register.
     * @return True if the registration was successful (the type is not already registered), false otherwise.
     */
    boolean registerBlockDataModifierBuilder(String type, BlockDataModifierBuilder builder);

    /**
     * Registers a BlockStateModifierBuilder for a specific type.
     * This method associates a BlockStateModifierBuilder with its type and adds it to the registry.
     *
     * @param type    The type of the BlockStateModifierBuilder to register.
     * @param builder The BlockStateModifierBuilder instance to register.
     * @return True if the registration was successful (the type is not already registered), false otherwise.
     */
    boolean registerBlockStateModifierBuilder(String type, BlockStateModifierBuilder builder);

    /**
     * Unregisters a BlockDataModifierBuilder with the specified type.
     *
     * @param type The type of the BlockDataModifierBuilder to unregister.
     * @return True if the BlockDataModifierBuilder was successfully unregistered, false otherwise.
     */
    boolean unregisterBlockDataModifierBuilder(String type);

    /**
     * Unregisters a BlockStateModifierBuilder with the specified type.
     *
     * @param type The type of the BlockStateModifierBuilder to unregister.
     * @return True if the BlockStateModifierBuilder was successfully unregistered, false otherwise.
     */
    boolean unregisterBlockStateModifierBuilder(String type);

    /**
     * Summons a falling block at a specified location based on the provided loot.
     * This method spawns a falling block at the given hookLocation with specific properties determined by the loot.
     *
     * @param player         The player who triggered the action.
     * @param hookLocation   The location where the hook is positioned.
     * @param playerLocation The location of the player.
     * @param loot           The loot to be associated with the summoned block.
     */
    void summonBlock(Player player, Location hookLocation, Location playerLocation, Loot loot);

    /**
     * Retrieves the block ID associated with a given Block instance using block detection order.
     * This method iterates through the configured block detection order to find the block's ID
     * by checking different BlockLibrary instances in the specified order.
     *
     * @param block The Block instance for which to retrieve the block ID.
     * @return The block ID
     */
    @NotNull String getAnyPluginBlockID(Block block);
}
