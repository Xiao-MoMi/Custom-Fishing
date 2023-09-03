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

import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public interface ItemManager {

    @Nullable
    ItemStack build(Player player, String namespace, String value);

    @Nullable
    ItemStack build(Player player, String namespace, String value, Map<String, String> placeholders);

    @NotNull
    ItemStack build(Player player, ItemBuilder builder);

    ItemStack buildAnyItemByID(Player player, String id);

    @Nullable
    String getItemID(ItemStack itemStack);

    String getAnyItemID(ItemStack itemStack);

    @Nullable
    ItemBuilder getItemBuilder(ConfigurationSection section, String type, String id);

    ItemStack build(Player player, ItemBuilder builder, Map<String, String> placeholders);

    Set<Key> getAllItemsKey();

    boolean registerCustomItem(String namespace, String value, BuildableItem buildableItem);

    boolean unregisterCustomItem(String namespace, String value);

    @Nullable
    BuildableItem getBuildableItem(String namespace, String value);

    boolean registerItemLibrary(ItemLibrary itemLibrary);

    boolean unRegisterItemLibrary(ItemLibrary itemLibrary);

    boolean unRegisterItemLibrary(String itemLibrary);

    void dropItem(Player player, Location hookLocation, Location playerLocation, Loot loot, Map<String, String> args);

    void dropItem(Location hookLocation, Location playerLocation, ItemStack itemStack);
}
