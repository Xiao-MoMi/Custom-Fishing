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

package net.momirealms.customfishing.common.item;

import net.momirealms.customfishing.common.util.Key;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Item<I> {

    Item<I> customModelData(Integer data);

    Optional<Integer> customModelData();

    Item<I> damage(Integer data);

    Optional<Integer> damage();

    Item<I> maxDamage(Integer data);

    Optional<Integer> maxDamage();

    Item<I> displayName(String displayName);

    Optional<String> displayName();

    Item<I> lore(List<String> lore);

    Optional<List<String>> lore();

    Item<I> unbreakable(boolean unbreakable);

    boolean unbreakable();

    Item<I> skull(String data);

    Item<I> enchantments(Map<Key, Short> enchantments);

    Item<I> addEnchantment(Key enchantment, int level);

    Item<I> storedEnchantments(Map<Key, Short> enchantments);

    Item<I> addStoredEnchantment(Key enchantment, int level);

    Item<I> itemFlags(List<String> flags);

    Optional<Object> getTag(Object... path);

    Item<I> setTag(Object value, Object... path);

    boolean hasTag(Object... path);

    boolean removeTag(Object... path);

    I getItem();

    I load();

    I loadCopy();

    void update();
}
