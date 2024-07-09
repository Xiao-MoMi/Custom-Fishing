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

package net.momirealms.customfishing.bukkit.item.impl;

import com.saicone.rtag.RtagItem;
import com.saicone.rtag.data.ComponentType;
import net.momirealms.customfishing.bukkit.item.BukkitItemFactory;
import net.momirealms.customfishing.common.item.ComponentKeys;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.util.Key;
import net.momirealms.sparrow.heart.SparrowHeart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class ComponentItemFactory extends BukkitItemFactory {

    public ComponentItemFactory(CustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void customModelData(RtagItem item, Integer data) {
        if (data == null) {
            item.removeComponent(ComponentKeys.CUSTOM_MODEL_DATA);
        } else {
            item.setComponent(ComponentKeys.CUSTOM_MODEL_DATA, data);
        }
    }

    @Override
    protected Optional<Integer> customModelData(RtagItem item) {
        if (!item.hasComponent(ComponentKeys.CUSTOM_MODEL_DATA)) return Optional.empty();
        return Optional.ofNullable(
                (Integer) ComponentType.encodeJava(
                        ComponentKeys.CUSTOM_MODEL_DATA,
                    item.getComponent(ComponentKeys.CUSTOM_MODEL_DATA)
                ).orElse(null)
        );
    }

    @Override
    protected void displayName(RtagItem item, String json) {
        if (json == null) {
            item.removeComponent(ComponentKeys.CUSTOM_NAME);
        } else {
            item.setComponent(ComponentKeys.CUSTOM_NAME, json);
        }
    }

    @Override
    protected Optional<String> displayName(RtagItem item) {
        if (!item.hasComponent(ComponentKeys.CUSTOM_NAME)) return Optional.empty();
        return Optional.ofNullable(
                (String) ComponentType.encodeJava(
                        ComponentKeys.CUSTOM_NAME,
                        item.getComponent(ComponentKeys.CUSTOM_NAME)
                ).orElse(null)
        );
    }

    @Override
    protected void skull(RtagItem item, String skullData) {
        final Map<String, Object> profile = Map.of(
                "properties", List.of(
                        Map.of(
                                "name", "textures",
                                "value", skullData
                        )
                )
        );
        item.setComponent("minecraft:profile", profile);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Optional<List<String>> lore(RtagItem item) {
        if (!item.hasComponent(ComponentKeys.LORE)) return Optional.empty();
        return Optional.ofNullable(
                (List<String>) ComponentType.encodeJava(
                        ComponentKeys.LORE,
                        item.getComponent(ComponentKeys.LORE)
                ).orElse(null)
        );
    }

    @Override
    protected void lore(RtagItem item, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            item.removeComponent(ComponentKeys.LORE);
        } else {
            item.setComponent(ComponentKeys.LORE, lore);
        }
    }

    @Override
    protected boolean unbreakable(RtagItem item) {
        return item.isUnbreakable();
    }

    @Override
    protected void unbreakable(RtagItem item, boolean unbreakable) {
        item.setUnbreakable(unbreakable);
    }

    @Override
    protected Optional<Boolean> glint(RtagItem item) {
        return Optional.ofNullable((Boolean) item.getComponent(ComponentKeys.ENCHANTMENT_GLINT_OVERRIDE));
    }

    @Override
    protected void glint(RtagItem item, Boolean glint) {
        item.setComponent(ComponentKeys.ENCHANTMENT_GLINT_OVERRIDE, glint);
    }

    @Override
    protected Optional<Integer> damage(RtagItem item) {
        if (!item.hasComponent(ComponentKeys.DAMAGE)) return Optional.empty();
        return Optional.ofNullable(
                (Integer) ComponentType.encodeJava(
                        ComponentKeys.DAMAGE,
                        item.getComponent(ComponentKeys.DAMAGE)
                ).orElse(null)
        );
    }

    @Override
    protected void damage(RtagItem item, Integer damage) {
        if (damage == null) damage = 0;
        item.setComponent(ComponentKeys.DAMAGE, damage);
    }

    @Override
    protected Optional<Integer> maxDamage(RtagItem item) {
        if (!item.hasComponent(ComponentKeys.MAX_DAMAGE)) return Optional.of((int) item.getItem().getType().getMaxDurability());
        return Optional.ofNullable(
                (Integer) ComponentType.encodeJava(
                        ComponentKeys.MAX_DAMAGE,
                        item.getComponent(ComponentKeys.MAX_DAMAGE)
                ).orElse(null)
        );
    }

    @Override
    protected void maxDamage(RtagItem item, Integer damage) {
        if (damage == null) {
            item.removeComponent(ComponentKeys.MAX_DAMAGE);
        } else {
            item.setComponent(ComponentKeys.MAX_DAMAGE, damage);
        }
    }

    @Override
    protected void enchantments(RtagItem item, Map<Key, Short> enchantments) {
        Map<String, Integer> enchants = new HashMap<>();
        for (Map.Entry<Key, Short> entry : enchantments.entrySet()) {
            enchants.put(entry.getKey().toString(), Integer.valueOf(entry.getValue()));
        }
        item.setComponent(ComponentKeys.ENCHANTMENTS, enchants);
    }

    @Override
    protected void storedEnchantments(RtagItem item, Map<Key, Short> enchantments) {
        Map<String, Integer> enchants = new HashMap<>();
        for (Map.Entry<Key, Short> entry : enchantments.entrySet()) {
            enchants.put(entry.getKey().toString(), Integer.valueOf(entry.getValue()));
        }
        item.setComponent(ComponentKeys.STORED_ENCHANTMENTS, enchants);
    }

    @Override
    protected void addEnchantment(RtagItem item, Key enchantment, int level) {
        Object enchant = item.getComponent(ComponentKeys.ENCHANTMENTS);
        Map<String, Integer> map = SparrowHeart.getInstance().itemEnchantmentsToMap(enchant);
        map.put(enchantment.toString(), level);
        item.setComponent(ComponentKeys.ENCHANTMENTS, map);
    }

    @Override
    protected void addStoredEnchantment(RtagItem item, Key enchantment, int level) {
        Object enchant = item.getComponent(ComponentKeys.STORED_ENCHANTMENTS);
        Map<String, Integer> map = SparrowHeart.getInstance().itemEnchantmentsToMap(enchant);
        map.put(enchantment.toString(), level);
        item.setComponent(ComponentKeys.STORED_ENCHANTMENTS, map);
    }

    @Override
    protected void itemFlags(RtagItem item, List<String> flags) {
        throw new UnsupportedOperationException("This feature is not available on 1.20.5+");
    }
}