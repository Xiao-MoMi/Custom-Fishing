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

package net.momirealms.customfishing.api.mechanic.item;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.common.Tuple;
import net.momirealms.customfishing.api.mechanic.misc.Value;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemBuilder extends BuildableItem {

    ItemBuilder customModelData(int value);

    ItemBuilder name(String name);

    ItemBuilder amount(int amount);

    ItemBuilder amount(int min_amount, int max_amount);

    ItemBuilder tag(boolean tag, String type, String id);

    ItemBuilder unbreakable(boolean unbreakable);

    ItemBuilder placeable(boolean placeable);

    ItemBuilder lore(List<String> lore);

    ItemBuilder nbt(Map<String, Object> nbt);

    ItemBuilder itemFlag(List<ItemFlag> itemFlags);

    ItemBuilder nbt(ConfigurationSection section);

    ItemBuilder enchantment(List<Pair<String, Short>> enchantments, boolean store);

    ItemBuilder randomEnchantments(List<Tuple<Double, String, Short>> enchantments, boolean store);

    ItemBuilder enchantmentPool(List<Pair<Integer, Value>> amountPairs, List<Pair<Pair<String, Short>, Value>> enchantments, boolean store);

    ItemBuilder maxDurability(int max);

    ItemBuilder price(float base, float bonus);

    ItemBuilder size(Pair<Float, Float> size);

    ItemBuilder stackable(boolean stackable);

    ItemBuilder preventGrabbing(boolean prevent);

    ItemBuilder head(String base64);

    ItemBuilder randomDamage(boolean damage);

    @NotNull
    String getId();

    @NotNull
    String getLibrary();

    int getAmount();

    Collection<ItemPropertyEditor> getEditors();

    ItemBuilder removeEditor(String type);

    ItemBuilder registerCustomEditor(String type, ItemPropertyEditor editor);

    interface ItemPropertyEditor {

        void edit(Player player, NBTItem nbtItem, Map<String, String> placeholders);

        default void edit(Player player, NBTItem nbtItem) {
            edit(player, nbtItem, null);
        }
    }
}
