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
import com.saicone.rtag.tag.TagBase;
import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.tag.TagList;
import com.saicone.rtag.util.EnchantmentTag;
import net.momirealms.customfishing.bukkit.item.BukkitItemFactory;
import net.momirealms.customfishing.common.plugin.CustomFishingPlugin;
import net.momirealms.customfishing.common.util.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class UniversalItemFactory extends BukkitItemFactory {

    public UniversalItemFactory(CustomFishingPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void displayName(RtagItem item, String json) {
        if (json != null) {
            item.set(json, "display", "Name");
        } else {
            item.remove("display", "Name");
        }
    }

    @Override
    protected Optional<String> displayName(RtagItem item) {
        if (!item.hasTag("display", "Name")) return Optional.empty();
        return Optional.of(item.get("display", "Name"));
    }

    @Override
    protected void customModelData(RtagItem item, Integer data) {
        if (data == null) {
            item.remove("CustomModelData");
        } else {
            item.set(data, "CustomModelData");
        }
    }

    @Override
    protected Optional<Integer> customModelData(RtagItem item) {
        if (!item.hasTag("CustomModelData")) return Optional.empty();
        return Optional.of(item.get("CustomModelData"));
    }

    @Override
    protected void skull(RtagItem item, String skullData) {
        if (skullData == null) {
            item.remove("SkullOwner");
        } else {
            item.set(List.of(Map.of("Value", skullData)), "SkullOwner", "Properties", "textures");
        }
    }

    @Override
    protected Optional<List<String>> lore(RtagItem item) {
        if (!item.hasTag("display", "Lore")) return Optional.empty();
        return Optional.of(item.get("display", "Lore"));
    }

    @Override
    protected void lore(RtagItem item, List<String> lore) {
        if (lore == null || lore.isEmpty()) {
            item.remove("display", "Lore");
        } else {
            item.set(lore, "display", "Lore");
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
        return Optional.of(false);
    }

    @Override
    protected void glint(RtagItem item, Boolean glint) {
        throw new UnsupportedOperationException("This feature is only available on 1.20.5+");
    }

    @Override
    protected Optional<Integer> damage(RtagItem item) {
        if (!item.hasTag("Damage")) return Optional.empty();
        return Optional.of(item.get("Damage"));
    }

    @Override
    protected void damage(RtagItem item, Integer damage) {
        item.set(damage, "Damage");
    }

    @Override
    protected void enchantments(RtagItem item, Map<Key, Short> enchantments) {
        ArrayList<Object> tags = new ArrayList<>();
        for (Map.Entry<Key, Short> entry : enchantments.entrySet()) {
            tags.add((Map.of("id", entry.getKey().toString(), "lvl", entry.getValue())));
        }
        item.set(tags, "Enchantments");
    }

    @Override
    protected void storedEnchantments(RtagItem item, Map<Key, Short> enchantments) {
        ArrayList<Object> tags = new ArrayList<>();
        for (Map.Entry<Key, Short> entry : enchantments.entrySet()) {
            tags.add((Map.of("id", entry.getKey().toString(), "lvl", entry.getValue())));
        }
        item.set(tags, "StoredEnchantments");
    }

    @Override
    protected void addEnchantment(RtagItem item, Key enchantment, int level) {
        Object enchantments = item.getExact("Enchantments");
        if (enchantments != null) {
            for (Object enchant : TagList.getValue(enchantments)) {
                if (TagBase.getValue(TagCompound.get(enchant, "id")).equals(enchant.toString())) {
                    TagCompound.set(enchant, "lvl", TagBase.newTag(level));
                    return;
                }
            }
            item.add(Map.of("id", enchantment.toString(), "lvl", (short) level), "Enchantments");
        } else {
            item.set(List.of(Map.of("id", enchantment.toString(), "lvl", (short) level)), "Enchantments");
        }
    }

    @Override
    protected void addStoredEnchantment(RtagItem item, Key enchantment, int level) {
        Object enchantments = item.getExact("StoredEnchantments");
        if (enchantments != null) {
            for (Object enchant : TagList.getValue(enchantments)) {
                if (TagBase.getValue(TagCompound.get(enchant, "id")).equals(enchant.toString())) {
                    TagCompound.set(enchant, "lvl", TagBase.newTag(level));
                    return;
                }
            }
            item.add(Map.of("id", enchantment.toString(), "lvl", (short) level), "StoredEnchantments");
        } else {
            item.set(List.of(Map.of("id", enchantment.toString(), "lvl", (short) level)), "StoredEnchantments");
        }
    }
}